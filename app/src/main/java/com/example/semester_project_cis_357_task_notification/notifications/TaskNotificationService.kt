package com.example.semester_project_cis_357_task_notification.notifications

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
import com.example.semester_project_cis_357_task_notification.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

        // Keys for FCM data payload
        private const val KEY_TASK_ID = "taskId"
        private const val KEY_TASK_TITLE = "taskTitle"
        private const val KEY_DUE_DATE = "dueDate"
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
        Log.d(TAG, "New FCM token generated: $token")
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Log.w(TAG, "User not logged in. Skipping FCM token update.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(mapOf("fcmToken" to token), SetOptions.merge())
                Log.d(TAG, "FCM token updated successfully for userId: $userId.")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating FCM token in Firestore for userId: $userId", e)
            }
        }
    }

    private fun sendNotification(title: String, body: String, taskId: String?) {
        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Notification permission not granted. Notification skipped.")
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
        Log.d(TAG, "Notification sent: Title=$title, Body=$body, TaskId=$taskId")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val existingChannel = manager?.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = CHANNEL_DESCRIPTION }
                manager?.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created: $CHANNEL_NAME")
            }
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val taskId = data[KEY_TASK_ID]
        val taskTitle = data[KEY_TASK_TITLE]
        val dueDate = data[KEY_DUE_DATE]

        if (taskId.isNullOrEmpty() || taskTitle.isNullOrEmpty() || dueDate.isNullOrEmpty()) {
            Log.w(TAG, "Incomplete data payload: $data")
            sendNotification("Task Update", "A task update is available, but details are missing.", null)
            return
        }

        Log.d(TAG, "Processed taskId: $taskId, Title: $taskTitle, Due Date: $dueDate")
        sendNotification("Task Update", "Task '$taskTitle' is due on $dueDate.", taskId)
    }
}
