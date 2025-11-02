package com.alex.hortina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HortinaApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HortinaApp() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("🌱 Hortina") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Compose funcionando correctamente ✅",
                fontSize = 18.sp
            )
        }
    }
}
