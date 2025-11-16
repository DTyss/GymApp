package com.tys.gymapp.presentation.utils

import com.google.gson.Gson
import com.tys.gymapp.data.remote.dto.Branch
import com.tys.gymapp.data.remote.dto.ClassItem
import com.tys.gymapp.data.remote.dto.Plan

object NavigationUtils {
    private val gson = Gson()

    fun planToJson(plan: Plan): String {
        return gson.toJson(plan)
    }

    fun jsonToPlan(json: String): Plan? {
        return try {
            gson.fromJson(json, Plan::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun branchToJson(branch: Branch): String {
        return gson.toJson(branch)
    }

    fun jsonToBranch(json: String): Branch? {
        return try {
            gson.fromJson(json, Branch::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun classToJson(classItem: ClassItem): String {
        return gson.toJson(classItem)
    }

    fun jsonToClass(json: String): ClassItem? {
        return try {
            gson.fromJson(json, ClassItem::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

