package com.macro.automation.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
// Ensure this import is present if in a different package or scope:
import com.macro.automation.service.ActionExecutor

class AutomationService : Service() {

    // ... your notification & service lifecycle code ...

    fun runCommand(cmd: String) {
        // Line 46 call:
        ActionExecutor.execute(cmd)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
