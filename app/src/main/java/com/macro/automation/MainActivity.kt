package com.macro.automation

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.macro.automation.model.ActionType
import com.macro.automation.model.MacroAction
import com.macro.automation.service.ActionExecutor
import com.macro.automation.service.AutomationService
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        // Trigger UI update when permission granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MacroScreen(
                        onRequestShizuku = { requestShizukuPermission() },
                        onExecuteMacro = { delaySec, actions ->
                            AutomationService.runMacro(this, delaySec, actions)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
    }

    private fun requestShizukuPermission() {
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(1001)
            }
        }
    }
}

@Composable
fun MacroScreen(
    onRequestShizuku: () -> Unit,
    onExecuteMacro: (Long, List<MacroAction>) -> Unit
) {
    var delayText by remember {树 var delay = "5"; mutableStateOf(delay) }
    var wifiState by remember { mutableStateOf(true) }
    var btState by remember { mutableStateOf(false) }
    var dataState by remember { mutableStateOf(true) }
    var locState by remember { mutableStateOf(false) }
    var hotspotState by remember { mutableStateOf(false) }

    val hasShizuku = ActionExecutor.isShizukuAvailable()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Macro Automation", style = MaterialTheme.typography.headlineMedium)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (hasShizuku) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (hasShizuku) "Privilege Status: Connected (Shizuku)" else "Privilege Status: Disconnected",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!hasShizuku) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = onRequestShizuku) {
                        Text("Grant Shizuku Permission")
                    }
                }
            }
        }

        OutlinedTextField(
            value = delayText,
            onValueChange = { delayText = it },
            label = { Text("Timer Delay Trigger (seconds)") },
            modifier = Modifier.fillMaxWidth()
        )

        Divider()
        Text("Actions to execute after trigger:", style = MaterialTheme.typography.titleMedium)

        ActionRow(label = "Wi-Fi", checked = wifiState, onCheckedChange = { wifiState = it })
        ActionRow(label = "Bluetooth", checked = btState, onCheckedChange = { btState = it })
        ActionRow(label = "Mobile Data", checked = dataState, onCheckedChange = { dataState = it })
        ActionRow(label = "Live Location", checked = locState, onCheckedChange = { locState = it })
        ActionRow(label = "Hotspot", checked = hotspotState, onCheckedChange = { hotspotState = it })

        Button(
            onClick = {
                val delayVal = delayText.toLongOrNull() ?: 0L
                val actions = listOf(
                    MacroAction(ActionType.TOGGLE_WIFI, wifiState),
                    MacroAction(ActionType.TOGGLE_BLUETOOTH, btState),
                    MacroAction(ActionType.TOGGLE_MOBILE_DATA, dataState),
                    MacroAction(ActionType.TOGGLE_LOCATION, locState),
                    MacroAction(ActionType.TOGGLE_HOTSPOT, hotspotState)
                )
                onExecuteMacro(delayVal, actions)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Arm / Run Macro")
        }
    }
}

@Composable
fun ActionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
