package top.ekiz.whitenoise.audio.generators

class DeepBrownNoiseGenerator : NoiseGenerator() {
    private val deepBrownL = deepBrownLowpass()
    private val deepBrownR = deepBrownLowpass()

    override fun reset() {
        deepBrownL.reset()
        deepBrownR.reset()
    }

    override fun process(whiteL: Float, whiteR: Float) {
        outL = (deepBrownL.process(whiteL.toDouble()) * 5.0).toFloat()
        outR = (deepBrownR.process(whiteR.toDouble()) * 5.0).toFloat()
    }
}
