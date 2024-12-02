package com.example.semester_project_cis_357_task_notification

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.semester_project_cis_357_task_notification.ui.theme.SemesterProjectCIS357TaskNotificationTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NewAccountActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        setContent {
            SemesterProjectCIS357TaskNotificationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NewAccountScreen(
                        auth = auth,
                        db = db,
                        onAccountCreated = { navigateToTaskList() },
                        onCancel = { navigateToLogin() }
                    )
                }
            }
        }
    }

    private fun navigateToTaskList() {
        val intent = Intent(this, TaskListActivity::class.java)
        startActivity(intent)
        finish() // Close NewAccountActivity
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close NewAccountActivity
    }
}

@Composable
fun NewAccountScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onAccountCreated: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Name Input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Create Account Button
        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && password.length >= 6) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser!!.uid
                                val user = hashMapOf(
                                    "realName" to name,
                                    "email" to email,
                                    "createdAt" to Timestamp.now()
                                )
                                db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                                        onAccountCreated()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Failed to create account", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        context,
                        "All fields must be filled and password must be at least 6 characters long",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Cancel Button
        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewAccountScreenPreview() {
    SemesterProjectCIS357TaskNotificationTheme {
        NewAccountScreen(
            auth = FirebaseAuth.getInstance(),
            db = FirebaseFirestore.getInstance(),
            onAccountCreated = {},
            onCancel = {}
        )
    }
}
