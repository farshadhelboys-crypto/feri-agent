package com.feri.agent.command

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.view.KeyEvent

class CommandHandler(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun handle(text: String): String? {
        val t = text.lowercase().trim()

        return when {
            // Next track
            t.contains("بعدی") || t.contains("برو جلو") || t.contains("آهنگ بعدی") ||
            t.contains("موزیک بعدی") || t.contains("next") -> {
                sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                "اوکی، آهنگ بعدی"
            }

            // Previous track
            t.contains("قبلی") || t.contains("آهنگ قبلی") || t.contains("موزیک قبلی") ||
            t.contains("previous") || t.contains("قبل") -> {
                sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                "اوکی، آهنگ قبلی"
            }

            // Play / Pause
            t.contains("پلی") || t.contains("پخش") || t.contains("ادامه بده") -> {
                sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
                "اوکی، پخش شد"
            }
            t.contains("توقف") || t.contains("پاز") || t.contains("مکث") || t.contains("ایست") -> {
                sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
                "اوکی، متوقف شد"
            }

            // Volume up
            t.contains("ولوم") && (t.contains("زیاد") || t.contains("بالا") || t.contains("بیشتر")) ||
            t.contains("صدا رو زیاد") || t.contains("بلندتر") -> {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                "اوکی، ولوم زیاد شد"
            }

            // Volume down
            t.contains("ولوم") && (t.contains("کم") || t.contains("پایین") || t.contains("کمتر")) ||
            t.contains("صدا رو کم") || t.contains("آروم‌تر") || t.contains("آرومتر") -> {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                "اوکی، ولوم کم شد"
            }

            // Call contact: "به فلانی زنگ بزن" / "زنگ بزن به ..."
            t.contains("زنگ بزن") || t.contains("تماس بگیر") || t.contains("تماس با") -> {
                val name = extractContactName(t)
                if (name != null) {
                    val number = findContactNumber(name)
                    if (number != null) {
                        makeCall(number)
                        "اوکی الان به $name زنگ می‌زنم"
                    } else {
                        "متأسفم، مخاطب $name رو پیدا نکردم"
                    }
                } else {
                    "اسم مخاطب رو واضح‌تر بگو"
                }
            }

            else -> {
                Log.d("CommandHandler", "Unknown command: $t")
                null // no response for unknown
            }
        }
    }

    private fun sendMediaKey(keyCode: Int) {
        try {
            val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val up = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            audioManager.dispatchMediaKeyEvent(down)
            audioManager.dispatchMediaKeyEvent(up)
        } catch (e: Exception) {
            Log.e("CommandHandler", "Media key failed", e)
        }
    }

    private fun extractContactName(text: String): String? {
        // Patterns: "به علی زنگ بزن" / "زنگ بزن به مریم" / "تماس بگیر با رضا"
        val patterns = listOf(
            Regex("به\s+(.+?)\s+زنگ"),
            Regex("زنگ\s+بزن\s+به\s+(.+)"),
            Regex("تماس\s+بگیر\s+با\s+(.+)"),
            Regex("تماس\s+با\s+(.+)")
        )
        for (p in patterns) {
            val match = p.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
                    .replace("زنگ بزن", "")
                    .replace("کن", "")
                    .trim()
            }
        }
        return null
    }

    private fun findContactNumber(name: String): String? {
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$name%"),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numberIndex >= 0) {
                    return it.getString(numberIndex)
                }
            }
        }
        return null
    }

    private fun makeCall(number: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: SecurityException) {
            Log.e("CommandHandler", "No CALL_PHONE permission", e)
        } catch (e: Exception) {
            Log.e("CommandHandler", "Call failed", e)
        }
    }
}
