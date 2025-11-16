package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: GymApiService
) {
    suspend fun getUsers(role: String? = null, status: String? = null): Resource<List<User>>
    suspend fun getUserById(id: String): Resource<User>
    suspend fun updateUser(id: String, data: UpdateUserDto): Resource<User>
    suspend fun updateStatus(id: String, status: String): Resource<User>
}