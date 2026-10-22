package top.ekiz.whitenoise.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt
import top.ekiz.whitenoise.ui.components.SliderSettingRow
import top.ekiz.whitenoise.ui.theme.WhiteNoiseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    volume: Float,
    onVolumeChanged: (Float) -> Unit,
    balance: Float,
    onBalanceChanged: (Float) -> Unit,
    sleepTimer: Int,
    onSleepTimerChanged: (Int) -> Unit,
    isTimerRunning: Boolean,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onCancelTimer: () -> Unit,
    remainingTimeMillis: Long,
    totalTimeMillis: Long,
    themeMode: String,
    onThemeModeChanged: (String) -> Unit,
    isSpatialAudioEnabled: Boolean,
    onSpatialAudioEnabledChanged: (Boolean) -> Unit
) {
    val transparentItem = ListItemDefaults.colors(containerColor = Color.Transparent)

    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SettingsSection(title = "音频控制") {
            SliderSettingRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "音量大小: ${(volume * 100).roundToInt()}%",
                value = volume,
                onValueChange = onVolumeChanged,
                valueRange = 0f..1f
            )

            val balancePercent = (abs(balance) * 100).roundToInt()
            SliderSettingRow(
                icon = Icons.Filled.Tune,
                title =
                    when {
                        balancePercent == 0 -> "左右耳平衡: 居中"
                        balance < 0f -> "左右耳平衡: 偏左 $balancePercent%"
                        else -> "左右耳平衡: 偏右 $balancePercent%"
                    },
                value = balance,
                onValueChange = onBalanceChanged,
                valueRange = -1f..1f,
                steps = 9
            )
        }

        SettingsSection(title = "定时关闭") {
            var showTimePicker by remember { mutableStateOf(false) }

            SliderSettingRow(
                icon = Icons.Filled.Timer,
                title = "倒计时长: $sleepTimer 分钟",
                value = sleepTimer.coerceAtMost(120).toFloat(),
                onValueChange = { onSleepTimerChanged(it.roundToInt()) },
                valueRange = 0f..120f,
                steps = 11
            )

            val calendar = Calendar.getInstance()
            if (isTimerRunning || remainingTimeMillis > 0L) {
                calendar.timeInMillis = System.currentTimeMillis() + remainingTimeMillis
            } else {
                calendar.timeInMillis = System.currentTimeMillis() + sleepTimer * 60000L
            }
            val targetHour = calendar.get(Calendar.HOUR_OF_DAY)
            val targetMinute = calendar.get(Calendar.MINUTE)

            ListItem(
                colors = transparentItem,
                headlineContent = { Text("指定时间关闭") },
                supportingContent = {
                    Text("将在 %02d:%02d 自动关闭".format(targetHour, targetMinute))
                },
                leadingContent = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                trailingContent = {
                    OutlinedButton(onClick = { showTimePicker = true }) { Text("设定时间") }
                }
            )

            if (showTimePicker) {
                val timePickerState =
                    rememberTimePickerState(
                        initialHour = targetHour,
                        initialMinute = targetMinute,
                        is24Hour = true
                    )
                var displayMode by remember { mutableStateOf(TimePickerDisplayMode.Picker) }
                TimePickerDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { TimePickerDialogDefaults.Title(displayMode = displayMode) },
                    modeToggleButton = {
                        TimePickerDialogDefaults.DisplayModeToggle(
                            onDisplayModeChange = {
                                displayMode =
                                    if (displayMode == TimePickerDisplayMode.Picker) {
                                        TimePickerDisplayMode.Input
                                    } else {
                                        TimePickerDisplayMode.Picker
                                    }
                            },
                            displayMode = displayMode
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val now = Calendar.getInstance()
                                val selected =
                                    Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                        set(Calendar.MINUTE, timePickerState.minute)
                                        set(Calendar.SECOND, 0)
                                    }
                                if (selected.before(now)) {
                                    selected.add(Calendar.DAY_OF_YEAR, 1)
                                }
                                val diffMinutes =
                                    ((selected.timeInMillis - now.timeInMillis) / 60000L)
                                        .toInt()
                                        .coerceAtLeast(1)
                                onSleepTimerChanged(diffMinutes)
                                showTimePicker = false
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text("取消") }
                    }
                ) {
                    if (displayMode == TimePickerDisplayMode.Picker) {
                        TimePicker(state = timePickerState)
                    } else {
                        TimeInput(state = timePickerState)
                    }
                }
            }

            if (isTimerRunning || remainingTimeMillis > 0L) {
                val totalForProgress =
                    if (totalTimeMillis > 0L) totalTimeMillis else remainingTimeMillis
                val progress =
                    if (totalForProgress > 0) {
                        (remainingTimeMillis.toFloat() / totalForProgress.toFloat())
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                val remainingMinutes = remainingTimeMillis / 60000
                val remainingSeconds = (remainingTimeMillis % 60000) / 1000
                ListItem(
                    colors = transparentItem,
                    headlineContent = {
                        Text("剩余时间: %02d:%02d".format(remainingMinutes, remainingSeconds))
                    },
                    trailingContent = { Text("${(progress * 100).toInt()}%") },
                    supportingContent = {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                )
            }

            val isPaused = !isTimerRunning && remainingTimeMillis > 0L
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isTimerRunning) {
                    OutlinedButton(onClick = onPauseTimer, modifier = Modifier.weight(1f)) {
                        Text("暂停")
                    }
                } else {
                    Button(
                        onClick = onStartTimer,
                        enabled = sleepTimer > 0 || isPaused,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isPaused) "继续" else "开始")
                    }
                    if (isPaused) {
                        OutlinedButton(onClick = onCancelTimer, modifier = Modifier.weight(1f)) {
                            Text("取消")
                        }
                    }
                }
            }
        }

        SettingsSection(title = "高级功能") {
            ListItem(
                modifier =
                    Modifier.toggleable(
                        value = isSpatialAudioEnabled,
                        role = Role.Switch,
                        onValueChange = onSpatialAudioEnabledChanged
                    ),
                colors = transparentItem,
                headlineContent = { Text("3D 空间音频 (双耳渲染)") },
                leadingContent = { Icon(Icons.Filled.SurroundSound, contentDescription = null) },
                trailingContent = {
                    Switch(checked = isSpatialAudioEnabled, onCheckedChange = null)
                }
            )
        }

        SettingsSection(title = "外观与显示") {
            val options = listOf("System" to "跟随系统", "Light" to "浅色", "Dark" to "深色")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                options.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChanged(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Card(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 1100)
@Preview(
    name = "Dark",
    showBackground = true,
    heightDp = 1100,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsScreenPreview() {
    WhiteNoiseTheme {
        Surface {
            SettingsScreen(
                innerPadding = PaddingValues(),
                volume = 0.6f,
                onVolumeChanged = {},
                balance = -0.2f,
                onBalanceChanged = {},
                sleepTimer = 30,
                onSleepTimerChanged = {},
                isTimerRunning = true,
                onStartTimer = {},
                onPauseTimer = {},
                onCancelTimer = {},
                remainingTimeMillis = 12 * 60000L + 34000L,
                totalTimeMillis = 30 * 60000L,
                themeMode = "System",
                onThemeModeChanged = {},
                isSpatialAudioEnabled = true,
                onSpatialAudioEnabledChanged = {}
            )
        }
    }
}
