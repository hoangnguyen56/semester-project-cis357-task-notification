package com.example.semester_project_cis_357_task_notification

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.semester_project_cis_357_task_notification.data.FirestoreRepository
import com.example.semester_project_cis_357_task_notification.ui.theme.SemesterProjectCIS357TaskNotificationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskDetailsActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskId = intent.getStringExtra("TASK_ID") ?: ""
        if (auth.currentUser == null || taskId.isBlank()) {
            Toast.makeText(this, "Invalid task or user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            SemesterProjectCIS357TaskNotificationTheme {
                TaskDetailsScreen(
                    taskId = taskId,
                    fetchTask = { onTaskLoaded -> startRealTimeTaskListener(taskId, onTaskLoaded) },
                    saveTask = { title, description, dueDate -> updateTask(taskId, title, description, dueDate) },
                    deleteTask = { deleteTask(taskId) },
                    onBackPressed = { finish() }
                )
            }
        }
    }

    private fun startRealTimeTaskListener(taskId: String, onTaskLoaded: (String, String, String) -> Unit) {
        db.collection("tasks")
            .document(taskId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Failed to listen for task updates", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val title = snapshot.getString("title") ?: ""
                    val description = snapshot.getString("description") ?: ""
                    val dueDate = snapshot.getString("dueDate") ?: ""
                    onTaskLoaded(title, description, dueDate)
                } else {
                    Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateTask(taskId: String, title: String, description: String, dueDate: String) {
        if (title.isBlank() || description.isBlank() || dueDate.isBlank()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedTask = mapOf(
            "title" to title,
            "description" to description,
            "dueDate" to dueDate
        )

        CoroutineScope(Dispatchers.IO).launch {
            db.collection("tasks")
                .document(taskId)
                .update(updatedTask)
                .addOnSuccessListener {
                    Toast.makeText(this@TaskDetailsActivity, "Task updated successfully", Toast.LENGTH_SHORT).show()
                    updateFcmToken()
                }
                .addOnFailureListener {
                    Toast.makeText(this@TaskDetailsActivity, "Failed to update task", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteTask(taskId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this@TaskDetailsActivity, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                    updateFcmToken()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@TaskDetailsActivity, "Failed to delete task", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                auth.currentUser?.uid?.let { userId ->
                    CoroutineScope(Dispatchers.IO).launch {
                        FirestoreRepository(FirebaseFirestore.getInstance()).updateFcmToken(userId, fcmToken ?: "")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: String,
    fetchTask: (onTaskLoaded: (String, String, String) -> Unit) -> Unit,
    saveTask: (String, String, String) -> Unit,
    deleteTask: () -> Unit,
    onBackPressed: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(taskId) {
        fetchTask { fetchedTitle, fetchedDescription, fetchedDueDate ->
            title = fetchedTitle
            description = fetchedDescription
            dueDate = fetchedDueDate
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Task Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (e.g., 2024-12-01)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { saveTask(title, description, dueDate) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Task")
                    }
                    Button(
                        onClick = { deleteTask() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Task")
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDetailsPreview() {
    SemesterProjectCIS357TaskNotificationTheme {
        TaskDetailsScreen(
            taskId = "sampleTaskId",
            fetchTask = { onTaskLoaded -> onTaskLoaded("Sample Title", "Sample Description", "2024-12-01") },
            saveTask = { _, _, _ -> },
            deleteTask = {},
            onBackPressed = {}
        )
    }
}
