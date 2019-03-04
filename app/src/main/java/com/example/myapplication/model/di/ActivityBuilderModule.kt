package com.example.myapplication.model.di

import com.example.myapplication.view.DetailActivity
import com.example.myapplication.view.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector
    abstract fun provideMainActivityInjector(): MainActivity

    @ContributesAndroidInjector
    abstract fun provideDetailActivityInjector(): DetailActivity
}