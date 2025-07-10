package com.example.appranzo.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TokensRepository(private val dataStore: DataStore<Preferences>) {
    companion object{
        private val ACCESS_TOKEN = stringPreferencesKey("accessToken")
        private val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
    }

    val accessToken = dataStore.data.map{
        preferences-> try{
            preferences[ACCESS_TOKEN] ?: ""
        } catch (e:Exception){
                ""
            }
    }

    val refreshToken = dataStore.data.map{
            preferences-> try{
        preferences[REFRESH_TOKEN] ?: ""
    } catch (e:Exception){
        ""
    }
    }

    suspend fun changeTokens(accessToken:String, refreshToken:String){
        dataStore.edit { it[ACCESS_TOKEN] = accessToken }
        dataStore.edit { it[REFRESH_TOKEN] = refreshToken }
    }

    suspend fun hasTokens(): Boolean {
        return try {
            val preferences = dataStore.data.first()
            val currentAccessToken = preferences[ACCESS_TOKEN]
            val currentRefreshToken = preferences[REFRESH_TOKEN]
            currentAccessToken !=null && currentRefreshToken!=null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }


}