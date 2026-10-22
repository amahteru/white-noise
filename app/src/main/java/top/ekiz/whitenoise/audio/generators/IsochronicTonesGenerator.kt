package top.ekiz.whitenoise.audio.generators

import kotlin.math.PI
import kotlin.math.sin

class IsochronicTonesGenerator : NoiseGenerator() {
    private var modPhase = 0.0
    private var carrierPhase = 0.0
    private var modInc = 0.0
    private var carrierInc = 0.0

    init {
        updateSampleRate(44100.0)
    }

    override fun updateSampleRate(sr: Double) {
        modInc = 4.0 / sr
        carrierInc = 2.0 * PI * 200.0 / sr
    }

    override fun reset() {
        modPhase = 0.0
        carrierPhase = 0.0
    }

    override fun process(whiteL: Float, whiteR: Float) {
        modPhase += modInc
        if (modPhase >= 1.0) modPhase -= 1.0

        carrierPhase += carrierInc
        if (carrierPhase > 2.0 * PI) carrierPhase -= 2.0 * PI

        val envelope = smoothWave(modPhase)

        val output = (sin(carrierPhase) * 0.5 * envelope).toFloat()

        outL = output
        outR = output
    }
}
