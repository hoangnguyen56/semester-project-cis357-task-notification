package com.example.semester_project_cis_357_task_notification

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.semester_project_cis_357_task_notification.data.FirestoreRepository
import com.example.semester_project_cis_357_task_notification.data.Task
import com.example.semester_project_cis_357_task_notification.ui.theme.SemesterProjectCIS357TaskNotificationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskListActivity : ComponentActivity() {

    private val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    private val taskListState = mutableStateOf<List<Task>>(emptyList())
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null
    private var taskListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            logout()
            return
        }

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

        // Update FCM token for the logged-in user
        updateFcmToken()
    }

    private fun updateFcmToken() {
        val userId = currentUserId ?: return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                if (!fcmToken.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        firestoreRepository.updateFcmToken(userId, fcmToken)
                    }
                }
            }
        }
    }

    private fun navigateToTaskDetails(taskId: String) {
        val intent = Intent(this, TaskDetailsActivity::class.java).apply {
            putExtra("TASK_ID", taskId)
        }
        startActivity(intent)
    }

    private fun saveTask(title: String, description: String, dueDate: String, context: android.content.Context) {
        val userId = currentUserId ?: return
        val task = Task(title = title, description = description, dueDate = dueDate, userId = userId)

        CoroutineScope(Dispatchers.IO).launch {
            val success = firestoreRepository.addTask(task)
            if (success) {
                updateFcmToken()
            }

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    if (success) "Task saved successfully" else "Failed to save task",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteTask(taskId: String, context: android.content.Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = firestoreRepository.deleteTask(taskId)
            if (success) {
                updateFcmToken()
            }

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    if (success) "Task deleted successfully" else "Failed to delete task",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        currentUserId?.let { userId ->
            taskListener = firestoreRepository.addTaskListener(userId) { tasks ->
                taskListState.value = tasks
            }
        }
    }

    override fun onStop() {
        super.onStop()
        taskListener?.remove()
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
        Button(
            onClick = { onLogout() },
            modifier = Modifier.fillMaxWidth()
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
            label = { Text("Due Date (YYYY-MM-DD)") },
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
                        "All fields are required",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
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
            onLogout = {}
        )
    }
}
