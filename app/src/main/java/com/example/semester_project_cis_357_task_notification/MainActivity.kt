package com.example.semester_project_cis_357_task_notification

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Check if the user is logged in
        if (auth.currentUser != null) {
            // Redirect to TaskListActivity
            val intent = Intent(this, TaskListActivity::class.java)
            startActivity(intent)
        } else {
            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Finish MainActivity to remove it from the backstack
        finish()
    }
}
