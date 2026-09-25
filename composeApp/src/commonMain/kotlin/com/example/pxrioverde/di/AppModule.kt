package com.example.pxrioverde.di

import com.example.pxrioverde.database.AppDatabase
import com.example.pxrioverde.database.getDatabase
import com.example.pxrioverde.repository.FeedRepository
import com.example.pxrioverde.repository.TicketRepository
import com.example.pxrioverde.repository.TripRepository
import com.example.pxrioverde.repository.AbsenceRepository
import com.example.pxrioverde.repository.PurchaseRepository
import com.example.pxrioverde.repository.UserPreferencesRepository
import com.example.pxrioverde.service.AuthService
import com.example.pxrioverde.service.NotificationService
import com.example.pxrioverde.service.supabase
import com.example.pxrioverde.viewmodel.*
import com.example.pxrioverde.domain.usecase.GetAnalyticsDashboardUseCase
import com.example.pxrioverde.domain.usecase.SubmitTicketUseCase
import com.example.pxrioverde.domain.usecase.SyncTripsUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect fun platformModule(): Module

val appModule = module {
    includes(platformModule())

    // Database
    single { getDatabase() }
    single { get<AppDatabase>().ticketDao() }
    single { get<AppDatabase>().messageDao() }
    single { get<AppDatabase>().tripDao() }

    // Supabase
    single { supabase }

    // Services
    singleOf(::AuthService)
    singleOf(::NotificationService)

    // Repositories
    singleOf(::UserPreferencesRepository)
    singleOf(::FeedRepository)
    single { TicketRepository(get(), get(), get()) }
    single { TripRepository(get(), get(), get()) }
    single { AbsenceRepository(get()) }
    single { PurchaseRepository(get()) }

    // UseCases
    singleOf(::GetAnalyticsDashboardUseCase)
    singleOf(::SubmitTicketUseCase)
    singleOf(::SyncTripsUseCase)

    // ViewModels
    viewModelOf(::TicketViewModel)
    viewModelOf(::TripViewModel)
    viewModelOf(::AnalyticsViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::AbsenceViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::AdminViewModel)
    viewModelOf(::PurchaseViewModel)
    viewModelOf(::FeedViewModel)
}
