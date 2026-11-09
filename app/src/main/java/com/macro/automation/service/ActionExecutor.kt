package com.macro.automation.service

import android.content.pm.PackageManager
import dev.rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method

object ActionExecutor {

    fun executeShell(command: String): Boolean {
        return try {
            val process = if (isShizukuAvailable()) {
                executeViaShizuku(command)
            } else {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            }

            process?.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun executeViaShizuku(command: String): Process? {
        return try {
            // Shizuku.newProcess is private in newer API distributions; invoke via reflection
            val newProcessMethod: Method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            newProcessMethod.isAccessible = true
            newProcessMethod.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as? Process
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && 
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
}
