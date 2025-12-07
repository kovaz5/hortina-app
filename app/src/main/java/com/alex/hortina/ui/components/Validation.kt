package com.alex.hortina.ui.components

fun isValidEmail(email: String): Boolean {
    val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return regex.matches(email)
}

fun isValidPassword(password: String): Boolean {
    val regex = "^(?=.*[A-Z])(?=.*\\d).{8,}$".toRegex()
    return regex.matches(password)
}
