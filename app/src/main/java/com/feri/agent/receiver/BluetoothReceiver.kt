package com.feri.agent.receiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.feri.agent.service.VoiceAgentService

class BluetoothReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        val name = try {
            device?.name ?: "Unknown
        } catch (e: SecurityException) {
            "Unknown"
        }

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                Log.d(TAG, "Bluetooth connected: $name")
                // Optionally auto-start service when AirPods connect
                if (isLikelyAirPods(name)) {
                    val serviceIntent = Intent(context, VoiceAgentService::class.java).apply {
                        this.action = VoiceAgentService.ACTION_START
                    }
                    context.startForegroundService(serviceIntent)
                }
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                Log.d(TAG, "Bluetooth disconnected: $name")
            }
        }
    }

    private fun isLikelyAirPods(name: String): Boolean {
        val lower = name.lowercase()
        return lower.contains("airpods") ||
                lower.contains("airpod") ||
                lower.contains("buds") ||
                lower.contains("galaxy buds") ||
                lower.contains("freebuds")
    }

    companion object {
        private const val TAG = "BluetoothReceiver"
    }
}
