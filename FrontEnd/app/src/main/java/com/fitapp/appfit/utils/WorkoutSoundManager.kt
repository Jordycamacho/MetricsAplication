package com.fitapp.appfit.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.media.SoundPool
import timber.log.Timber

object WorkoutSoundManager {

    private var soundPool: SoundPool? = null
    private var beepId: Int = 0
    private var bellId: Int = 0
    private var chimeId: Int = 0
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build()

            initialized = true
            Timber.d("WorkoutSoundManager inicializado")
        } catch (e: Exception) {
            Timber.e(e, "Error inicializando WorkoutSoundManager")
        }
    }

    fun playRestFinished(context: Context) {
        if (!WorkoutPreferences.isSoundEnabled(context)) return
        try {
            val type = WorkoutPreferences.getSoundType(context)
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
            when (type) {
                WorkoutPreferences.SoundType.BEEP -> {
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                    Thread.sleep(300)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                }
                WorkoutPreferences.SoundType.BELL -> {
                    toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                }
                WorkoutPreferences.SoundType.CHIME -> {
                    toneGen.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 600)
                }
            }
            Thread.sleep(800)
            toneGen.release()
        } catch (e: Exception) {
            Timber.e(e, "Error reproduciendo sonido")
        }
    }

    fun playSetComplete(context: Context) {
        if (!WorkoutPreferences.isSoundEnabled(context)) return
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 150)
            Thread.sleep(300)
            toneGen.release()
        } catch (e: Exception) {
            Timber.e(e, "Error reproduciendo sonido set")
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        initialized = false
    }
}