package com.sonms.aishortcut.presentation.main

import com.sonms.aishortcut.core.translate.Translator
import com.sonms.aishortcut.core.translate.createTranslator
import com.sonms.aishortcut.data.githubtrending.GithubTrendingRepository
import com.sonms.aishortcut.data.hftrending.HfTrendingRepository
import com.sonms.aishortcut.data.newsfeed.NewsFeedRepository
import com.sonms.aishortcut.data.saved.SavedRepository
import com.sonms.aishortcut.presentation.discover.DiscoverViewModel
import com.sonms.aishortcut.presentation.home.HomeViewModel
import com.sonms.aishortcut.presentation.saved.SavedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// The one Koin module for the whole app. Split it per feature
// (homeModule, savedModule, ...) once the list is long enough to be hard to
// scan, or when a new feature module is added -- whichever comes first.
val appModule = module {
    single<Translator> { createTranslator() }
    single { HfTrendingRepository() }
    single { GithubTrendingRepository() }
    single { NewsFeedRepository() }
    single { SavedRepository() }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { DiscoverViewModel(get(), get()) }
    viewModel { SavedViewModel(get()) }
}
