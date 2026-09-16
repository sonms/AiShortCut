package com.sonms.aishortcut.presentation.main

import com.sonms.aishortcut.core.database.AiShortCutDatabase
import com.sonms.aishortcut.core.database.createDatabase
import com.sonms.aishortcut.core.network.createHttpClient
import com.sonms.aishortcut.core.translate.Translator
import com.sonms.aishortcut.core.translate.createTranslator
import com.sonms.aishortcut.data.githubtrending.GithubTrendingRepository
import com.sonms.aishortcut.data.hftrending.HfTrendingRepository
import com.sonms.aishortcut.data.home.HomeRepository
import com.sonms.aishortcut.data.newsfeed.NewsFeedRepository
import com.sonms.aishortcut.data.openrouter.OpenRouterRepository
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
    // One HttpClient for the whole app -- every remote repository shares this
    // connection pool and engine rather than standing up its own.
    single { createHttpClient() }
    single<Translator> { createTranslator() }
    // One Room database for the whole app; repositories take the DAO they need.
    single { createDatabase() }
    single { get<AiShortCutDatabase>().savedArticleDao() }
    single { get<AiShortCutDatabase>().homeVisitDao() }
    single { get<AiShortCutDatabase>().savedModelDao() }
    single { HfTrendingRepository(get()) }
    single { GithubTrendingRepository(get()) }
    single { NewsFeedRepository(get()) }
    single { OpenRouterRepository(get()) }
    single { SavedRepository(get(), get()) }
    single { HomeRepository(get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { DiscoverViewModel(get(), get(), get(), get(), get()) }
    viewModel { SavedViewModel(get()) }
}
