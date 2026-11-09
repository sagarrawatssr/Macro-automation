package com.macro.automation.service

import android.content.Context
import android.content.pm.PackageManager
import com.macro.automation.model.ActionType
import com.macro.automation.model.MacroAction
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ActionExecutor {

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun execute(action: MacroAction, context: Context): Boolean {
        val state = if (action.enable) "enable" else "disable"
        val stateBool = if (action.enable) "true" else "false"
        val stateInt = if (action.enable) "1" else "0"

        val command = when (action.type) {
            ActionType.TOGGLE_WIFI -> "svc wifi $state"
            ActionType.TOGGLE_BLUETOOTH -> "cmd bluetooth_manager $state || svc bluetooth $state"
            ActionType.TOGGLE_MOBILE_DATA -> "svc data $state"
            ActionType.TOGGLE_LOCATION -> "cmd location set-location-enabled $stateBool || settings put secure location_mode $stateInt"
            ActionType.TOGGLE_HOTSPOT -> {
                if (action.enable) "cmd tethering start-tethering 0"
                else "cmd tethering stop-tethering"
            }
        }

        return runPrivilegedCommand(command)
    }

    private fun runPrivilegedCommand(cmd: String): Boolean {
        return if (isShizukuAvailable()) {
            runViaShizuku(cmd)
        } else {
            // Fallback to su if rooted
            runViaRoot(cmd)
        }
    }

    private fun runViaShizuku(cmd: String): Boolean {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", cmd), null, null)
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun runViaRoot(cmd: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
