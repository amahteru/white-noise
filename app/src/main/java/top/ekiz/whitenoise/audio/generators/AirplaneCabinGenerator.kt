package top.ekiz.whitenoise.audio.generators

import kotlin.math.PI
import kotlin.math.sin

class AirplaneCabinGenerator : NoiseGenerator() {
    private val deepBrownL = deepBrownLowpass()
    private val deepBrownR = deepBrownLowpass()
    private var cabinPhase = 0.0
    private var phaseInc = 0.0

    init {
        updateSampleRate(44100.0)
    }

    override fun updateSampleRate(sr: Double) {
        phaseInc = 2.0 * PI * 160.0 / sr
    }

    override fun reset() {
        deepBrownL.reset()
        deepBrownR.reset()
        cabinPhase = 0.0
    }

    override fun process(whiteL: Float, whiteR: Float) {
        val brownL = deepBrownL.process(whiteL.toDouble())
        val brownR = deepBrownR.process(whiteR.toDouble())

        cabinPhase += phaseInc
        if (cabinPhase > 2.0 * PI) cabinPhase -= 2.0 * PI

        val drone = sin(cabinPhase) * 0.2

        outL = (brownL * 4.0 + drone).toFloat()
        outR = (brownR * 4.0 + drone).toFloat()
    }
}
