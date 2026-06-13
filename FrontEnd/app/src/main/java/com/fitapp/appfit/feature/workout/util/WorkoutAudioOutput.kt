package com.fitapp.appfit.feature.workout.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

/**
 * Elige la salida de audio del temporizador:
 * - Cascos/BT → STREAM_MUSIC (suena por los auriculares)
 * - Altavoz del móvil → STREAM_ALARM (funciona con pantalla apagada)
 */
object WorkoutAudioOutput {

    fun timerStreamType(context: Context): Int =
        if (hasHeadphonesConnected(context)) {
            AudioManager.STREAM_MUSIC
        } else {
            AudioManager.STREAM_ALARM
        }

    /**
     * Pide foco solo en STREAM_MUSIC para no pausar la música del usuario.
     * Usa ducking (baja volumen momentáneo) y debe liberarse con [TimerFocusHandle.abandon].
     */
    fun requestTimerFocus(context: Context, streamType: Int): TimerFocusHandle? {
        if (streamType != AudioManager.STREAM_MUSIC) return null

        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attrs)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(false)
                .build()
            val granted = am.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            return if (granted) TimerFocusHandle(am, request) else null
        }

        @Suppress("DEPRECATION")
        val granted = am.requestAudioFocus(
            null,
            streamType,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return if (granted) TimerFocusHandle(am) else null
    }

    private fun hasHeadphonesConnected(context: Context): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { device ->
                device.type in HEADPHONE_TYPES
            }
        }
        @Suppress("DEPRECATION")
        return am.isWiredHeadsetOn || am.isBluetoothA2dpOn || am.isBluetoothScoOn
    }

    private val HEADPHONE_TYPES = setOf(
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_BLE_HEADSET,
    )
}

class TimerFocusHandle internal constructor(
    private val audioManager: AudioManager,
    private val focusRequest: AudioFocusRequest? = null
) {
    fun abandon() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
            audioManager.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }
}
