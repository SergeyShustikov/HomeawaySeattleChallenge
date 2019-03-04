package com.example.myapplication.view.common

import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

abstract class ViewModelActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
}
