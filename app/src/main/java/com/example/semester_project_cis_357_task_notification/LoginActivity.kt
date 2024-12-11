package com.example.semester_project_cis_357_task_notification

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Check if the user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateFcmTokenForLoggedInUser(currentUser.uid)
            navigateToTaskList() // Skip login screen if already logged in
        } else {
            setContent {
                SemesterProjectCIS357TaskNotificationTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LoginScreen(
                            auth = auth,
                            onLoginSuccess = { handleSuccessfulLogin() },
                            onRegister = { navigateToRegister() }
                        )
                    }
                }
            }
        }
    }

    /**
     * Called after successful login to update FCM token and navigate to task list.
     */
    private fun handleSuccessfulLogin() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            updateFcmTokenForLoggedInUser(userId)
        }
        navigateToTaskList()
    }

    /**
     * Updates the FCM token for the currently logged-in user.
     */
    private fun updateFcmTokenForLoggedInUser(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("LoginActivity", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            val fcmToken = task.result
            CoroutineScope(Dispatchers.IO).launch {
                FirestoreRepository(FirebaseFirestore.getInstance())
                    .updateFcmToken(userId, fcmToken ?: "")
            }
        }
    }

    private fun navigateToTaskList() {
        val intent = Intent(this, TaskListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, NewAccountActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth?, onLoginSuccess: () -> Unit, onRegister: () -> Unit) {
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
            text = "Login",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

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
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth?.let {
                        loginUser(it, email, password, onLoginSuccess, context)
                    } ?: Toast.makeText(context, "Auth not available", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SemesterProjectCIS357TaskNotificationTheme {
        LoginScreen(auth = null, onLoginSuccess = {}, onRegister = {})
    }
}

/**
 * Handles the user login process using Firebase Authentication.
 */
private fun loginUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onLoginSuccess: () -> Unit,
    context: android.content.Context
) {
    if (email.isBlank() || password.isBlank()) {
        Toast.makeText(context, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
        return
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            } else {
                val message = task.exception?.localizedMessage ?: "Login failed"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
}
