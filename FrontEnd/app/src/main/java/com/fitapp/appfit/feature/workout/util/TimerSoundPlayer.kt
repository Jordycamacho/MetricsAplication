package com.fitapp.appfit.feature.workout.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import timber.log.Timber

/**
 * Manager para reproducir sonidos de timers.
 * Usa SoundPool para mejor control y funcionamiento con pantalla apagada.
 *
 * Los sonidos se generan sintéticamente (sin archivos de audio).
 */
object TimerSoundPlayer {

    private var soundPool: SoundPool? = null
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
            Timber.d("TimerSoundPlayer inicializado")
        } catch (e: Exception) {
            Timber.e(e, "Error inicializando TimerSoundPlayer")
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        initialized = false
    }

    /**
     * Reproduce el sonido configurado para el tipo de timer.
     * Respeta el volumen global y si los sonidos están habilitados.
     */
    fun playTimerSound(
        context: Context,
        timerType: WorkoutPreferences.TimerSoundType
    ) {
        if (!WorkoutPreferences.isSoundEnabled(context)) return
        if (!initialized) init(context)

        val soundVariant = WorkoutPreferences.getTimerSound(context, timerType)
        val volume = WorkoutPreferences.getTimerVolume(context) / 100f

        try {
            playSound(soundVariant, volume, timerType)
        } catch (e: Exception) {
            Timber.e(e, "Error reproduciendo sonido de timer")
        }
    }

    private fun playSound(
        variant: WorkoutPreferences.SoundVariant,
        volume: Float,
        timerType: WorkoutPreferences.TimerSoundType
    ) {
        // Reproducir sonido en hilo de IO para no bloquear UI
        Thread {
            when (variant) {
                WorkoutPreferences.SoundVariant.BEEP -> playBeep(volume, timerType)
                WorkoutPreferences.SoundVariant.BELL -> playBell(volume, timerType)
                WorkoutPreferences.SoundVariant.CHIME -> playChime(volume, timerType)
                WorkoutPreferences.SoundVariant.BUZZ -> playBuzz(volume, timerType)
                WorkoutPreferences.SoundVariant.PING -> playPing(volume, timerType)
            }
        }.start()
    }

    /**
     * BEEP: Pitido doble, discreto. Ideal para sets.
     */
    private fun playBeep(volume: Float, timerType: WorkoutPreferences.TimerSoundType) {
        val frequencies = when (timerType) {
            WorkoutPreferences.TimerSoundType.SET_REST -> listOf(800 to 150, 950 to 150)
            WorkoutPreferences.TimerSoundType.EXERCISE_REST -> listOf(900 to 200, 1050 to 200)
            WorkoutPreferences.TimerSoundType.DURATION_COMPLETE -> listOf(700 to 100, 800 to 100, 900 to 100)
        }
        playSynthSound(frequencies, volume)
    }

    /**
     * BELL: Campana suave, clásica. Ideal para ejercicios.
     */
    private fun playBell(volume: Float, timerType: WorkoutPreferences.TimerSoundType) {
        val frequencies = when (timerType) {
            WorkoutPreferences.TimerSoundType.SET_REST -> listOf(523 to 400, 659 to 200)
            WorkoutPreferences.TimerSoundType.EXERCISE_REST -> listOf(523 to 500, 659 to 300)
            WorkoutPreferences.TimerSoundType.DURATION_COMPLETE -> listOf(523 to 200, 659 to 200, 523 to 200)
        }
        playSynthSound(frequencies, volume)
    }

    /**
     * CHIME: Tono suave y melodioso. Versátil.
     */
    private fun playChime(volume: Float, timerType: WorkoutPreferences.TimerSoundType) {
        val frequencies = when (timerType) {
            WorkoutPreferences.TimerSoundType.SET_REST -> listOf(1047 to 300, 1319 to 200)
            WorkoutPreferences.TimerSoundType.EXERCISE_REST -> listOf(1047 to 400, 1319 to 300)
            WorkoutPreferences.TimerSoundType.DURATION_COMPLETE -> listOf(1047 to 150, 1319 to 150, 1047 to 150)
        }
        playSynthSound(frequencies, volume)
    }

    /**
     * BUZZ: Sonido de zumbido, único. Muy diferenciador.
     */
    private fun playBuzz(volume: Float, timerType: WorkoutPreferences.TimerSoundType) {
        val frequencies = when (timerType) {
            WorkoutPreferences.TimerSoundType.SET_REST -> listOf(200 to 250, 150 to 100, 200 to 100)
            WorkoutPreferences.TimerSoundType.EXERCISE_REST -> listOf(200 to 350, 150 to 150, 200 to 150)
            WorkoutPreferences.TimerSoundType.DURATION_COMPLETE -> listOf(200 to 100, 150 to 100, 200 to 100, 250 to 100)
        }
        playSynthSound(frequencies, volume)
    }

    /**
     * PING: Sonido agudo y corto, muy moderno.
     */
    private fun playPing(volume: Float, timerType: WorkoutPreferences.TimerSoundType) {
        val frequencies = when (timerType) {
            WorkoutPreferences.TimerSoundType.SET_REST -> listOf(2000 to 100, 2500 to 100)
            WorkoutPreferences.TimerSoundType.EXERCISE_REST -> listOf(2000 to 150, 2500 to 150)
            WorkoutPreferences.TimerSoundType.DURATION_COMPLETE -> listOf(2000 to 80, 2500 to 80, 2000 to 80)
        }
        playSynthSound(frequencies, volume)
    }

    /**
     * Reproduce una secuencia de frecuencias sintéticamente usando ToneGenerator.
     * Lista: (frecuencia Hz, duración ms)
     */
    private fun playSynthSound(frequencies: List<Pair<Int, Int>>, volume: Float) {
        try {
            // Convertir volumen 0-1 a rango de ToneGenerator (0-100)
            val toneGenVolume = (volume * 100).toInt().coerceIn(0, 100)

            val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, toneGenVolume)

            for ((freq, duration) in frequencies) {
                // Aproximar frecuencia a ToneGenerator tones (limitado)
                val tone = freqToTone(freq)
                toneGen.startTone(tone, duration)
                Thread.sleep((duration + 50).toLong()) // Pequeña pausa entre tonos
            }

            Thread.sleep(200)
            toneGen.release()
        } catch (e: Exception) {
            Timber.e(e, "Error reproduciendo sonido sintético")
        }
    }

    /**
     * Convierte una frecuencia aproximada a una de las constantes de ToneGenerator.
     */
    private fun freqToTone(freq: Int): Int = when {
        freq < 300 -> android.media.ToneGenerator.TONE_DTMF_0
        freq < 700 -> android.media.ToneGenerator.TONE_DTMF_1
        freq < 1000 -> android.media.ToneGenerator.TONE_DTMF_2
        freq < 1500 -> android.media.ToneGenerator.TONE_DTMF_3
        freq < 2000 -> android.media.ToneGenerator.TONE_DTMF_4
        else -> android.media.ToneGenerator.TONE_DTMF_5
    }
}