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

    // Actualizamos la función para recibir el correo
    fun setUserData(id: Int, name: String, email: String, isPremium: Boolean) {
        _userId.value = id
        _userName.value = name
        _userEmail.value = email
        _isPremium.value = isPremium
    }

    fun clearData() {
        _userId.value = -1
        _userName.value = ""
        _userEmail.value = ""
        _isPremium.value = false
    }
}