package com.example.tailstale.repo

import com.example.tailstale.model.FoodModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FoodRepositoryImpl(private val db: FirebaseFirestore) : FoodRepository {
    override suspend fun getFoodById(foodId: String): FoodModel? {
        val snapshot = db.collection("food").document(foodId).get().await()
        return snapshot.toObject(FoodModel::class.java)
    }

    override suspend fun updateFood(food: FoodModel) {
        db.collection("food").document(food.id).set(food).await()
    }

    override suspend fun createFood(food: FoodModel) {
        db.collection("food").document(food.id).set(food).await()
    }

    override suspend fun getAllFood(): List<FoodModel> {
        val snapshot = db.collection("food").get().await()
        return snapshot.toObjects(FoodModel::class.java)
    }
}