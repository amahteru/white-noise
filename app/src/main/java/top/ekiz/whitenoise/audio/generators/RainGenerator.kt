package top.ekiz.whitenoise.audio.generators

import kotlin.math.PI

class RainGenerator : NoiseGenerator() {

    private val pinkFilterL = PinkFilter()
    private val pinkFilterR = PinkFilter()

    private var lp1L = 0.0
    private var lp2L = 0.0
    private var lp1R = 0.0
    private var lp2R = 0.0

    private var hp1L = 0.0
    private var hp2L = 0.0
    private var hp1R = 0.0
    private var hp2R = 0.0

    private var gustLfoPhase = 0.0
    private var piOverSr = 0.0
    private var gustLfoInc = 0.0

    init {
        updateSampleRate(44100.0)
    }

    override fun updateSampleRate(sr: Double) {
        piOverSr = PI / sr

        gustLfoInc = 0.02 / sr
    }

    override fun reset() {
        pinkFilterL.reset()
        pinkFilterR.reset()

        lp1L = 0.0
        lp2L = 0.0
        lp1R = 0.0
        lp2R = 0.0
        hp1L = 0.0
        hp2L = 0.0
        hp1R = 0.0
        hp2R = 0.0

        gustLfoPhase = 0.0
    }

    override fun process(whiteL: Float, whiteR: Float) {
        val wL = whiteL.toDouble()
        val wR = whiteR.toDouble()

        val pinkL = pinkFilterL.process(wL) * 0.11
        val pinkR = pinkFilterR.process(wR) * 0.11

        gustLfoPhase += gustLfoInc
        if (gustLfoPhase >= 1.0) gustLfoPhase -= 1.0
        val gust = smoothWave(gustLfoPhase)

        val lpCutoff = 800.0 + gust * 500.0
        val fLp = 2.0 * lpCutoff * piOverSr

        val hpL_state = pinkL - lp1L - 1.2 * lp2L
        lp2L += fLp * hpL_state
        lp1L += fLp * lp2L
        val rumbleL = lp1L

        val hpR_state = pinkR - lp1R - 1.2 * lp2R
        lp2R += fLp * hpR_state
        lp1R += fLp * lp2R
        val rumbleR = lp1R

        val hpCutoff = 2500.0 - gust * 400.0
        val fHp = 2.0 * hpCutoff * piOverSr

        val hpL_hstate = pinkL - hp1L - 1.2 * hp2L
        hp2L += fHp * hpL_hstate
        hp1L += fHp * hp2L
        val sizzleL = hpL_hstate

        val hpR_hstate = pinkR - hp1R - 1.2 * hp2R
        hp2R += fHp * hpR_hstate
        hp1R += fHp * hp2R
        val sizzleR = hpR_hstate

        val outVolumeL = rumbleL * 0.6 + sizzleL * 0.15
        val outVolumeR = rumbleR * 0.6 + sizzleR * 0.15

        val swell = 0.85 + gust * 0.15

        outL = (outVolumeL * swell).toFloat().coerceIn(-1.0f, 1.0f)
        outR = (outVolumeR * swell).toFloat().coerceIn(-1.0f, 1.0f)
    }
}
