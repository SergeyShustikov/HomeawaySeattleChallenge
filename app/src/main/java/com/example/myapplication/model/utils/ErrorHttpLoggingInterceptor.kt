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

class ErrorHttpLoggingInterceptor @JvmOverloads constructor(private val logger: Logger = Logger.DEFAULT) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response
        val request = chain.request()

        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.log("<-- HTTP FAILED: $e")
            throw e
        }

        val isValid = checkResponse(response)
        if (!isValid) {
            logRequest(chain.connection(), request)
            logResponse(response)
        }
        return response
    }


    @Throws(IOException::class)
    private fun checkResponse(response: Response): Boolean {
        val responseBody = response.body()
        val contentLength = responseBody!!.contentLength()

        if (HttpHeaders.hasBody(response) && !bodyEncoded(response.headers())) {
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()

            var charset: Charset? = UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8)
                } catch (e: UnsupportedCharsetException) {
                    return false
                }

            }

            if (contentLength != 0L) {
                val result = buffer.clone().readString(charset!!)
                if (result.startsWith(PROVISIONS)) {
                    return true
                }

                if ((result.startsWith(START_WITH_SINGLE) || result.startsWith(START_WITH)) && (result.contains(
                        OK_MARKER
                    ) || result.contains(NO_NEW_DATA_MARKER))
                ) {
                    return true
                }
            }
        }

        return false
    }


    @Throws(IOException::class)
    private fun logRequest(connection: Connection?, request: Request) {
        val requestBody = request.body()
        val hasRequestBody = requestBody != null
        val protocol = if (connection != null) connection.protocol() else Protocol.HTTP_1_1
        val requestStartMessage = "--> " + request.method() + ' '.toString() + request.url() + ' '.toString() + protocol
        logger.log(requestStartMessage)

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

        val headers = request.headers()
        var i = 0
        val count = headers.size()
        while (i < count) {
            val name = headers.name(i)
            // Skip headers from the request body as they are explicitly logged above.
            if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(name, ignoreCase = true)) {
                logger.log(name + ": " + headers.value(i))
            }
            i++
        }

        if (!hasRequestBody) {
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
                    "--> END " + request.method() + " (binary "
                            + requestBody.contentLength() + "-byte body omitted)"
                )
            }
        }
    }

    @Throws(IOException::class)
    private fun logResponse(response: Response) {
        val responseBody = response.body()
        val contentLength = responseBody!!.contentLength()
        logger.log("<-- " + response.code() + ' '.toString() + response.message() + ' '.toString() + response.request().url())

        val headers = response.headers()
        var i = 0
        val count = headers.size()
        while (i < count) {
            logger.log(headers.name(i) + ": " + headers.value(i))
            i++
        }

        if (!HttpHeaders.hasBody(response)) {
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

                    return
                }

            }

            if (!isPlaintext(buffer)) {
                logger.log("")
                logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)")
                return
            }

            if (contentLength != 0L) {
                logger.log("")
                logger.log(buffer.clone().readString(charset!!))
            }

            logger.log("<-- END HTTP (" + buffer.size() + "-byte body)")
        }
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
        private const val OK_MARKER = "\"Status\":\"OK\""
        private const val NO_NEW_DATA_MARKER = "\"Status\":\"NoNewData\""
        private const val START_WITH = "[{\"Request\""
        private const val START_WITH_SINGLE = "{\"Request\""
        private const val PROVISIONS = "{\"MasterRegions\""

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        private fun isPlaintext(buffer: Buffer): Boolean {
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
