package com.example.shoppinglist.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglist.data.Product
import com.example.shoppinglist.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShoppingViewModel(private val context: Context) : ViewModel() {

    private val settingsRepository = SettingsRepository(context)

    private val _products = mutableStateListOf<Product>()
    val products: SnapshotStateList<Product> = _products

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        viewModelScope.launch {
            // Загружаем настройки темы
            settingsRepository.isDarkTheme.collect { isDark ->
                _isDarkTheme.value = isDark
            }
        }

        // Загружаем сохранённый список продуктов
        viewModelScope.launch {
            settingsRepository.products.collect { savedProducts ->
                if (savedProducts.isNotEmpty()) {
                    _products.clear()
                    _products.addAll(savedProducts)
                }
            }
        }
    }

    private fun saveProducts() {
        viewModelScope.launch {
            settingsRepository.saveProducts(_products.toList())
        }
    }

    fun addProduct(name: String, quantity: Int) {
        val newId = (_products.maxOfOrNull { it.id } ?: 0) + 1
        _products.add(Product(newId, name, quantity))
        saveProducts()
    }

    fun deleteProduct(product: Product) {
        _products.remove(product)
        saveProducts()
    }

    fun updateQuantity(product: Product, newQuantity: Int) {
        val index = _products.indexOf(product)
        if (index != -1) {
            _products[index] = product.copy(quantity = newQuantity)
            saveProducts()
        }
    }

    fun toggleBought(product: Product) {
        val index = _products.indexOf(product)
        if (index != -1) {
            _products[index] = product.copy(isBought = !product.isBought)
            saveProducts()
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveTheme(isDark)
            _isDarkTheme.value = isDark
        }
    }
}