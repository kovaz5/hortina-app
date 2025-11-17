package com.alex.hortina.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.alex.hortina.R

data class BottomNavItem(
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("dashboard", R.string.home, Icons.Filled.Home),
    BottomNavItem("cultivos", R.string.crops, Icons.Filled.Favorite),
    BottomNavItem("tareas", R.string.tasks, Icons.Filled.Build),
    BottomNavItem("perfil", R.string.account, Icons.Filled.Person)
)
