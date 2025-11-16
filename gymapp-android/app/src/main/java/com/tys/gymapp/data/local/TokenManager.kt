package com.tys.gymapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenManager - Quản lý JWT token
 * Sử dụng DataStore để lưu token persistent
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }

    /**
     * Lưu token và thông tin user
     */
    suspend fun saveToken(token: String, userId: String, userName: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USER_NAME_KEY] = userName
            prefs[USER_ROLE_KEY] = role
        }
    }

    /**
     * Lấy token dưới dạng Flow (reactive)
     */
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    /**
     * Lấy token ngay lập tức (suspend function)
     */
    suspend fun getTokenSync(): String? {
        return context.dataStore.data.map { it[TOKEN_KEY] }.firstOrNull()
    }

    /**
     * Lấy User ID
     */
    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_ID_KEY]
        }
    }

    /**
     * Lấy User Name
     */
    fun getUserName(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_NAME_KEY]
        }
    }

    /**
     * Lấy User Role
     */
    fun getUserRole(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_ROLE_KEY]
        }
    }

    /**
     * Xóa tất cả dữ liệu (logout)
     */
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    /**
     * Kiểm tra đã login chưa
     */
    suspend fun isLoggedIn(): Boolean {
        return getTokenSync() != null
    }
}