package com.example.semester_project_cis_357_task_notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskNotificationService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "TaskNotificationService"
        private const val CHANNEL_ID = "task_notifications"
        private const val CHANNEL_NAME = "Task Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for task updates and reminders"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Handle notification payload
        remoteMessage.notification?.let {
            val title = it.title ?: "Task Notification"
            val body = it.body ?: "You have a new task update."
            sendNotification(title, body, null)
        }

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener { Log.d(TAG, "FCM token updated successfully.") }
                .addOnFailureListener { e -> Log.e(TAG, "Error updating FCM token.", e) }
        }
    }

    private fun sendNotification(title: String, body: String, taskId: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Notification permission not granted.")
            return
        }

        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            taskId?.let { putExtra("taskId", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = CHANNEL_DESCRIPTION }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val taskId = data["taskId"]
        val taskTitle = data["taskTitle"]
        val dueDate = data["dueDate"]

        if (taskId.isNullOrEmpty() || taskTitle.isNullOrEmpty() || dueDate.isNullOrEmpty()) {
            Log.d(TAG, "Data payload incomplete.")
            return
        }

        Log.d(TAG, "Task ID: $taskId, Title: $taskTitle, Due Date: $dueDate")
        sendNotification("Task Update", "Task '$taskTitle' is due on $dueDate.", taskId)
    }
}
