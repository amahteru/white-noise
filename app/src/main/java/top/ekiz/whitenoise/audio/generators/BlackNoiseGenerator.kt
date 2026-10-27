package top.ekiz.whitenoise.audio.generators

class BlackNoiseGenerator : NoiseGenerator() {
    private val brownL = brownLowpass()
    private val blackL = deepBrownLowpass()
    private val brownR = brownLowpass()
    private val blackR = deepBrownLowpass()

    override fun reset() {
        brownL.reset()
        blackL.reset()
        brownR.reset()
        blackR.reset()
    }

    override fun process(whiteL: Float, whiteR: Float) {
        outL = (blackL.process(brownL.process(whiteL.toDouble())) * 5.5).toFloat()
        outR = (blackR.process(brownR.process(whiteR.toDouble())) * 5.5).toFloat()
    }
}
