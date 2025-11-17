package com.alex.hortina.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.alex.hortina.R

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 16.sp
    ), titleLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 22.sp
    ), labelSmall = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Medium, fontSize = 12.sp
    )
)
