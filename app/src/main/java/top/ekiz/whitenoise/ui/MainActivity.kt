package top.ekiz.whitenoise.ui

import android.app.UiModeManager
import android.content.ComponentName
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import top.ekiz.whitenoise.service.NoiseService
import top.ekiz.whitenoise.ui.screens.MainAppScreen
import top.ekiz.whitenoise.ui.theme.WhiteNoiseTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: NoiseViewModel by viewModels()
    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, NoiseService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                val mediaController =
                    try {
                        controllerFuture?.get()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to connect to MediaController", e)
                        return@addListener
                    }
                viewModel.setMediaController(mediaController)
                mediaController?.addListener(
                    object : androidx.media3.common.Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            viewModel.updatePlaybackState()
                        }
                    }
                )
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onStop() {
        super.onStop()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            if (!uiState.isLoading) {
                LaunchedEffect(uiState.themeMode) {
                    getSystemService(UiModeManager::class.java)
                        .setApplicationNightMode(
                            when (uiState.themeMode) {
                                "Dark" -> UiModeManager.MODE_NIGHT_YES
                                "Light" -> UiModeManager.MODE_NIGHT_NO
                                else -> UiModeManager.MODE_NIGHT_AUTO
                            }
                        )
                }
            }

            val darkTheme =
                when (uiState.themeMode) {
                    "Dark" -> true
                    "Light" -> false
                    else -> isSystemInDarkTheme()
                }

            DisposableEffect(darkTheme) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            WhiteNoiseTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScreen(uiState = uiState, viewModel = viewModel)
                }
            }
        }

        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (viewModel.uiState.value.isLoading) return false
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            }
        )
    }
}
