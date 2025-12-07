package com.alex.hortina.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alex.hortina.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarDateField(
    label: String, date: LocalDate, onDateSelected: (LocalDate) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = date.format(formatter),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = null)
            })

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    open = true
                })
    }

    if (open) {
        CalendarDialog(initialDate = date, onDismiss = { open = false }, onDateSelected = {
            onDateSelected(it)
            open = false
        })
    }
}


@Composable
fun CalendarDialog(
    initialDate: LocalDate, onDismiss: () -> Unit, onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(initialDate.withDayOfMonth(1)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(initialDate) }

    AlertDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
    }, text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                }

                Text(
                    currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }

            Spacer(Modifier.height(8.dp))

            val weekDays = listOf("L", "M", "X", "J", "V", "S", "D")
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach {
                    Text(
                        it,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            CalendarMonthGrid(
                monthStart = currentMonth, selectedDate = selectedDate, onDayClick = { date ->
                    selectedDate = date
                    onDateSelected(date)
                    onDismiss()
                })
        }
    })
}


@Composable
fun CalendarMonthGrid(
    monthStart: LocalDate, selectedDate: LocalDate?, onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfWeek = monthStart.dayOfWeek.value % 7
    val daysInMonth = monthStart.lengthOfMonth()

    Column {
        var dayCounter = 1 - firstDayOfWeek

        repeat(6) {
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) {
                    if (dayCounter in 1..daysInMonth) {
                        val date = monthStart.withDayOfMonth(dayCounter)
                        val isSelected = selectedDate == date

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable { onDayClick(date) }, contentAlignment = Alignment.Center
                        ) {
                            // CÍRCULO SOLO SI ESTÁ SELECCIONADO
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                            shape = CircleShape
                                        ), contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayCounter.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Text(
                                    text = dayCounter.toString(),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    dayCounter++
                }
            }
        }
    }
}



