package com.macro.automation.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.macro.automation.AutomationApp
import com.macro.automation.model.ActionType
import com.macro.automation.model.MacroAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutomationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val delaySec = intent?.getLongExtra(EXTRA_DELAY, 0L) ?: 0L
        val actionTypes = intent?.getStringArrayListExtra(EXTRA_ACTION_TYPES) ?: arrayListOf()
        val actionStates = intent?.getBooleanArrayExtra(EXTRA_ACTION_STATES) ?: booleanArrayOf()

        val actions = actionTypes.indices.map { i ->
            MacroAction(ActionType.valueOf(actionTypes[i]), actionStates[i])
        }

        val notification: Notification = NotificationCompat.Builder(this, AutomationApp.CHANNEL_ID)
            .setContentTitle("Macro Running")
            .setContentText("Delay: ${delaySec}s before actions trigger...")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .build()

        startForeground(101, notification)

        serviceScope.launch {
            if (delaySec > 0) {
                delay(delaySec * 1000L)
            }
            actions.forEach { action ->
                ActionExecutor.execute(action, applicationContext)
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    companion object {
        const val EXTRA_DELAY = "extra_delay"
        const val EXTRA_ACTION_TYPES = "extra_action_types"
        const val EXTRA_ACTION_STATES = "extra_action_states"

        fun runMacro(context: Context, delaySec: Long, actions: List<MacroAction>) {
            val intent = Intent(context, AutomationService::class.java).apply {
                putExtra(EXTRA_DELAY, delaySec)
                putStringArrayListExtra(EXTRA_ACTION_TYPES, ArrayList(actions.map { it.type.name }))
                putExtra(EXTRA_ACTION_STATES, actions.map { it.enable }.toBooleanArray())
            }
            context.startForegroundService(intent)
        }
    }
}
