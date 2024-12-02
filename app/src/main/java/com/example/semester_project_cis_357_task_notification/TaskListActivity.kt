package com.example.semester_project_cis_357_task_notification

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.semester_project_cis_357_task_notification.ui.theme.SemesterProjectCIS357TaskNotificationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = ""
)

class TaskListActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var taskListener: ListenerRegistration
    private val taskListState = mutableStateOf<List<Task>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SemesterProjectCIS357TaskNotificationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TaskListScreen(
                        tasks = taskListState.value,
                        modifier = Modifier.padding(innerPadding),
                        onTaskClick = { taskId -> navigateToTaskDetails(taskId) },
                        onSaveTask = { title, description, dueDate, context ->
                            saveTask(title, description, dueDate, context)
                        },
                        onDeleteTask = { taskId, context ->
                            deleteTask(taskId, context)
                        },
                        onLogout = { logout() }
                    )
                }
            }
        }
    }

    private fun navigateToTaskDetails(taskId: String) {
        val intent = Intent(this, TaskDetailsActivity::class.java)
        intent.putExtra("TASK_ID", taskId)
        startActivity(intent)
    }

    private fun saveTask(
        title: String,
        description: String,
        dueDate: String,
        context: android.content.Context
    ) {
        if (title.isBlank() || description.isBlank() || dueDate.isBlank()) {
            Toast.makeText(context, "Title, description, and due date cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val task = hashMapOf(
            "title" to title,
            "description" to description,
            "dueDate" to dueDate
        )

        db.collection("tasks").add(task)
            .addOnSuccessListener {
                Toast.makeText(context, "Task saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save task", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTask(taskId: String, context: android.content.Context) {
        db.collection("tasks").document(taskId).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        taskListener = db.collection("tasks").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Toast.makeText(this, "Failed to listen for tasks", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            snapshots?.let {
                val tasks = it.documents.map { document ->
                    Task(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: "",
                        dueDate = document.getString("dueDate") ?: ""
                    )
                }
                taskListState.value = tasks
            }
        }
    }

    override fun onStop() {
        super.onStop()
        taskListener.remove()
    }
}

@Composable
fun TaskListScreen(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    onTaskClick: (String) -> Unit,
    onSaveTask: (String, String, String, android.content.Context) -> Unit,
    onDeleteTask: (String, android.content.Context) -> Unit,
    onLogout: () -> Unit
) {
    var taskTitle by remember { mutableStateOf(TextFieldValue("")) }
    var taskDescription by remember { mutableStateOf(TextFieldValue("")) }
    var taskDueDate by remember { mutableStateOf(TextFieldValue("")) }

    val context = LocalContext.current

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Logout Button
        Button(
            onClick = { onLogout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Logout")
        }

        OutlinedTextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            label = { Text("Task Description") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = taskDueDate,
            onValueChange = { taskDueDate = it },
            label = { Text("Due Date (e.g., 2024-12-01)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (taskTitle.text.isNotEmpty() && taskDescription.text.isNotEmpty() && taskDueDate.text.isNotEmpty()) {
                    onSaveTask(taskTitle.text, taskDescription.text, taskDueDate.text, context)
                    taskTitle = TextFieldValue("")
                    taskDescription = TextFieldValue("")
                    taskDueDate = TextFieldValue("")
                } else {
                    Toast.makeText(
                        context,
                        "Title, description, and due date cannot be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Task List", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                TaskListItem(
                    task = task,
                    onTaskClick = { onTaskClick(task.id) },
                    onDeleteTask = { onDeleteTask(task.id, context) }
                )
            }
        }
    }
}

@Composable
fun TaskListItem(
    task: Task,
    onTaskClick: () -> Unit,
    onDeleteTask: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onTaskClick() },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDeleteTask) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskListPreview() {
    SemesterProjectCIS357TaskNotificationTheme {
        TaskListScreen(
            tasks = listOf(
                Task(id = "1", title = "Task 1", description = "Description 1", dueDate = "2024-12-01"),
                Task(id = "2", title = "Task 2", description = "Description 2", dueDate = "2024-12-02")
            ),
            onTaskClick = {},
            onSaveTask = { _, _, _, _ -> },
            onDeleteTask = { _, _ -> },
            onLogout = { /* Handle Logout */ }
        )
    }
}