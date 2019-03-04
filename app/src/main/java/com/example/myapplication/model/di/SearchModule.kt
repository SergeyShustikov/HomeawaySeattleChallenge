package com.example.myapplication.model.di

import android.app.Application
import android.content.Context
import com.example.myapplication.model.rest.SearchApi
import com.example.myapplication.model.storage.FavoritePlacesManager
import com.example.myapplication.model.storage.PlacesRepository
import com.example.myapplication.model.utils.RxSchedulers
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class SearchModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context {
        return app.applicationContext
    }

    @Provides
    @Singleton
    fun provideRxSchedulers(app: Application): RxSchedulers {
        return RxSchedulers(app)
    }

    @Provides
    @Singleton
    fun provideFavoritesManager(context: Context): FavoritePlacesManager {
        return FavoritePlacesManager(context)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(
        searchApi: SearchApi,
        rxSchedulers: RxSchedulers,
        favoritePlacesManager: FavoritePlacesManager
    ): PlacesRepository {
        return PlacesRepository(searchApi, rxSchedulers, favoritePlacesManager)
    }


    @Singleton
    @Provides
    fun provideSearchApi(retrofit: Retrofit): SearchApi =
        retrofit.create(SearchApi::class.java)
}