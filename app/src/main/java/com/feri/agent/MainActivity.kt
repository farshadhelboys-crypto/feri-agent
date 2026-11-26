package com.feri.agent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.feri.agent.service.VoiceAgentService
import com.feri.agent.ui.FeriAgentApp
import com.feri.agent.ui.theme.FeriAgentTheme

class MainActivity : ComponentActivity() {

    private val requiredPermissions = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.READ_CONTACTS)
        add(Manifest.permission.CALL_PHONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            startVoiceService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FeriAgentTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FeriAgentApp(
                        onStartService = { checkAndRequestPermissions() },
                        onStopService = { stopVoiceService() }
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            startVoiceService()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startVoiceService() {
        val intent = Intent(this, VoiceAgentService::class.java).apply {
            action = VoiceAgentService.ACTION_START
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopVoiceService() {
        val intent = Intent(this, VoiceAgentService::class.java).apply {
            action = VoiceAgentService.ACTION_STOP
        }
        startService(intent)
    }
}
