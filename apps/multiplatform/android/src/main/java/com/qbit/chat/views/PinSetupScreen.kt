package com.qbit.chat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qbit.chat.security.PinManager
import kotlinx.coroutines.delay

/**
 * QBIT PIN Setup Screen
 *
 * Shown on first launch when no PIN is configured.
 * Also used for changing PIN from settings.
 *
 * Flow: Enter PIN → Confirm PIN → (Optional) Set Decoy PIN → Done
 */

enum class PinSetupStep {
    ENTER_NEW,
    CONFIRM_NEW,
    ENTER_DECOY,
    CONFIRM_DECOY,
    DONE
}

@Composable
fun PinSetupScreen(
    isChangeMode: Boolean = false,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(PinSetupStep.ENTER_NEW) }
    var pin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var decoyPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSkipDecoyDialog by remember { mutableStateOf(false) }

    val title = when (step) {
        PinSetupStep.ENTER_NEW -> if (isChangeMode) "New Unlock Code" else "Set Your Unlock Code"
        PinSetupStep.CONFIRM_NEW -> "Confirm Unlock Code"
        PinSetupStep.ENTER_DECOY -> "Set Decoy Code (Optional)"
        PinSetupStep.CONFIRM_DECOY -> "Confirm Decoy Code"
        PinSetupStep.DONE -> "Setup Complete"
    }

    val subtitle = when (step) {
        PinSetupStep.ENTER_NEW -> "Choose a 4-digit code to access your secure space"
        PinSetupStep.CONFIRM_NEW -> "Enter the same code again"
        PinSetupStep.ENTER_DECOY -> "A decoy code shows only the calendar when entered"
        PinSetupStep.CONFIRM_DECOY -> "Enter the decoy code again"
        PinSetupStep.DONE -> ""
    }

    // Calendar-themed disguise: looks like "app setup" not "PIN entry"
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF4285F4),
            background = Color.White,
            surface = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon area
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF4285F4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = subtitle,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // PIN Dots
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .size(16.dp)
                                    .background(
                                        if (index < pin.length) Color(0xFF4285F4) else Color(0xFFE0E0E0),
                                        CircleShape
                                    )
                            )
                        }
                    }

                    // Error message
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Keypad
                    PinKeypad(
                        pin = pin,
                        onPinChanged = { newPin ->
                            errorMessage = null
                            pin = newPin
                        },
                        onComplete = { enteredPin ->
                            when (step) {
                                PinSetupStep.ENTER_NEW -> {
                                    firstPin = enteredPin
                                    pin = ""
                                    step = PinSetupStep.CONFIRM_NEW
                                }
                                PinSetupStep.CONFIRM_NEW -> {
                                    if (enteredPin == firstPin) {
                                        PinManager.setRealPin(context, enteredPin)
                                        pin = ""
                                        step = PinSetupStep.ENTER_DECOY
                                    } else {
                                        errorMessage = "Codes don't match. Try again."
                                        pin = ""
                                        step = PinSetupStep.ENTER_NEW
                                        firstPin = ""
                                    }
                                }
                                PinSetupStep.ENTER_DECOY -> {
                                    if (enteredPin == firstPin) {
                                        errorMessage = "Decoy must be different from unlock code"
                                        pin = ""
                                    } else {
                                        decoyPin = enteredPin
                                        pin = ""
                                        step = PinSetupStep.CONFIRM_DECOY
                                    }
                                }
                                PinSetupStep.CONFIRM_DECOY -> {
                                    if (enteredPin == decoyPin) {
                                        PinManager.setDecoyPin(context, enteredPin)
                                        onComplete()
                                    } else {
                                        errorMessage = "Codes don't match. Try again."
                                        pin = ""
                                        step = PinSetupStep.ENTER_DECOY
                                        decoyPin = ""
                                    }
                                }
                                PinSetupStep.DONE -> {}
                            }
                        }
                    )

                    // Skip decoy option (only during decoy setup)
                    if (step == PinSetupStep.ENTER_DECOY) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { showSkipDecoyDialog = true }) {
                            Text("Skip Decoy Setup", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Skip decoy confirmation dialog
    if (showSkipDecoyDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDecoyDialog = false },
            title = { Text("Skip Decoy Code?") },
            text = { Text("Without a decoy code, there's no plausible deniability if someone forces you to unlock the app. You can set one later in settings.") },
            confirmButton = {
                TextButton(onClick = {
                    showSkipDecoyDialog = false
                    onComplete()
                }) {
                    Text("Skip", color = Color(0xFF4285F4))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDecoyDialog = false }) {
                    Text("Set Decoy")
                }
            }
        )
    }
}

/**
 * Reusable numpad keypad component.
 */
@Composable
fun PinKeypad(
    pin: String,
    onPinChanged: (String) -> Unit,
    onComplete: (String) -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "<")
    )

    Column {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable(enabled = key.isNotEmpty()) {
                                if (key == "<") {
                                    if (pin.isNotEmpty()) onPinChanged(pin.dropLast(1))
                                } else {
                                    if (pin.length < 4) {
                                        val newPin = pin + key
                                        onPinChanged(newPin)
                                        if (newPin.length == 4) {
                                            onComplete(newPin)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "<") {
                            Text("⌫", fontSize = 20.sp, color = Color.Gray)
                        } else {
                            Text(
                                key,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (key.isEmpty()) Color.Transparent else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
