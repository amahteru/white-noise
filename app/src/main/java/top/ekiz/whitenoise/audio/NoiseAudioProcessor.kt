package top.ekiz.whitenoise.audio

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tanh
import top.ekiz.whitenoise.audio.generators.NoiseGenerator
import top.ekiz.whitenoise.audio.generators.NoiseGeneratorFactory
import top.ekiz.whitenoise.domain.NoiseType

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class NoiseAudioProcessor : BaseAudioProcessor() {
    @Volatile var volume: Float = 0.5f
    @Volatile var balance: Float = 0f
    @Volatile var noiseType: NoiseType = NoiseType.WHITE
    @Volatile var isSpatialAudioEnabled: Boolean = false
    @Volatile var forceImmediateSwitch: Boolean = false

    private class FadingVoice(val generator: NoiseGenerator, var baseGain: Float)

    private var currentNoiseType = noiseType

    private var currentGenerator: NoiseGenerator = NoiseGeneratorFactory.create(noiseType)

    // Each fading voice plays at baseGain * (1 - crossfadeProgress); the bases sum to 1, so a
    // switch in the middle of a crossfade keeps every voice's gain continuous.
    private val fadingVoices = ArrayList<FadingVoice>()

    fun initialize(type: NoiseType) {
        noiseType = type
        switchImmediately(type)
    }

    private fun createGenerator(type: NoiseType): NoiseGenerator {
        val generator = NoiseGeneratorFactory.create(type)
        if (inputAudioFormat.sampleRate > 0) {
            generator.updateSampleRate(inputAudioFormat.sampleRate.toDouble())
        }
        return generator
    }

    private fun switchImmediately(type: NoiseType) {
        currentNoiseType = type
        fadingVoices.clear()
        crossfadeProgress = 1f
        currentGenerator = createGenerator(type)
        forceImmediateSwitch = false
    }

    private fun startCrossfade(type: NoiseType) {
        val remaining = 1f - crossfadeProgress
        for (voice in fadingVoices) voice.baseGain *= remaining
        fadingVoices.add(FadingVoice(currentGenerator, crossfadeProgress))
        currentGenerator = createGenerator(type)
        currentNoiseType = type
        crossfadeProgress = 0f
    }

    private var crossfadeProgress = 1f
    private var crossfadeStep = 1f / (44100f * 1.5f)

    private var lfoPhase: Double = 0.0
    private var lfoStep: Double = 2.0 * Math.PI * 0.05 / 44100.0
    private var headShadowL = 0f
    private var headShadowR = 0f

    // Per-sample smoothing so volume, balance and spatial changes never step mid-waveform.
    private var smoothing = smoothingCoeff(44100)
    private var gainL = 0f
    private var gainR = 0f
    private var spatialMix = 0f

    private var rngState: Long = System.nanoTime().let { if (it == 0L) 1L else it }

    override fun onConfigure(
        inputAudioFormat: AudioProcessor.AudioFormat
    ): AudioProcessor.AudioFormat {
        val sr = inputAudioFormat.sampleRate
        if (sr > 0) {
            crossfadeStep = 1f / (sr.toFloat() * 1.5f)
            lfoStep = 2.0 * Math.PI * 0.05 / sr.toDouble()
            smoothing = smoothingCoeff(sr)
            currentGenerator.updateSampleRate(sr.toDouble())
            for (voice in fadingVoices) voice.generator.updateSampleRate(sr.toDouble())
        }
        return inputAudioFormat
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        currentGenerator.reset()
        for (voice in fadingVoices) voice.generator.reset()
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val buffer = replaceOutputBuffer(remaining)

        val currentVolume = volume
        val currentBalance = balance
        val isStereo = inputAudioFormat.channelCount == 2
        val targetGainL = (1f - max(0f, currentBalance)) * currentVolume
        val targetGainR = (1f + min(0f, currentBalance)) * currentVolume
        val targetSpatial = if (isSpatialAudioEnabled && isStereo) 1f else 0f

        val numShorts = remaining / 2

        inputBuffer.position(inputBuffer.position() + remaining)

        if (currentNoiseType != noiseType) {
            if (forceImmediateSwitch) {
                switchImmediately(noiseType)
            } else {
                startCrossfade(noiseType)
            }
        } else {
            forceImmediateSwitch = false
        }

        var i = 0
        while (i < numShorts) {
            rngState = rngState xor (rngState shl 13)
            rngState = rngState xor (rngState ushr 7)
            rngState = rngState xor (rngState shl 17)
            val centerWhite = (rngState ushr 40).toFloat() * 1.1920929E-7f - 1f

            rngState = rngState xor (rngState shl 13)
            rngState = rngState xor (rngState ushr 7)
            rngState = rngState xor (rngState shl 17)
            val sideWhiteL = (rngState ushr 40).toFloat() * 1.1920929E-7f - 1f

            val sideWhiteR =
                if (isStereo) {
                    rngState = rngState xor (rngState shl 13)
                    rngState = rngState xor (rngState ushr 7)
                    rngState = rngState xor (rngState shl 17)
                    (rngState ushr 40).toFloat() * 1.1920929E-7f - 1f
                } else {
                    sideWhiteL
                }

            val whiteL = centerWhite * 0.8f + sideWhiteL * 0.6f
            val whiteR = centerWhite * 0.8f + sideWhiteR * 0.6f

            currentGenerator.process(whiteL, whiteR)
            var outputL = currentGenerator.outL
            var outputR = currentGenerator.outR

            if (crossfadeProgress < 1f) {
                crossfadeProgress += crossfadeStep
                if (crossfadeProgress >= 1f) {
                    crossfadeProgress = 1f
                    fadingVoices.clear()
                } else {
                    val fadeOutGain = 1f - crossfadeProgress
                    outputL *= crossfadeProgress
                    outputR *= crossfadeProgress
                    for (voice in fadingVoices) {
                        voice.generator.process(whiteL, whiteR)
                        val gain = voice.baseGain * fadeOutGain
                        outputL += voice.generator.outL * gain
                        outputR += voice.generator.outR * gain
                    }
                }
            }

            spatialMix += (targetSpatial - spatialMix) * smoothing
            if (isStereo && (spatialMix > 1e-4f || targetSpatial > 0f)) {
                lfoPhase += lfoStep
                if (lfoPhase > Math.PI * 2.0) {
                    lfoPhase -= Math.PI * 2.0
                }

                val lfoVal = kotlin.math.sin(lfoPhase).toFloat()

                val levelL = 1.0f - max(0f, lfoVal * 0.25f)
                val levelR = 1.0f - max(0f, -lfoVal * 0.25f)

                val lpfCoeffL = 1.0f - max(0f, lfoVal * 0.4f)
                headShadowL = headShadowL + lpfCoeffL * (outputL - headShadowL)

                val lpfCoeffR = 1.0f - max(0f, -lfoVal * 0.4f)
                headShadowR = headShadowR + lpfCoeffR * (outputR - headShadowR)

                outputL += (headShadowL * levelL - outputL) * spatialMix
                outputR += (headShadowR * levelR - outputR) * spatialMix
            } else {
                headShadowL = outputL
                headShadowR = outputR
            }

            gainL += (targetGainL - gainL) * smoothing
            gainR += (targetGainR - gainR) * smoothing

            val sampleL = (softLimit(outputL) * gainL * Short.MAX_VALUE).toInt().toShort()
            buffer.putShort(sampleL)
            i++

            if (isStereo) {
                val sampleR = (softLimit(outputR) * gainR * Short.MAX_VALUE).toInt().toShort()
                buffer.putShort(sampleR)
                i++
            }
        }

        buffer.flip()
    }

    private companion object {
        const val LIMIT_KNEE = 0.8f

        fun smoothingCoeff(sampleRate: Int): Float =
            (1.0 - exp(-1.0 / (0.02 * sampleRate))).toFloat()

        // Transparent below the knee, then saturates smoothly towards ±1 instead of hard clipping.
        fun softLimit(x: Float): Float {
            val magnitude = abs(x)
            if (magnitude <= LIMIT_KNEE) return x
            val headroom = 1f - LIMIT_KNEE
            val limited = LIMIT_KNEE + headroom * tanh((magnitude - LIMIT_KNEE) / headroom)
            return if (x < 0f) -limited else limited
        }
    }
}
