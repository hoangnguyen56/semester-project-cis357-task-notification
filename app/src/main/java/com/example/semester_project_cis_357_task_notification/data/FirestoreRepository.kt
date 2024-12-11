package com.example.semester_project_cis_357_task_notification.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
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
    val dueDate: String = "", // ISO 8601 date string for consistency
    val userId: String = "", // Links task to a specific user
    val fcmToken: String? = null // Optional fcmToken field for task
)

class FirestoreRepository(private val db: FirebaseFirestore) {

    private val taskCollection = db.collection("tasks")
    private val userCollection = db.collection("users")

    /**
     * Adds a new task to Firestore.
     * Returns true if the task is added successfully, false otherwise.
     */
    suspend fun addTask(task: Task): Boolean {
        return try {
            if (task.userId.isBlank() || task.title.isBlank()) {
                throw IllegalArgumentException("Task must have a valid userId and title.")
            }

            // Get the FCM token for the user, if available
            val fcmToken = getFcmTokenForUser(task.userId)

            // If the token exists, include it in the task
            val taskWithToken = task.copy(fcmToken = fcmToken)

            val docRef = taskCollection.document()
            taskCollection.document(docRef.id).set(taskWithToken.copy(id = docRef.id)).await()

            true
        } catch (e: Exception) {
            logError("Failed to add task", e)
            false
        }
    }

    /**
     * Retrieves all tasks associated with a specific user.
     * Returns an empty list if no tasks are found or if an error occurs.
     */
    suspend fun getTasksForUser(userId: String): List<Task> {
        return try {
            if (userId.isBlank()) throw IllegalArgumentException("User ID cannot be blank.")
            val snapshot = taskCollection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
        } catch (e: Exception) {
            logError("Failed to retrieve tasks for user: $userId", e)
            emptyList()
        }
    }

    /**
     * Updates an existing task in Firestore.
     * Returns true if the task is updated successfully, false otherwise.
     */
    suspend fun updateTask(task: Task): Boolean {
        return try {
            if (task.id.isBlank()) throw IllegalArgumentException("Task ID cannot be blank.")
            taskCollection.document(task.id).set(task).await()
            true
        } catch (e: Exception) {
            logError("Failed to update task: ${task.id}", e)
            false
        }
    }

    /**
     * Deletes a task by its ID.
     * Returns true if the task is deleted successfully, false otherwise.
     */
    suspend fun deleteTask(taskId: String): Boolean {
        return try {
            if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be blank.")
            taskCollection.document(taskId).delete().await()
            true
        } catch (e: Exception) {
            logError("Failed to delete task: $taskId", e)
            false
        }
    }

    /**
     * Saves or updates a user's FCM token in Firestore.
     * Returns true if the FCM token is updated successfully, false otherwise.
     */
    suspend fun updateFcmToken(userId: String, token: String): Boolean {
        return try {
            if (userId.isBlank() || token.isBlank()) {
                throw IllegalArgumentException("User ID and FCM token must not be blank.")
            }
            userCollection.document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge()).await()
            Log.d("FirestoreRepository", "FCM token updated for userId: $userId")
            true
        } catch (e: Exception) {
            logError("Failed to update FCM token for user: $userId", e)
            false
        }
    }

    /**
     * Retrieves the FCM token for a specific user.
     * Returns the FCM token if available, or null if not found or an error occurs.
     */
    suspend fun getFcmTokenForUser(userId: String): String? {
        return try {
            val snapshot = userCollection.document(userId).get().await()
            snapshot.getString("fcmToken")
        } catch (e: Exception) {
            logError("Failed to retrieve FCM token for user: $userId", e)
            null
        }
    }

    /**
     * Adds a real-time listener for tasks associated with a specific user.
     * Executes the callback whenever a change occurs.
     */
    fun addTaskListener(userId: String, onTaskChange: (List<Task>) -> Unit): ListenerRegistration {
        return taskCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    logError("Task listener error for user: $userId", e)
                    onTaskChange(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) } ?: emptyList()
                onTaskChange(tasks)
            }
    }

    /**
     * Adds a real-time listener for FCM token changes of a specific user.
     * Executes the callback whenever the FCM token changes.
     */
    fun addFcmTokenListener(userId: String, onTokenChange: (String?) -> Unit): ListenerRegistration {
        return userCollection.document(userId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                logError("FCM token listener error for user: $userId", e)
                onTokenChange(null)
                return@addSnapshotListener
            }
            val fcmToken = snapshot?.getString("fcmToken")
            onTokenChange(fcmToken)
        }
    }

    /**
     * Removes a real-time listener.
     */
    fun removeListener(listenerRegistration: ListenerRegistration) {
        listenerRegistration.remove()
    }

    /**
     * Logs errors consistently.
     */
    private fun logError(message: String, exception: Exception) {
        Log.e("FirestoreRepository", "$message: ${exception.message}", exception)
    }
}
