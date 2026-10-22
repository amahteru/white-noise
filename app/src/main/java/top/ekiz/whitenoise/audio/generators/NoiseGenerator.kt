package top.ekiz.whitenoise.audio.generators

abstract class NoiseGenerator {
    var outL: Float = 0f
    var outR: Float = 0f

    open fun updateSampleRate(sr: Double) {}

    abstract fun reset()

    abstract fun process(whiteL: Float, whiteR: Float)
}
