package com.example.semester_project_cis_357_task_notification

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
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
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
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
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (validateInput(name, email, password, context)) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser!!.uid
                                val user = hashMapOf(
                                    "userId" to userId,
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
                                        Toast.makeText(context, "Failed to save user data: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Failed to create account: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

private fun validateInput(name: String, email: String, password: String, context: android.content.Context): Boolean {
    return when {
        name.isBlank() -> {
            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            false
        }
        email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
            Toast.makeText(context, "Invalid email address", Toast.LENGTH_SHORT).show()
            false
        }
        password.length < 6 -> {
            Toast.makeText(context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            false
        }
        else -> true
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
