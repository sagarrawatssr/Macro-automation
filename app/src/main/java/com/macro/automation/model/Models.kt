package com.macro.automation.model

enum class ActionType {
    TOGGLE_WIFI,
    TOGGLE_BLUETOOTH,
    TOGGLE_MOBILE_DATA,
    TOGGLE_LOCATION,
    TOGGLE_HOTSPOT
}

data class MacroAction(
    val type: ActionType,
    val enable: Boolean
)

data class Macro(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val delaySeconds: Long,
    val actions: List<MacroAction>
)
