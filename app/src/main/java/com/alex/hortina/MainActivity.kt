package com.alex.hortina

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HortinaApp()
        }
        // dentro de MainActivity.onCreate
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instance.create(HortinaApiService::class.java)
                val list = api.getCultivos()
                Log.d("Hortina", "Recibidos ${list.size} cultivos, primer nombre: ${list.firstOrNull()?.nombre}")
            } catch (e: Exception) {
                Log.e("Hortina", "Error al llamar API", e)
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HortinaApp() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Proba Hortiña App") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ola mundo!",
                fontSize = 18.sp
            )
        }
    }
}
