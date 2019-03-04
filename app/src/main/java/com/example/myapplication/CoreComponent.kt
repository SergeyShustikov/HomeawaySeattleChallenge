package com.example.myapplication

import android.app.Application
import com.example.myapplication.model.di.NetworkModule
import com.example.myapplication.model.di.ActivityBuilderModule
import com.example.myapplication.model.di.SearchModule
import com.example.myapplication.model.di.ViewModelFactoryModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        ViewModelFactoryModule::class,
        ActivityBuilderModule::class,
        NetworkModule::class,
        SearchModule::class
    ]
)
interface CoreComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): CoreComponent
    }

    fun inject(application: CommonApplication)
}