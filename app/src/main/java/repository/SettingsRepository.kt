package com.example.shoppinglist.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.shoppinglist.data.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        private val PRODUCTS_LIST_KEY = stringPreferencesKey("products_list")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_THEME_KEY] ?: false
        }

    suspend fun saveTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDark
        }
    }

    // Сохраняем список продуктов
    suspend fun saveProducts(products: List<Product>) {
        val json = Json.encodeToString(products)
        context.dataStore.edit { preferences ->
            preferences[PRODUCTS_LIST_KEY] = json
        }
    }

    // Загружаем список продуктов
    val products: Flow<List<Product>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[PRODUCTS_LIST_KEY] ?: return@map emptyList()
            try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
}