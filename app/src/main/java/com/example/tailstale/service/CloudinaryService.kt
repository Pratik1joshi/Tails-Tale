package com.example.tailstale.service

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryService {
    companion object {
        private var isInitialized = false

        fun initialize(context: Context) {
            if (!isInitialized) {
                val config = mapOf(
                    "cloud_name" to "drlbmawxc", // Replace with your Cloudinary cloud name
                    "api_key" to "282215947279546",       // Replace with your API key
                    "api_secret" to "KHa9E__UDrpSfqc3XWxx8-DZyUE"  // Replace with your API secret
                )
                MediaManager.init(context, config)
                isInitialized = true
            }
        }
    }

    suspend fun uploadImage(imageUri: Uri, folder: String = "profile_images"): String {
        return suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(imageUri)
                .option("folder", folder)
                .option("resource_type", "image")
                .option("transformation", "w_400,h_400,c_fill,q_auto")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        // Upload started
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // Progress update
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        if (secureUrl != null) {
                            continuation.resume(secureUrl)
                        } else {
                            continuation.resumeWithException(Exception("Failed to get image URL"))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(Exception(error.description))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(Exception("Upload rescheduled: ${error.description}"))
                    }
                })
                .dispatch()
        }
    }

    fun getOptimizedImageUrl(
        publicId: String,
        width: Int = 400,
        height: Int = 400,
        quality: String = "auto"
    ): String {
        // Build the URL manually using Cloudinary's URL structure
        val cloudName = "drlbmawxc"
        val transformationString = "w_${width},h_${height},c_fill,q_${quality}"
        return "https://res.cloudinary.com/${cloudName}/image/upload/${transformationString}/${publicId}"
    }
}
