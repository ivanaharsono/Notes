package com.angels.notes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NetworkStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isConnected = intent.getBooleanExtra("isConnected", true)
        if (!isConnected) {
            Toast.makeText(context, "Network Disconnected! Please check your internet.", Toast.LENGTH_LONG).show()
        }
    }
}