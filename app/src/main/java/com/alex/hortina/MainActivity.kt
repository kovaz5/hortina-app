package com.alex.hortina

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.ui.navigation.HortinaNavGraph
import com.alex.hortina.ui.theme.HortinaAppTheme
import com.alex.hortina.utils.LocaleUtils
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.Locale
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.alex.hortina.notifications.DailyTaskWorker
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        RetrofitClient.init(this)

        setContent {

            HortinaAppTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    val target = intent.getStringExtra("navigateTo")
                    if (target == "tareas") {
                        navController.navigate("tareas")
                    }
                }

                val context = LocalContext.current
                val dataStore = remember { UserPreferencesDataStore(context) }

                val notificationsEnabled by dataStore.notificationsEnabledFlow.collectAsState(
                        initial = true
                    )

                LaunchedEffect(notificationsEnabled) {
                    if (notificationsEnabled) {
                        scheduleDailyWorker()
                    } else {
                        cancelDailyWorker()
                    }
                }

                HortinaNavGraph(navController = navController, startDestination = "splash")
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val dataStore = UserPreferencesDataStore(newBase!!)
        val lang = runBlocking { dataStore.getLanguage() ?: Locale.getDefault().language }
        val localizedContext = LocaleUtils.updateLocale(newBase, lang)
        super.attachBaseContext(localizedContext)
    }

    private fun scheduleDailyWorker() {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val request =
            PeriodicWorkRequestBuilder<DailyTaskWorker>(24, TimeUnit.HOURS).setInitialDelay(
                    delay,
                    TimeUnit.MILLISECONDS
                ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_task_worker", ExistingPeriodicWorkPolicy.REPLACE, request
        )
    }

    private fun cancelDailyWorker() {
        WorkManager.getInstance(this).cancelUniqueWork("daily_task_worker")
    }
}
