package com.alex.hortina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alex.hortina.ui.navigation.HortinaNavGraph
import com.alex.hortina.ui.theme.HortinaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HortinaAppTheme {
                HortinaNavGraph()
            }
        }
    }
}
