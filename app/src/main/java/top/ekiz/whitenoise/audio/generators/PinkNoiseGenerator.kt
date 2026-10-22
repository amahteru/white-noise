package top.ekiz.whitenoise.audio.generators

class PinkNoiseGenerator : NoiseGenerator() {
    private val pinkL = PinkFilter()
    private val pinkR = PinkFilter()

    override fun reset() {
        pinkL.reset()
        pinkR.reset()
    }

    override fun process(whiteL: Float, whiteR: Float) {
        outL = (pinkL.process(whiteL.toDouble()) * 0.11).toFloat()
        outR = (pinkR.process(whiteR.toDouble()) * 0.11).toFloat()
    }
}
