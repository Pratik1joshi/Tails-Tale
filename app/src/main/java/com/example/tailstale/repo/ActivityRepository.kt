package com.example.tailstale.repo

import com.example.tailstale.model.ActivityRecord

interface ActivityRepository {
    suspend fun recordActivity(activity: ActivityRecord): Result<Boolean>
    suspend fun getActivitiesByPetId(petId: String): Result<List<ActivityRecord>>
    suspend fun getActivitiesByUserId(userId: String): Result<List<ActivityRecord>>
    suspend fun getRecentActivities(userId: String, limit: Int = 50): Result<List<ActivityRecord>>
    suspend fun getActivitiesByDateRange(userId: String, startDate: Long, endDate: Long): Result<List<ActivityRecord>>
    suspend fun deleteActivity(activityId: String): Result<Boolean>
    suspend fun getActivityStats(petId: String, days: Int = 7): Result<Map<String, Int>>
}
