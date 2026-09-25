package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AppScreen {
    object Main : AppScreen()
    data class TicketCreate(val preselectedSector: String? = null) : AppScreen()
    object CarBooking : AppScreen()
    data class VehicleCheckOut(val booking: Booking) : AppScreen()
    object ComunicadoRH : AppScreen()
    object PurchaseRequest : AppScreen()
    object PurchaseHistory : AppScreen()
    object Mural : AppScreen()
}

class MainViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _currentTab = MutableStateFlow(0)
    val currentTab = _currentTab.asStateFlow()

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Main)
    val currentScreen = _currentScreen.asStateFlow()

    val userSector = preferencesRepository.userSector
    val machineName = preferencesRepository.machineName

    fun setTab(tab: Int) {
        _currentTab.value = tab
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun goBack() {
        if (_currentScreen.value != AppScreen.Main) {
            _currentScreen.value = AppScreen.Main
        }
    }
    
    fun clearData() {
        _currentTab.value = 0
        _currentScreen.value = AppScreen.Main
    }
}
