package top.ekiz.whitenoise.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import top.ekiz.whitenoise.R
import top.ekiz.whitenoise.domain.NoiseType
import top.ekiz.whitenoise.ui.NoiseUiState
import top.ekiz.whitenoise.ui.NoiseViewModel

private val colorNoises =
    listOf(
        NoiseType.BLACK to "黑噪音",
        NoiseType.DEEP_BROWN to "深棕噪音",
        NoiseType.BROWN to "棕噪音",
        NoiseType.PINK to "粉噪音",
        NoiseType.WHITE to "白噪音",
        NoiseType.BLUE to "蓝噪音",
        NoiseType.VIOLET to "紫噪音",
        NoiseType.GREEN to "绿噪音",
        NoiseType.GREY to "灰噪音"
    )

private val otherNoises =
    listOf(
        NoiseType.VELVET to "天鹅绒噪音",
        NoiseType.OCEAN_WAVES to "海浪声",
        NoiseType.BINAURAL_BEATS to "双耳节拍",
        NoiseType.ISOCHRONIC_TONES to "等时音",
        NoiseType.SOLFEGGIO_FREQUENCIES to "冥想频率",
        NoiseType.WOMB_SOUNDS to "子宫环境",
        NoiseType.WIND to "风声",
        NoiseType.AIRPLANE_CABIN to "飞机机舱",
        NoiseType.HEARTBEAT to "心跳声",
        NoiseType.RAIN to "雨声",
        NoiseType.CAMPFIRE to "营火声"
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(uiState: NoiseUiState, viewModel: NoiseViewModel) {
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasNotificationPermission = isGranted
                viewModel.togglePlayPause()
            }
        )

    fun onPlayPauseClicked() {
        if (!hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        viewModel.togglePlayPause()
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            0 -> "颜色噪音"
                            1 -> "其他声音"
                            else -> "设置"
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 1) {
                FloatingActionButton(onClick = { onPlayPauseClicked() }) {
                    Icon(
                        painter =
                            painterResource(
                                if (uiState.isPlaying) R.drawable.ic_pause
                                else R.drawable.ic_play_arrow
                            ),
                        contentDescription = if (uiState.isPlaying) "暂停" else "播放"
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.ic_palette), contentDescription = "颜色") },
                    label = { Text("颜色") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.ic_forest), contentDescription = "其他") },
                    label = { Text("其他") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.ic_settings), contentDescription = "设置") },
                    label = { Text("设置") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        if (selectedTab == 0 || selectedTab == 1) {
            SoundsScreen(
                innerPadding = paddingValues,
                currentType = uiState.noiseType,
                noises = if (selectedTab == 0) colorNoises else otherNoises,
                onTypeSelected = { viewModel.setNoiseType(it) }
            )
        } else {
            SettingsScreen(
                innerPadding = paddingValues,
                volume = uiState.volume,
                onVolumePreview = { viewModel.previewVolume(it) },
                onVolumeChanged = { viewModel.setVolume(it) },
                balance = uiState.balance,
                onBalancePreview = { viewModel.previewBalance(it) },
                onBalanceChanged = { viewModel.setBalance(it) },
                sleepTimer = uiState.sleepTimerMinutes,
                onSleepTimerChanged = { viewModel.setSleepTimerMinutes(it) },
                isTimerRunning = uiState.isTimerRunning,
                onStartTimer = { viewModel.startTimer() },
                onPauseTimer = { viewModel.pauseTimer() },
                onCancelTimer = { viewModel.cancelTimer() },
                remainingTimeMillis = uiState.activeRemainingMillis,
                totalTimeMillis = uiState.totalTimerMillis,
                themeMode = uiState.themeMode,
                onThemeModeChanged = { viewModel.setThemeMode(it) },
                isSpatialAudioEnabled = uiState.isSpatialAudioEnabled,
                onSpatialAudioEnabledChanged = { viewModel.setSpatialAudioEnabled(it) }
            )
        }
    }
}
