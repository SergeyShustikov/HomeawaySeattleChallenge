package com.example.myapplication.model.di

import com.example.myapplication.BuildConfig
import com.example.myapplication.model.utils.ErrorHttpLoggingInterceptor
import com.example.myapplication.model.utils.RequestResponseHttpLoggingInterceptor
import com.example.myapplication.model.utils.TimberErrorLogAdapter
import com.example.myapplication.model.utils.TimberLogAdapter
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return httpLoggingInterceptor
    }

    @Provides
    @Singleton
    fun provideHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
        val requestResponseInterceptor = RequestResponseHttpLoggingInterceptor(TimberLogAdapter())
            .apply {
                setRequestLevel(RequestResponseHttpLoggingInterceptor.Level.FULL)
                setResponseLevel(RequestResponseHttpLoggingInterceptor.Level.FULL)
            }

        val errorHttpLoggingInterceptor = ErrorHttpLoggingInterceptor(TimberErrorLogAdapter())
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(requestResponseInterceptor)
            .addInterceptor(errorHttpLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}