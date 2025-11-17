package com.alex.hortina.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.alex.hortina.R

@Composable
fun BottomBar(
    navController: NavController, barHeight: Dp = 72.dp
) {
    val currentRoute = currentRoute(navController as NavHostController)

    val onFabClick = { navController.navigate("cultivo_form") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(barHeight), contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .height(56.dp)
                .shadow(
                    elevation = 8.dp, shape = RoundedCornerShape(20.dp), clip = false
                )
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .zIndex(0f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .height(barHeight),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavIconButton(
                navController = navController,
                item = bottomNavItems[0],
                selected = currentRoute == bottomNavItems[0].route
            )

            BottomNavIconButton(
                navController = navController,
                item = bottomNavItems[1],
                selected = currentRoute == bottomNavItems[1].route
            )

            Spacer(modifier = Modifier.width(6.dp))

            BottomNavIconButton(
                navController = navController,
                item = bottomNavItems[2],
                selected = currentRoute == bottomNavItems[2].route
            )

            BottomNavIconButton(
                navController = navController,
                item = bottomNavItems[3],
                selected = currentRoute == bottomNavItems[3].route
            )
        }

        FloatingActionButton(
            onClick = onFabClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
                .size(58.dp)
                .zIndex(2f)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
        }
    }
}


@Composable
private fun BottomNavIconButton(
    navController: NavController, item: BottomNavItem, selected: Boolean
) {
    val targetScale = if (selected) 1.08f else 1.0f
    val scale by animateFloatAsState(targetValue = targetScale)

    val iconTint =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = iconTint

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .width(64.dp)
            .height(56.dp)
            .clickable(
                interactionSource = interactionSource, indication = null
            ) {
                if (navController.currentBackStackEntry?.destination?.route != item.route) {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.title),
            tint = iconTint,
            modifier = Modifier
                .size(if (selected) 28.dp else 22.dp)
                .scale(scale)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(item.title),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
