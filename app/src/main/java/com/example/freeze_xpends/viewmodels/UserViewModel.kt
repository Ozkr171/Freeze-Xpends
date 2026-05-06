package com.example.freeze_xpends.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {
    private val _userId = MutableStateFlow(-1)
    val userId: StateFlow<Int> = _userId.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    fun setUsuario(id: Int, nombreStr: String, premium: Boolean) {
        _userId.value = id
        _nombre.value = nombreStr
        _isPremium.value = premium
    }

    fun setPremium(premium: Boolean) {
        _isPremium.value = premium
    }

    fun logout() {
        _userId.value = -1
        _nombre.value = ""
        _isPremium.value = false
    }
}