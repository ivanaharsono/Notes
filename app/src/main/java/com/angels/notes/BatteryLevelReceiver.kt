package com.angels.notes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BatteryLevelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_LOW) {
            Toast.makeText(context, "Battery is low! Please save your notes soon.", Toast.LENGTH_LONG).show()
        }
    }
}