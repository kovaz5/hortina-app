package com.alex.hortina.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alex.hortina.R

object NotificationHelper {

    const val CHANNEL_ID = "hortina_daily_tasks"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, "Tareas de hoy", NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun show(
        context: Context, title: String, message: String, pendingIntent: PendingIntent? = null
    ) {
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_hortina)
                .setContentTitle(title).setContentText(message).setAutoCancel(true)

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        try {
            NotificationManagerCompat.from(context).notify(1001, builder.build())
        } catch (_: SecurityException) {
        }
    }
}
