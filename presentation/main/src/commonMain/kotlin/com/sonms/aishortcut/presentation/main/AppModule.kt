package com.sonms.aishortcut.presentation.main

import com.sonms.aishortcut.data.githubtrending.GithubTrendingRepository
import com.sonms.aishortcut.data.hftrending.HfTrendingRepository
import com.sonms.aishortcut.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// The one Koin module for the whole app. Split it per feature
// (hfTrendingModule, savedModule, ...) once the list is long enough to be
// hard to scan -- not before.
val appModule = module {
    single { HfTrendingRepository() }
    single { GithubTrendingRepository() }
    viewModel { HomeViewModel(get(), get()) }
}
