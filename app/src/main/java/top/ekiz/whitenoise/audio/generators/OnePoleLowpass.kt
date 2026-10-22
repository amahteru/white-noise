package top.ekiz.whitenoise.audio.generators

internal class OnePoleLowpass(private val pole: Double, private val gain: Double) {
    var state = 0.0
        private set

    fun reset() {
        state = 0.0
    }

    fun process(x: Double): Double {
        state = state * pole + x * gain
        return state
    }
}

/** Integrator used by brown noise. */
internal fun brownLowpass() = OnePoleLowpass(0.98039, 0.01961)

/** Slower integrator used by deep brown noise. */
internal fun deepBrownLowpass() = OnePoleLowpass(0.9901, 0.0099)
