package top.ekiz.whitenoise.audio.generators

internal fun smoothWave(phase: Double): Double {
    val tri = if (phase < 0.5) phase * 2.0 else 2.0 - phase * 2.0
    return tri * tri * (3.0 - 2.0 * tri)
}
