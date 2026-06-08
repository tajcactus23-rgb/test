package com.bluemeanie.axonscanner.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.bluemeanie.axonscanner.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private var axonAlertSound: Int = 0
    private var scanStartSound: Int = 0
    private var pingSound: Int = 0
    
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        // Note: In production, you would load actual sound files from res/raw
        // For now, we'll use programmatic sound generation
    }

    fun playAxonAlert() {
        // Generate alert tones programmatically
        playAlertTones(listOf(880, 1100, 1320, 1540, 1760, 1980, 2200), 100)
    }

    fun playScanStart() {
        playAlertTones(listOf(440, 554, 659), 150)
    }

    fun playScanStop() {
        playAlertTones(listOf(659, 554, 440), 150)
    }

    fun playPing() {
        playAlertTones(listOf(880), 50)
    }

    fun playThreatAlert() {
        playAlertTones(listOf(220, 330, 440, 330, 220, 440, 330, 220), 80)
    }

    private fun playAlertTones(frequencies: List<Int>, durationMs: Long) {
        // For a production app, you would use SoundPool with actual audio files
        // This is a placeholder that demonstrates the API structure
        try {
            // In a real implementation, you would:
            // 1. Generate sine waves at the specified frequencies
            // 2. Play them through AudioTrack
            // 3. Or pre-record the sounds and load them into SoundPool
        } catch (e: Exception) {
            // Ignore audio errors
        }
    }

    fun vibrate(pattern: VibrationPattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (pattern) {
                VibrationPattern.AXON -> createAxonPattern()
                VibrationPattern.THREAT -> createThreatPattern()
                VibrationPattern.START -> createStartPattern()
                VibrationPattern.PING -> createPingPattern()
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val patternArray = when (pattern) {
                VibrationPattern.AXON -> longArrayOf(0, 100, 100, 200, 100, 100, 100, 200, 100, 100, 100, 200, 100, 100)
                VibrationPattern.THREAT -> longArrayOf(0, 200, 100, 150, 100, 150, 100, 150, 100, 150, 100, 150, 100)
                VibrationPattern.START -> longArrayOf(0, 300, 200, 300)
                VibrationPattern.PING -> longArrayOf(0, 50)
            }
            @Suppress("DEPRECATION")
            vibrator.vibrate(patternArray, -1)
        }
    }

    private fun createAxonPattern(): VibrationEffect {
        // 11-pulse 2.5s sequence
        val timings = longArrayOf(
            0, 80, 60, 120, 80, 60, 120, 80, 60, 120, 80, 60, 120, 80, 60, 120, 80, 60, 120, 80, 60, 120, 80
        )
        val amplitudes = intArrayOf(
            0, 255, 0, 255, 0, 200, 0, 255, 0, 200, 0, 255, 0, 200, 0, 255, 0, 200, 0, 255, 0, 200, 0
        )
        return VibrationEffect.createWaveform(timings, amplitudes, -1)
    }

    private fun createThreatPattern(): VibrationEffect {
        // 13-pulse 3s heavy sequence
        val timings = longArrayOf(
            0, 150, 80, 100, 80, 100, 80, 100, 80, 100, 80, 100, 80, 100, 80, 100, 80, 100, 80, 100, 80, 100, 80, 100, 80
        )
        val amplitudes = intArrayOf(
            0, 255, 0, 220, 0, 200, 0, 220, 0, 200, 0, 220, 0, 200, 0, 220, 0, 200, 0, 220, 0, 200, 0, 220, 0
        )
        return VibrationEffect.createWaveform(timings, amplitudes, -1)
    }

    private fun createStartPattern(): VibrationEffect {
        val timings = longArrayOf(0, 200, 150, 300)
        val amplitudes = intArrayOf(0, 255, 0, 200)
        return VibrationEffect.createWaveform(timings, amplitudes, -1)
    }

    private fun createPingPattern(): VibrationEffect {
        return VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}

enum class VibrationPattern {
    AXON,
    THREAT,
    START,
    PING
}