package top.ekiz.whitenoise.audio.generators

class BlueNoiseGenerator : NoiseGenerator() {
    private val pinkL = PinkFilter()
    private val pinkR = PinkFilter()
    private var lastPinkL = 0.0
    private var lastPinkR = 0.0

    override fun reset() {
        pinkL.reset()
        pinkR.reset()
        lastPinkL = 0.0
        lastPinkR = 0.0
    }

    override fun process(whiteL: Float, whiteR: Float) {
        val pL = pinkL.process(whiteL.toDouble())
        outL = ((pL - lastPinkL) * 2.0).toFloat()
        lastPinkL = pL

        val pR = pinkR.process(whiteR.toDouble())
        outR = ((pR - lastPinkR) * 2.0).toFloat()
        lastPinkR = pR
    }
}
