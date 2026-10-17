package com.premsingh.pantrypal.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PantryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = PantryDatabase.get(application).pantryDao()
    val entries = dao.observeEntries().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String, category: String, quantity: String, daysUntilExpiry: Int) {
        if (name.isBlank() || quantity.isBlank() || daysUntilExpiry < 0) return
        val today = System.currentTimeMillis() / 86_400_000L
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(PantryEntry(name = name.trim(), category = category, quantity = quantity.trim(), expiryEpochDay = today + daysUntilExpiry))
        }
    }

    fun markUsed(id: Long) = viewModelScope.launch(Dispatchers.IO) { dao.delete(id) }
}
