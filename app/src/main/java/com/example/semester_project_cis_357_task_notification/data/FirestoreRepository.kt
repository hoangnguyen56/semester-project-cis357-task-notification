package com.example.semester_project_cis_357_task_notification.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Data class to represent a User
data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val fcmToken: String? = null // Optional FCM token for notifications
)

// Data class for Task, associated with a User
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val userId: String = "" // Links task to a specific user
)

class FirestoreRepository(private val db: FirebaseFirestore) {

    private val taskCollection = db.collection("tasks")
    private val userCollection = db.collection("users")

    // Add a new task to Firestore
    suspend fun addTask(task: Task): Boolean {
        return try {
            if (task.userId.isBlank() || task.title.isBlank()) {
                throw IllegalArgumentException("Task must have a valid userId and title.")
            }
            val docRef = taskCollection.document()
            taskCollection.document(docRef.id).set(task.copy(id = docRef.id)).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Retrieve tasks for a specific user
    suspend fun getTasksForUser(userId: String): List<Task> {
        return try {
            if (userId.isBlank()) throw IllegalArgumentException("User ID cannot be blank.")
            val snapshot = taskCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Update an existing task
    suspend fun updateTask(task: Task): Boolean {
        return try {
            if (task.id.isBlank()) throw IllegalArgumentException("Task ID cannot be blank.")
            taskCollection.document(task.id).set(task).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Delete a task by its ID
    suspend fun deleteTask(taskId: String): Boolean {
        return try {
            if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be blank.")
            taskCollection.document(taskId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Save or update a user's FCM token
    suspend fun updateFcmToken(userId: String, token: String): Boolean {
        return try {
            userCollection.document(userId)
                .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
