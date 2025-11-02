package com.alex.hortina.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("dashboard", "Inicio", Icons.Filled.Home),
    BottomNavItem("cultivos", "Cultivos", Icons.Filled.Favorite),
    BottomNavItem("tareas", "Tareas", Icons.Filled.Build),
    BottomNavItem("perfil", "Perfil", Icons.Filled.Person)
)
