package com.example.semester_project_cis_357_task_notification.notifications

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.semester_project_cis_357_task_notification.R

class TaskNotificationService(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun sendTaskCreatedNotification(title: String, description: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Task Created: $title")
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        private const val CHANNEL_ID = "task_notification_channel"
        private const val NOTIFICATION_ID = 1
    }
}
