/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.myapplication.model.utils

import okhttp3.*
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.TimeUnit

/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * [application interceptor][OkHttpClient.interceptors] or as a [ ][OkHttpClient.networkInterceptors].
 *
 * The format of the logs created by
 * this class should not be considered stable and may change slightly between releases. If you need
 * a stable logging format, use your own interceptor.
 *
 *
 * it was forked from the original class to provide different log levels to request and responses.
 * Because we have to log requests body, because all of them have same endpoint.
 * But we can't log response body because it is very huge.
 */
class RequestResponseHttpLoggingInterceptor @JvmOverloads constructor(private val logger: Logger = Logger.DEFAULT) :
    Interceptor {
    @Volatile
    private var requestLevel = Level.NONE
    @Volatile
    private var responseLevel = Level.NONE

    enum class Level {
        /**
         * No logs.
         */
        NONE,
        /**
         * logs only http errors.
         */
        ERROR,
        /**
         * Logs request and response lines.
         *
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
        `</pre> *
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
        `</pre> *
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective bodies (if present).
         *
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
        `</pre> *
         */
        ONLY_BODY,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
        `</pre> *
         */
        FULL
    }

    /**
     * Change the level at which this interceptor logs.
     */
    fun setRequestLevel(level: Level?): RequestResponseHttpLoggingInterceptor {
        if (level == null) {
            throw NullPointerException("level == null. Use Level.NONE instead.")
        }
        this.requestLevel = level
        return this
    }

    /**
     * Change the level at which this interceptor logs.
     */
    fun setResponseLevel(level: Level?): RequestResponseHttpLoggingInterceptor {
        if (level == null) {
            throw NullPointerException("level == null. Use Level.NONE instead.")
        }
        this.responseLevel = level
        return this
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestLevel = this.requestLevel
        val responseLevel = this.responseLevel
        logRequest(requestLevel, chain)
        return logResponse(responseLevel, chain)
    }

    @Throws(IOException::class)
    private fun logRequest(level: Level, chain: Interceptor.Chain) {
        val request = chain.request()
        if (level == Level.NONE || level == Level.ERROR) {
            return
        }

        val logBody = level == Level.FULL || level == Level.ONLY_BODY
        val logHeaders = level == Level.HEADERS || level == Level.FULL

        val requestBody = request.body()
        val hasRequestBody = requestBody != null

        val connection = chain.connection()
        val protocol = if (connection != null) connection.protocol() else Protocol.HTTP_1_1
        var requestStartMessage = "--> " + request.method() + ' '.toString() + request.url() + ' '.toString() + protocol
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody!!.contentLength() + "-byte body)"
        }
        logger.log(requestStartMessage)

        if (logHeaders || logBody) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody!!.contentType() != null) {
                    logger.log("Content-Type: " + requestBody.contentType()!!)
                }
                if (requestBody.contentLength() != -1L) {
                    logger.log("Content-Length: " + requestBody.contentLength())
                }
            }

            if (logHeaders) {
                val headers = request.headers()
                var i = 0
                val count = headers.size()
                while (i < count) {
                    val name = headers.name(i)
                    // Skip headers from the request body as they are explicitly logged above.
                    if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(
                            name,
                            ignoreCase = true
                        )
                    ) {
                        logger.log(name + ": " + headers.value(i))
                    }
                    i++
                }
            }

            if (!logBody || !hasRequestBody) {
                logger.log("--> END " + request.method())
            } else if (bodyEncoded(request.headers())) {
                logger.log("--> END " + request.method() + " (encoded body omitted)")
            } else {
                val buffer = Buffer()
                requestBody!!.writeTo(buffer)

                var charset: Charset? = UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }

                logger.log("")
                if (isPlaintext(buffer)) {
                    logger.log(buffer.readString(charset!!))
                    logger.log(
                        "--> END " + request.method()
                                + " (" + requestBody.contentLength() + "-byte body)"
                    )
                } else {
                    logger.log(
                        ("--> END " + request.method() + " (binary "
                                + requestBody.contentLength() + "-byte body omitted)")
                    )
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun logResponse(level: Level, chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        val logBody = level == Level.FULL || level == Level.ONLY_BODY
        val logHeaders = level == Level.FULL || level == Level.HEADERS

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.log("<-- HTTP FAILED: $e")
            throw e
        }

        if (level == Level.ERROR) {
            return response
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body()
        val contentLength = responseBody!!.contentLength()
        val bodySize = if (contentLength != -1L) (contentLength).toString() + "-byte" else "unknown-length"
        logger.log(
            ("<-- " + response.code() + ' '.toString() + response.message() + ' '.toString()
                    + response.request().url() + " (" + tookMs + "ms" + (if (!logHeaders)
                (", "
                        + bodySize + " body")
            else
                "") + ')'.toString())
        )

        if (logHeaders || logBody) {
            if (logHeaders) {
                val headers = response.headers()
                var i = 0
                val count = headers.size()
                while (i < count) {
                    logger.log(headers.name(i) + ": " + headers.value(i))
                    i++
                }
            }

            if (!logBody || !HttpHeaders.hasBody(response)) {
                logger.log("<-- END HTTP")
            } else if (bodyEncoded(response.headers())) {
                logger.log("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody.source()
                source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                val buffer = source.buffer()

                var charset: Charset? = UTF8
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF8)
                    } catch (e: UnsupportedCharsetException) {
                        logger.log("")
                        logger.log("Couldn't decode the response body; charset is likely malformed.")
                        logger.log("<-- END HTTP")

                        return response
                    }

                }

                if (!isPlaintext(buffer)) {
                    logger.log("")
                    logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)")
                    return response
                }

                if (contentLength != 0L) {
                    logger.log("")
                    logger.log(buffer.clone().readString(charset!!))
                }

                logger.log("<-- END HTTP (" + buffer.size() + "-byte body)")
            }
        }

        return response
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        internal fun isPlaintext(buffer: Buffer): Boolean {
            try {
                val prefix = Buffer()
                val byteCount = if (buffer.size() < 64) buffer.size() else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (e: EOFException) {
                return false // Truncated UTF-8 sequence.
            }

        }
    }
}
