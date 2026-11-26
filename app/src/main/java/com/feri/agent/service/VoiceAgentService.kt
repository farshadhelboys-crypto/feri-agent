package com.feri.agent.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.feri.agent.FeriApp
import com.feri.agent.MainActivity
import com.feri.agent.R
import com.feri.agent.command.CommandHandler
import java.util.Locale

class VoiceAgentService : Service(), TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isListening = false
    private var isActiveSession = false // after "داداش؟"
    private lateinit var commandHandler: CommandHandler
    private lateinit var audioManager: AudioManager

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        commandHandler = CommandHandler(this)
        tts = TextToSpeech(this, this)
        setupSpeechRecognizer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, createNotification("آماده • منتظر داداش؟"))
                startListening()
            }
            ACTION_STOP -> {
                stopListening()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("fa", "IR")
            // fallback to default if Persian not available
            if (tts?.isLanguageAvailable(Locale("fa")) == TextToSpeech.LANG_MISSING_DATA) {
                tts?.language = Locale.getDefault()
            }
        }
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e(TAG, "Speech recognition not available")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    Log.d(TAG, "Ready for speech")
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    // restart listening shortly
                }

                override fun onError(error: Int) {
                    Log.w(TAG, "Speech error: $error")
                    // Restart listening after short delay
                    android.os.Handler(mainLooper).postDelayed({
                        if (isListening) startListening()
                    }, 800)
                }

                override fun onResults(results: android.os.Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()?.trim()?.lowercase(Locale.ROOT) ?: return

                    Log.d(TAG, "Heard: $spoken")
                    handleSpokenText(spoken)

                    // Continue listening
                    android.os.Handler(mainLooper).postDelayed({
                        if (isListening) startListening()
                    }, 400)
                }

                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
        }
    }

    private fun handleSpokenText(text: String) {
        // Hotword detection
        if (!isActiveSession) {
            if (text.contains("داداش") || text.contains("dadash") || text.contains("داش")) {
                isActiveSession = true
                speak("میشنوم")
                updateNotification("در حال شنیدن دستور...")
                // keep session open for a few seconds
                android.os.Handler(mainLooper).postDelayed({
                    isActiveSession = false
                    updateNotification("آماده • منتظر داداش؟")
                }, 12000)
            }
            return
        }

        // Command mode
        val response = commandHandler.handle(text)
        if (response != null) {
            speak(response)
        }
    }

    private fun startListening() {
        if (speechRecognizer == null) return
        isListening = true

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
        }
    }

    private fun stopListening() {
        isListening = false
        isActiveSession = false
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "feri_utterance")
    }

    private fun createNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FeriApp.CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification(text))
    }

    override fun onDestroy() {
        stopListening()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.feri.agent.START"
        const val ACTION_STOP = "com.feri.agent.STOP"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "VoiceAgentService"
    }
}
