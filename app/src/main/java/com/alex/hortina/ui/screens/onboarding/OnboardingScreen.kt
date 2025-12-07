package com.alex.hortina.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alex.hortina.R
import com.alex.hortina.data.local.UserPreferencesDataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController, dataStore: UserPreferencesDataStore) {

    val userData by dataStore.user.collectAsState(initial = null)
    val userId = userData?.id

    val cream = Color(0xFFF6F0E9)

    val pages = listOf(
        OnboardPage(
            stringResource(R.string.welcome_message) + " 🌱",
            stringResource(R.string.onboard1) + " 👉"
        ),
        OnboardPage(
            stringResource(R.string.onboard2_title) + " ✔️",
            stringResource(R.string.onboard2_desc) + " 😎"
        ),
        OnboardPage(
            stringResource(R.string.onboard3_title) + " 🧐",
            stringResource(R.string.onboard3_desc) + " 😇"
        ),
        OnboardPage(
            stringResource(R.string.onboard4_title) + " 🧑‍🌾", stringResource(R.string.onboard4_desc)
        ),
    )

    val pageCount = pages.size
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        if (userId != null) {
                            scope.launch {
                                dataStore.setHasSeenOnboarding(userId, true)
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }
                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ), shape = RoundedCornerShape(14.dp), border = BorderStroke(
                        1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                    )
                ) {
                    Text(stringResource(R.string.skip))
                }

            }

            Spacer(Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState, modifier = Modifier.weight(1f)
            ) { page ->

                Card(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxSize(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = cream),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OnboardPageContent(pages[page])
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 12.dp else 8.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            if (pagerState.currentPage == pages.lastIndex) {

                Button(
                    onClick = {
                        if (userId != null) {
                            scope.launch {
                                dataStore.setHasSeenOnboarding(userId, true)
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.letsgo))
                }

            } else {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    if (pagerState.currentPage > 0) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }, modifier = Modifier
                                .size(46.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                                    ), shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(Modifier.width(42.dp))
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    (pagerState.currentPage + 1)
                                )
                            }
                        }, modifier = Modifier
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

data class OnboardPage(val title: String, val desc: String)

@Composable
fun OnboardPageContent(page: OnboardPage) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.title, style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = page.desc,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
