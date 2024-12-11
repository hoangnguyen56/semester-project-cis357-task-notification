
package com.example.semester_project_cis_357_task_notification

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.semester_project_cis_357_task_notification.ui.theme.SemesterProjectCIS357TaskNotificationTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Set splash screen content
        setContent {
            SemesterProjectCIS357TaskNotificationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SplashScreen()
                }
            }
        }

        // Redirect after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (auth.currentUser != null) {
                // Redirect to TaskListActivity if the user is logged in
                val intent = Intent(this, TaskListActivity::class.java)
                startActivity(intent)
            } else {
                // Redirect to LoginActivity if no user is logged in
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            // Finish MainActivity to remove it from the backstack
            finish()
        }, SPLASH_SCREEN_DURATION)
    }

    companion object {
        private const val SPLASH_SCREEN_DURATION = 2000L // 2 seconds
    }
}

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Task Notification App",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Managing tasks made simple!",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SemesterProjectCIS357TaskNotificationTheme {
        SplashScreen()
    }
}
