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
import androidx.compose.ui.window.Dialog

import androidx.compose.ui.platform.LocalUriHandler

@Composable
fun CalendarCoverScreen(onUnlock: () -> Unit) {
    var showAddEventDialog by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf("September 2026") }
    val uriHandler = LocalUriHandler.current

    // Calendar Light Theme
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
                        text = currentMonth,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = { /* Prev Month */ }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev")
                        }
                        IconButton(onClick = { /* Next Month */ }) {
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

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.weight(1f)
                ) {
                    // Empty start days
                    items(2) {
                        Box(modifier = Modifier.height(60.dp))
                    }
                    // Days
                    items(30) { day ->
                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .border(0.5.dp, Color(0xFFF0F0F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "${day + 1}")
                        }
                    }
                }

                // Credit Footer (Hidden in plain sight)
                Text(
                    text = "Danijel Zalac 2026",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                        .clickable { uriHandler.openUri("https://github.com/danijelzalac") }
                )
            }

            // FAB
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
    val correctPin = "2026"
    val decoyPin = "0000"

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
                Text("New Event", style = MaterialTheme.typography.h6, modifier = Modifier.padding(bottom = 16.dp))
                
                Text("Duration (minutes)", color = Color.Gray, fontSize = 14.sp)
                
                // PIN Display
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
                                                        if (pin == correctPin) {
                                                            onUnlock()
                                                            onDismiss()
                                                        } else if (pin == decoyPin) {
                                                            // Decoy action
                                                            onDismiss()
                                                        } else {
                                                            // Wrong pin
                                                            pin = ""
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
                    TextButton(onClick = { /* Fake save */ onDismiss() }) { Text("Save") }
                }
            }
        }
    }
}
