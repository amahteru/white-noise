package top.ekiz.whitenoise.audio

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min
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

    private var currentNoiseType = noiseType

    private var currentGenerator: NoiseGenerator = NoiseGeneratorFactory.create(noiseType)
    private var fadingGenerator: NoiseGenerator? = null

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
        fadingGenerator = null
        crossfadeProgress = 1f
        currentGenerator = createGenerator(type)
        forceImmediateSwitch = false
    }

    private var crossfadeProgress = 1f
    private var crossfadeStep = 1f / (44100f * 1.5f)

    private var lfoPhase: Double = 0.0
    private var lfoStep: Double = 2.0 * Math.PI * 0.05 / 44100.0
    private var headShadowL = 0f
    private var headShadowR = 0f

    private var rngState: Long = System.nanoTime().let { if (it == 0L) 1L else it }

    override fun onConfigure(
        inputAudioFormat: AudioProcessor.AudioFormat
    ): AudioProcessor.AudioFormat {
        val sr = inputAudioFormat.sampleRate
        if (sr > 0) {
            crossfadeStep = 1f / (sr.toFloat() * 1.5f)
            lfoStep = 2.0 * Math.PI * 0.05 / sr.toDouble()
            currentGenerator.updateSampleRate(sr.toDouble())
            fadingGenerator?.updateSampleRate(sr.toDouble())
        }
        return inputAudioFormat
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        currentGenerator.reset()
        fadingGenerator?.reset()
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val buffer = replaceOutputBuffer(remaining)

        val currentVolume = volume
        val currentBalance = balance
        val leftVol = 1f - max(0f, currentBalance)
        val rightVol = 1f + min(0f, currentBalance)

        val isStereo = inputAudioFormat.channelCount == 2
        val numShorts = remaining / 2

        inputBuffer.position(inputBuffer.position() + remaining)

        if (currentNoiseType != noiseType) {
            if (forceImmediateSwitch) {
                switchImmediately(noiseType)
            } else {
                fadingGenerator = currentGenerator
                currentGenerator = createGenerator(noiseType)
                currentNoiseType = noiseType
                crossfadeProgress = 0f
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
                    fadingGenerator = null
                } else {
                    val fadeGen = fadingGenerator
                    if (fadeGen != null) {
                        fadeGen.process(whiteL, whiteR)

                        val fadeInGain = crossfadeProgress
                        val fadeOutGain = 1f - crossfadeProgress

                        outputL = outputL * fadeInGain + fadeGen.outL * fadeOutGain
                        outputR = outputR * fadeInGain + fadeGen.outR * fadeOutGain
                    }
                }
            }

            if (isSpatialAudioEnabled && isStereo) {
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

                outputL = headShadowL * levelL
                outputR = headShadowR * levelR
            }

            if (outputL > 1f) outputL = 1f
            if (outputL < -1f) outputL = -1f
            if (outputR > 1f) outputR = 1f
            if (outputR < -1f) outputR = -1f

            val sampleL = (outputL * leftVol * Short.MAX_VALUE * currentVolume).toInt().toShort()
            buffer.putShort(sampleL)
            i++

            if (isStereo) {
                val sampleR =
                    (outputR * rightVol * Short.MAX_VALUE * currentVolume).toInt().toShort()
                buffer.putShort(sampleR)
                i++
            }
        }

        buffer.flip()
    }
}
