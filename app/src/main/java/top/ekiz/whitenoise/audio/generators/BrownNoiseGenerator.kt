package top.ekiz.whitenoise.audio.generators

class BrownNoiseGenerator : NoiseGenerator() {
    private val brownL = brownLowpass()
    private val brownR = brownLowpass()

    override fun reset() {
        brownL.reset()
        brownR.reset()
    }

    override fun process(whiteL: Float, whiteR: Float) {
        outL = (brownL.process(whiteL.toDouble()) * 3.5).toFloat()
        outR = (brownR.process(whiteR.toDouble()) * 3.5).toFloat()
    }
}
