package com.example.semester_project_cis_357_task_notification.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Data class to represent a User
data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = ""
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

    // Add a new task to Firestore
    suspend fun addTask(task: Task): Boolean {
        return try {
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
            taskCollection.document(taskId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Delete all tasks for a specific user
    suspend fun deleteAllTasksForUser(userId: String): Boolean {
        return try {
            val tasksForUser = getTasksForUser(userId)
            for (task in tasksForUser) {
                taskCollection.document(task.id).delete().await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
