package com.qbit.chat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
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
import com.qbit.chat.security.PinResult

/**
 * QBIT Security Settings Screen
 *
 * Accessible from within the unlocked app. Allows:
 * - Changing the unlock PIN
 * - Setting/changing the decoy PIN
 * - Clearing all data (factory reset)
 */

enum class SettingsMode {
    MENU,
    VERIFY_CURRENT,
    CHANGE_REAL,
    CHANGE_DECOY,
    CHANGE_PANIC
}

@Composable
fun SecuritySettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(SettingsMode.MENU) }
    var showSuccess by remember { mutableStateOf<String?>(null) }
    val hasDecoy = remember { PinManager.isDecoyConfigured(context) }
    val hasPanic = remember { PinManager.isPanicConfigured(context) }

    when (mode) {
        SettingsMode.MENU -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Security Settings") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, "Back")
                            }
                        },
                        backgroundColor = Color(0xFF1A1A2E),
                        contentColor = Color.White
                    )
                },
                backgroundColor = Color(0xFF16213E)
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    if (showSuccess != null) {
                        Card(
                            backgroundColor = Color(0xFF00C853),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                showSuccess!!,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Change Unlock Code
                    SettingsItem(
                        icon = "🔑",
                        title = "Change Unlock Code",
                        subtitle = "Change the code that opens your secure space",
                        onClick = { mode = SettingsMode.VERIFY_CURRENT }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Set/Change Decoy Code
                    SettingsItem(
                        icon = "🛡️",
                        title = if (hasDecoy) "Change Decoy Code" else "Set Decoy Code",
                        subtitle = if (hasDecoy)
                            "Change the code that shows a normal calendar"
                        else
                            "Set a code that dismisses the unlock dialog and shows a normal calendar",
                        onClick = { mode = SettingsMode.CHANGE_DECOY }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Set/Change Panic Code
                    SettingsItem(
                        icon = "⚠️",
                        title = if (hasPanic) "Change Panic Code" else "Set Panic Code",
                        subtitle = "Set a code that silently wipes all chat data",
                        onClick = { mode = SettingsMode.CHANGE_PANIC }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Info card
                    Card(
                        backgroundColor = Color(0xFF1A1A2E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "ℹ️ How it works",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4FC3F7),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "• Unlock code → opens your secure chat\n" +
                                "• Decoy code → shows 'Event saved' and stays in calendar\n" +
                                "• Panic code → silently destroys all chat data\n" +
                                "• Both codes are stored securely with PBKDF2 encryption",
                                color = Color(0xFFB0BEC5),
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        SettingsMode.VERIFY_CURRENT -> {
            VerifyCurrentPinScreen(
                onVerified = { mode = SettingsMode.CHANGE_REAL },
                onBack = { mode = SettingsMode.MENU }
            )
        }

        SettingsMode.CHANGE_REAL -> {
            PinSetupScreen(
                isChangeMode = true,
                onComplete = {
                    showSuccess = "✓ Unlock code changed successfully"
                    mode = SettingsMode.MENU
                }
            )
        }

        SettingsMode.CHANGE_DECOY -> {
            SetDecoyPinScreen(
                onComplete = {
                    showSuccess = "✓ Decoy code set successfully"
                    mode = SettingsMode.MENU
                },
                onBack = { mode = SettingsMode.MENU }
            )
        }

        SettingsMode.CHANGE_PANIC -> {
            SetPanicPinScreen(
                onComplete = {
                    showSuccess = "✓ Panic code set successfully"
                    mode = SettingsMode.MENU
                },
                onBack = { mode = SettingsMode.MENU }
            )
        }
    }
}

@Composable
fun SettingsItem(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = Color(0xFF1A1A2E),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(subtitle, color = Color(0xFF78909C), fontSize = 13.sp)
            }
            Text("›", color = Color(0xFF78909C), fontSize = 24.sp)
        }
    }
}

/**
 * Screen to verify the current unlock PIN before allowing a change.
 */
@Composable
fun VerifyCurrentPinScreen(onVerified: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Current Code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                backgroundColor = Color(0xFF1A1A2E),
                contentColor = Color.White
            )
        },
        backgroundColor = Color(0xFF16213E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter your current unlock code", color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // PIN dots
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(16.dp)
                            .background(
                                if (index < pin.length) Color(0xFF4FC3F7) else Color(0xFF37474F),
                                CircleShape
                            )
                    )
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(error!!, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PinKeypad(
                pin = pin,
                onPinChanged = {
                    error = null
                    pin = it
                },
                onComplete = { enteredPin ->
                    when (PinManager.verifyEnteredPin(context, enteredPin)) {
                        PinResult.REAL -> onVerified()
                        else -> {
                            error = "Incorrect code"
                            pin = ""
                        }
                    }
                }
            )
        }
    }
}

/**
 * Screen for setting/changing the decoy PIN.
 */
@Composable
fun SetDecoyPinScreen(onComplete: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var isConfirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isConfirm) "Confirm Decoy Code" else "Set Decoy Code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                backgroundColor = Color(0xFF1A1A2E),
                contentColor = Color.White
            )
        },
        backgroundColor = Color(0xFF16213E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isConfirm) "Enter the same code again" else "Choose a 4-digit decoy code",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "This code will dismiss the dialog and stay in calendar mode",
                color = Color(0xFF78909C),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(16.dp)
                            .background(
                                if (index < pin.length) Color(0xFF4FC3F7) else Color(0xFF37474F),
                                CircleShape
                            )
                    )
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(error!!, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PinKeypad(
                pin = pin,
                onPinChanged = {
                    error = null
                    pin = it
                },
                onComplete = { enteredPin ->
                    if (!isConfirm) {
                        // Check that decoy != real PIN
                        if (PinManager.verifyEnteredPin(context, enteredPin) == PinResult.REAL) {
                            error = "Decoy code must be different from unlock code"
                            pin = ""
                        } else {
                            firstPin = enteredPin
                            pin = ""
                            isConfirm = true
                        }
                    } else {
                        if (enteredPin == firstPin) {
                            PinManager.setDecoyPin(context, enteredPin)
                            onComplete()
                        } else {
                            error = "Codes don't match. Try again."
                            pin = ""
                            isConfirm = false
                            firstPin = ""
                        }
                    }
                }
            )
        }
    }
}

/**
 * Screen for setting/changing the Panic PIN.
 */
@Composable
fun SetPanicPinScreen(onComplete: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var isConfirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isConfirm) "Confirm Panic Code" else "Set Panic Code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                backgroundColor = Color(0xFF1A1A2E), // Dark Red/Purple for Danger? Keep consistent for stealth.
                contentColor = Color.White
            )
        },
        backgroundColor = Color(0xFF16213E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isConfirm) "Enter the same code again" else "Choose a 4-digit Panic code",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "⚠️ This code will SILENTLY WIPE all chat data when entered in the calendar",
                color = Color(0xFFFF5252), // Red warning
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(16.dp)
                            .background(
                                if (index < pin.length) Color(0xFFFF5252) else Color(0xFF37474F), // Red dots
                                CircleShape
                            )
                    )
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(error!!, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PinKeypad(
                pin = pin,
                onPinChanged = {
                    error = null
                    pin = it
                },
                onComplete = { enteredPin ->
                    if (!isConfirm) {
                        // Check conflicts
                        when (PinManager.verifyEnteredPin(context, enteredPin)) {
                            PinResult.REAL -> {
                                error = "Panic code must be different from unlock code"
                                pin = ""
                            }
                            PinResult.DECOY -> {
                                error = "Panic code must be different from decoy code"
                                pin = ""
                            }
                            else -> {
                                firstPin = enteredPin
                                pin = ""
                                isConfirm = true
                            }
                        }
                    } else {
                        if (enteredPin == firstPin) {
                            PinManager.setPanicPin(context, enteredPin)
                            onComplete()
                        } else {
                            error = "Codes don't match. Try again."
                            pin = ""
                            isConfirm = false
                            firstPin = ""
                        }
                    }
                }
            )
        }
    }
}
