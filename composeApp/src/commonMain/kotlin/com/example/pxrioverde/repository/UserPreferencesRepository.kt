package com.example.pxrioverde.repository

import com.example.pxrioverde.util.LocalStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository {

    private val _userSector = MutableStateFlow(LocalStorage.getString(KEY_USER_SECTOR, "Geral"))
    val userSector = _userSector.asStateFlow()

    private val _machineName = MutableStateFlow(LocalStorage.getString(KEY_MACHINE_NAME, ""))
    val machineName = _machineName.asStateFlow()

    fun updatePreferences(sector: String, machine: String) {
        _userSector.value = sector
        _machineName.value = machine
        
        LocalStorage.putString(KEY_USER_SECTOR, sector)
        LocalStorage.putString(KEY_MACHINE_NAME, machine)
    }

    companion object {
        const val KEY_USER_SECTOR = "user_sector"
        const val KEY_MACHINE_NAME = "machine_name"
    }
}
