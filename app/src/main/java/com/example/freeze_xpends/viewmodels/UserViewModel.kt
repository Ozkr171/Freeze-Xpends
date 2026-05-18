package com.example.freeze_xpends.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {
    private val _userId = MutableStateFlow(-1)
    val userId: StateFlow<Int> = _userId.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _userCurrency = MutableStateFlow("MXN")
    val userCurrency: StateFlow<String> = _userCurrency.asStateFlow()

    private val _userPhoto = MutableStateFlow<String?>(null)
    val userPhoto: StateFlow<String?> = _userPhoto.asStateFlow()

    // --- NUEVA VARIABLE DE FORMATO ---
    private val _userFormat = MutableStateFlow("US")
    val userFormat: StateFlow<String> = _userFormat.asStateFlow()

    fun setUserData(
        id: Int, name: String, email: String, isPremium: Boolean,
        currency: String = "MXN", photo: String? = null, format: String = "US"
    ) {
        _userId.value = id
        _userName.value = name
        _userEmail.value = email
        _isPremium.value = isPremium
        _userCurrency.value = currency
        _userPhoto.value = photo
        _userFormat.value = format
    }

    fun clearData() {
        _userId.value = -1
        _userName.value = ""
        _userEmail.value = ""
        _isPremium.value = false
        _userCurrency.value = "MXN"
        _userPhoto.value = null
        _userFormat.value = "US"
    }
}