package com.example.semester_project_cis_357_task_notification.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = ""
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

    // Retrieve all tasks
    suspend fun getTasks(): List<Task> {
        return try {
            val snapshot = taskCollection.get().await()
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
}
