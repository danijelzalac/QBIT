package com.qbit.chat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.qbit.chat.security.PinManager
import com.qbit.chat.security.PinResult
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarCoverScreen(onUnlock: () -> Unit) {
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Dynamic calendar: shows real current month
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = remember { LocalDate.now() }

    val monthTitle = remember(displayedMonth) {
        val monthName = displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        "$monthName ${displayedMonth.year}"
    }

    // Calculate calendar layout
    val daysInMonth = remember(displayedMonth) { displayedMonth.lengthOfMonth() }
    val firstDayOfWeek = remember(displayedMonth) {
        // Sunday = 0 offset for US calendar grid
        displayedMonth.atDay(1).dayOfWeek.value % 7
    }

    // Calendar Light Theme — designed to look exactly like a real calendar app
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF4285F4),
            background = Color.White,
            surface = Color.White,
            onPrimary = Color.White,
            onSurface = Color.Black
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = {
                            displayedMonth = displayedMonth.minusMonths(1)
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev")
                        }
                        IconButton(onClick = {
                            displayedMonth = displayedMonth.plusMonths(1)
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                }

                // Days Header
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                // Calendar Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.weight(1f)
                ) {
                    // Empty cells for offset
                    items(firstDayOfWeek) {
                        Box(modifier = Modifier.height(60.dp))
                    }
                    // Day cells
                    items(daysInMonth) { index ->
                        val dayNumber = index + 1
                        val isToday = displayedMonth.year == today.year
                                && displayedMonth.monthValue == today.monthValue
                                && dayNumber == today.dayOfMonth

                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .border(0.5.dp, Color(0xFFF0F0F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF4285F4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(text = "$dayNumber")
                            }
                        }
                    }
                }
            }

            // FAB — tapping this opens the "Add Event" dialog which is actually the PIN entry
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                backgroundColor = Color(0xFF4285F4)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event", tint = Color.White)
            }
        }
    }

    if (showAddEventDialog) {
        PinUnlockDialog(
            onDismiss = { showAddEventDialog = false },
            onUnlock = onUnlock
        )
    }
}

@Composable
fun PinUnlockDialog(onDismiss: () -> Unit, onUnlock: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Disguised as "New Event Duration" — innocent-looking
                Text("New Event", style = MaterialTheme.typography.h6, modifier = Modifier.padding(bottom = 16.dp))

                Text("Duration (minutes)", color = Color.Gray, fontSize = 14.sp)

                // PIN Display (Dots)
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(12.dp)
                                .background(
                                    if (index < pin.length) Color.Black else Color.LightGray,
                                    CircleShape
                                )
                        )
                    }
                }

                // Error Message
                var isError by remember { mutableStateOf(false) }
                if (isError) {
                     Text("Invalid duration", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                // Keypad
                Column {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("", "0", "<")
                    )

                    keys.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable {
                                            if (key == "<") {
                                                if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            } else if (key.isNotEmpty()) {
                                                if (pin.length < 4) {
                                                    pin += key
                                                    if (pin.length == 4) {
                                                        // Use PinManager for secure verification
                                                        when (PinManager.verifyEnteredPin(context, pin)) {
                                                            PinResult.REAL -> {
                                                                onUnlock()
                                                                onDismiss()
                                                            }
                                                            PinResult.DECOY -> {
                                                                // Decoy: show "Event Saved" toast and dismiss
                                                                android.widget.Toast.makeText(
                                                                    context,
                                                                    "Event saved",
                                                                    android.widget.Toast.LENGTH_SHORT
                                                                ).show()
                                                                onDismiss()
                                                            }
                                                            PinResult.WIPE -> {
                                                                // Panic: show "Event Saved" toast, then silent wipe
                                                                android.widget.Toast.makeText(
                                                                    context,
                                                                    "Event saved",
                                                                    android.widget.Toast.LENGTH_SHORT
                                                                ).show()
                                                                onDismiss()
                                                                Thread {
                                                                    Thread.sleep(1000)
                                                                    PinManager.wipe(context)
                                                                }.start()
                                                            }
                                                            PinResult.WRONG -> {
                                                                isError = true
                                                                pin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "<") {
                                        Text("⌫", fontSize = 20.sp)
                                    } else {
                                        Text(key, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    // "Save" button does nothing (PIN is auto-submit), but adds to the disguise
                    TextButton(onClick = { onDismiss() }) { Text("Save") }
                }
            }
        }
    }
}
