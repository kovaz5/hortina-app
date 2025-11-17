package com.alex.hortina.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alex.hortina.MainActivity
import com.alex.hortina.R
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.repository.TareaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import android.app.PendingIntent
import android.os.Build
import androidx.annotation.RequiresApi
import com.alex.hortina.utils.LocaleUtils

class DailyTaskWorker(
    context: Context, workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val context = applicationContext
            val dataStore = UserPreferencesDataStore(context)
            val repo = TareaRepository()

            val token = dataStore.getAccessToken()
            if (token.isNullOrEmpty()) return@withContext Result.success()

            val tareas = repo.getTareas()
            val hoy = LocalDate.now().toString()
            val tareasDeHoy = tareas.filter { it.fechaSugerida == hoy }

            if (tareasDeHoy.isEmpty()) return@withContext Result.success()

            val lang = dataStore.getLanguage() ?: "es"
            val localizedContext = LocaleUtils.updateLocale(context, lang)

            val titulo = localizedContext.getString(R.string.notis_title)

            val msgs = listOf(
                localizedContext.getString(R.string.notis_msg_1),
                localizedContext.getString(R.string.notis_msg_2),
                localizedContext.getString(R.string.notis_msg_3)
            )

            val msg = msgs.random()

            NotificationHelper.createChannel(context)

            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) return@withContext Result.success()

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigateTo", "tareas")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            NotificationHelper.show(context, titulo, msg, pendingIntent)

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
