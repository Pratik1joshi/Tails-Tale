package com.example.tailstale.repo

import com.example.tailstale.model.ActivityRecord
import com.example.tailstale.model.ActivityType
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await

class ActivityRepositoryImpl : ActivityRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("activities")
    private val userActivitiesDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("userActivities")

    override suspend fun recordActivity(activity: ActivityRecord): Result<Boolean> {
        return try {
            println("DEBUG: Recording activity: ${activity.activityName} for pet: ${activity.petName}")

            // Save activity to main activities collection
            database.child(activity.id).setValue(activity).await()

            println("DEBUG: Activity saved successfully to Firebase")
            Result.success(true)
        } catch (e: Exception) {
            println("DEBUG: Failed to record activity: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getActivitiesByPetId(petId: String): Result<List<ActivityRecord>> {
        return try {
            println("DEBUG: Getting activities for petId: $petId")
            val snapshot = database.orderByChild("petId").equalTo(petId).get().await()
            val activities = mutableListOf<ActivityRecord>()

            println("DEBUG: Found ${snapshot.childrenCount} potential activities for pet $petId")

            snapshot.children.forEach { child ->
                try {
                    // Try to get as a map first to debug
                    val activityMap = child.getValue(Map::class.java) as? Map<String, Any>
                    println("DEBUG: Activity data: $activityMap")

                    // Then try to convert to ActivityRecord
                    val activity = child.getValue<ActivityRecord>()
                    if (activity != null) {
                        activities.add(activity)
                        println("DEBUG: Successfully parsed activity: ${activity.activityName}")
                    } else {
                        println("DEBUG: Failed to parse activity from child: ${child.key}")

                        // Try manual parsing as fallback
                        activityMap?.let { map ->
                            val manualActivity = parseActivityFromMap(map)
                            manualActivity?.let {
                                activities.add(it)
                                println("DEBUG: Successfully manually parsed activity: ${it.activityName}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("DEBUG: Error parsing individual activity: ${e.message}")
                    e.printStackTrace()
                }
            }

            // Sort by timestamp descending (most recent first)
            activities.sortByDescending { it.timestamp }
            println("DEBUG: Returning ${activities.size} activities for pet $petId")
            Result.success(activities)
        } catch (e: Exception) {
            println("DEBUG: Error in getActivitiesByPetId: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Helper function to manually parse activity from map
    private fun parseActivityFromMap(map: Map<String, Any>): ActivityRecord? {
        return try {
            val activityTypeString = map["activityType"] as? String ?: "FEEDING"
            val activityType = try {
                ActivityType.valueOf(activityTypeString)
            } catch (e: Exception) {
                ActivityType.FEEDING // fallback
            }

            ActivityRecord(
                id = map["id"] as? String ?: "",
                petId = map["petId"] as? String ?: "",
                petName = map["petName"] as? String ?: "",
                activityType = activityType,
                activityName = map["activityName"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                duration = (map["duration"] as? Number)?.toLong() ?: 0L,
                details = (map["details"] as? Map<String, Any>) ?: emptyMap(),
                statsChanged = (map["statsChanged"] as? Map<String, Any>)?.mapValues {
                    (it.value as? Number)?.toInt() ?: 0
                } ?: emptyMap(),
                videoPlayed = map["videoPlayed"] as? String,
                success = map["success"] as? Boolean ?: true
            )
        } catch (e: Exception) {
            println("DEBUG: Error in manual parsing: ${e.message}")
            null
        }
    }

    override suspend fun getActivitiesByUserId(userId: String): Result<List<ActivityRecord>> {
        return try {
            println("DEBUG: ActivityRepository - Getting activities for user: $userId")

            // First get all user's pets to know which pet IDs belong to this user
            val userPetsSnapshot = FirebaseDatabase.getInstance().reference
                .child("userPets").child(userId).get().await()

            val petIds = userPetsSnapshot.children.mapNotNull { it.key }
            println("DEBUG: ActivityRepository - Found pet IDs for user: $petIds")

            if (petIds.isEmpty()) {
                println("DEBUG: ActivityRepository - No pets found for user, returning empty list")
                return Result.success(emptyList())
            }

            val allActivities = mutableListOf<ActivityRecord>()

            // Get activities for each pet
            for (petId in petIds) {
                println("DEBUG: ActivityRepository - Getting activities for pet: $petId")
                val petActivities = getActivitiesByPetId(petId).getOrNull() ?: emptyList()
                println("DEBUG: ActivityRepository - Found ${petActivities.size} activities for pet: $petId")
                allActivities.addAll(petActivities)
            }

            // Also try a direct approach - get all activities and filter by pet IDs
            // This is a fallback in case the pet linking isn't working properly
            if (allActivities.isEmpty()) {
                println("DEBUG: ActivityRepository - No activities found via pet IDs, trying direct approach")

                val allActivitiesSnapshot = database.get().await()
                println("DEBUG: ActivityRepository - Total activities in database: ${allActivitiesSnapshot.childrenCount}")

                allActivitiesSnapshot.children.forEach { activitySnapshot ->
                    try {
                        val activity = activitySnapshot.getValue<ActivityRecord>()
                        activity?.let {
                            println("DEBUG: ActivityRepository - Found activity: ${it.activityName} for pet: ${it.petId}")
                            if (it.petId in petIds) {
                                allActivities.add(it)
                                println("DEBUG: ActivityRepository - Activity matches user's pet, added to list")
                            } else {
                                println("DEBUG: ActivityRepository - Activity pet ID ${it.petId} not in user's pets: $petIds")
                            }
                        }
                    } catch (e: Exception) {
                        println("DEBUG: ActivityRepository - Error parsing activity: ${e.message}")
                    }
                }
            }

            // Sort by timestamp descending
            allActivities.sortByDescending { it.timestamp }
            println("DEBUG: ActivityRepository - Returning ${allActivities.size} activities")
            Result.success(allActivities)
        } catch (e: Exception) {
            println("DEBUG: ActivityRepository - Error getting activities: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getRecentActivities(userId: String, limit: Int): Result<List<ActivityRecord>> {
        return try {
            val allActivities = getActivitiesByUserId(userId).getOrNull() ?: emptyList()
            val recentActivities = allActivities.take(limit)
            Result.success(recentActivities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActivitiesByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<ActivityRecord>> {
        return try {
            val allActivities = getActivitiesByUserId(userId).getOrNull() ?: emptyList()
            val filteredActivities = allActivities.filter { activity ->
                activity.timestamp in startDate..endDate
            }
            Result.success(filteredActivities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteActivity(activityId: String): Result<Boolean> {
        return try {
            database.child(activityId).removeValue().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActivityStats(petId: String, days: Int): Result<Map<String, Int>> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            val activities = getActivitiesByPetId(petId).getOrNull() ?: emptyList()

            val recentActivities = activities.filter { it.timestamp >= cutoffTime }
            val stats = mutableMapOf<String, Int>()

            // Count activities by type
            ActivityType.values().forEach { activityType ->
                val count = recentActivities.count { it.activityType == activityType }
                if (count > 0) {
                    stats[activityType.displayName] = count
                }
            }

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
