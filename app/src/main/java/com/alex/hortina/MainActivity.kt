package com.alex.hortina

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)
        setContent {
            HortinaAppTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val dataStore = remember { UserPreferencesDataStore(context) }
                val user by dataStore.user.collectAsState(initial = null)
                val startDestination = "splash"
                HortinaNavGraph(navController = navController, startDestination = startDestination)
            }

        }
    }
}
