package top.ekiz.whitenoise.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import kotlin.math.roundToInt
import top.ekiz.whitenoise.R
import top.ekiz.whitenoise.domain.DailyStopSchedule
import top.ekiz.whitenoise.ui.components.SliderSettingRow
import top.ekiz.whitenoise.ui.theme.WhiteNoiseTheme
import top.ekiz.whitenoise.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    volume: Float,
    onVolumePreview: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    balance: Float,
    onBalancePreview: (Float) -> Unit,
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
    onSpatialAudioEnabledChanged: (Boolean) -> Unit,
    dailyStop: DailyStopSchedule,
    onDailyStopChanged: (DailyStopSchedule) -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
    ) {
        Text(
            text = "音频控制",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.padding(
                    start = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.large,
                    bottom = MaterialTheme.spacing.small
                )
        )

        var volumeDraft by remember { mutableStateOf<Float?>(null) }
        LaunchedEffect(volume) { volumeDraft = null }
        SliderSettingRow(
            icon = R.drawable.ic_volume_up,
            contentDescription = "音量大小",
            value = volumeDraft ?: volume,
            onValueChange = {
                volumeDraft = it
                onVolumePreview(it)
            },
            onValueChangeFinished = { volumeDraft?.let(onVolumeChanged) },
            valueRange = 0f..1f,
            steps = 0
        )

        var balanceDraft by remember { mutableStateOf<Float?>(null) }
        LaunchedEffect(balance) { balanceDraft = null }
        SliderSettingRow(
            icon = R.drawable.ic_tune,
            contentDescription = "左右耳平衡",
            value = balanceDraft ?: balance,
            onValueChange = {
                balanceDraft = it
                onBalancePreview(it)
            },
            onValueChangeFinished = { balanceDraft?.let(onBalanceChanged) },
            valueRange = -1f..1f,
            labels = listOf("偏左", "居中", "偏右")
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium))

        Text(
            text = "定时关闭",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.padding(
                    start = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.small
                )
        )

        var showTimePicker by remember { mutableStateOf(false) }

        var sleepTimerDraft by remember { mutableStateOf<Int?>(null) }
        LaunchedEffect(sleepTimer) { sleepTimerDraft = null }
        val shownSleepTimer = sleepTimerDraft ?: sleepTimer
        SliderSettingRow(
            icon = R.drawable.ic_schedule,
            contentDescription = "倒计时长: $shownSleepTimer 分钟",
            value = shownSleepTimer.coerceAtMost(120).toFloat(),
            onValueChange = { sleepTimerDraft = it.roundToInt() },
            onValueChangeFinished = { sleepTimerDraft?.let(onSleepTimerChanged) },
            valueRange = 0f..120f,
            labels = listOf("0", "60", "120"),
            steps = 11
        )

        val calendar = Calendar.getInstance()
        if (isTimerRunning || remainingTimeMillis > 0L) {
            calendar.timeInMillis = System.currentTimeMillis() + remainingTimeMillis
        } else {
            calendar.timeInMillis = System.currentTimeMillis() + shownSleepTimer * 60000L
        }
        val targetHour = calendar.get(Calendar.HOUR_OF_DAY)
        val targetMinute = calendar.get(Calendar.MINUTE)

        ListItem(
            headlineContent = { Text("指定时间关闭") },
            supportingContent = {
                Text(
                    "将在 %02d:%02d 自动关闭".format(
                        targetHour,
                        targetMinute
                    )
                )
            },
            leadingContent = { Icon(painterResource(R.drawable.ic_schedule), contentDescription = null) },
            trailingContent = {
                OutlinedButton(onClick = { showTimePicker = true }) { Text("设定时间") }
            }
        )

        if (showTimePicker) {
            ClockTimePickerDialog(
                initialHour = targetHour,
                initialMinute = targetMinute,
                onDismiss = { showTimePicker = false },
                onConfirm = { hour, minute ->
                    val now = Calendar.getInstance()
                    val selected =
                        Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
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
            )
        }

        Column(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)) {
            val isPaused = !isTimerRunning && remainingTimeMillis > 0L

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                Button(
                    onClick = onStartTimer,
                    enabled = !isTimerRunning && (sleepTimer > 0 || remainingTimeMillis > 0L),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isPaused) "继续" else "开始")
                }
                if (isTimerRunning) {
                    OutlinedButton(onClick = onPauseTimer, modifier = Modifier.weight(1f)) {
                        Text("暂停")
                    }
                } else if (isPaused) {
                    OutlinedButton(onClick = onCancelTimer, modifier = Modifier.weight(1f)) {
                        Text("取消")
                    }
                } else {
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
                        Text("暂停")
                    }
                }
            }

            if (isTimerRunning || remainingTimeMillis > 0L) {
                val totalForProgress =
                    if (totalTimeMillis > 0L) totalTimeMillis else remainingTimeMillis
                val progress =
                    if (totalForProgress > 0)
                        (remainingTimeMillis.toFloat() / totalForProgress.toFloat()).coerceIn(
                            0f,
                            1f
                        )
                    else 0f
                val remainingMinutes = remainingTimeMillis / 60000
                val remainingSeconds = (remainingTimeMillis % 60000) / 1000
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "剩余时间: ${"%02d:%02d".format(remainingMinutes, remainingSeconds)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.small),
                    strokeCap = StrokeCap.Round
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium))

        Text(
            text = "每日定时关闭",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.padding(
                    start = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.small
                )
        )

        ListItem(
            modifier =
                Modifier.toggleable(
                    value = dailyStop.enabled,
                    role = Role.Switch,
                    onValueChange = { onDailyStopChanged(dailyStop.copy(enabled = it)) }
                ),
            headlineContent = { Text("启用每日定时关闭") },
            supportingContent = { Text(dailyStopSummary(dailyStop)) },
            leadingContent = { Icon(painterResource(R.drawable.ic_alarm), contentDescription = null) },
            trailingContent = { Switch(checked = dailyStop.enabled, onCheckedChange = null) }
        )

        var showDailyStopPicker by remember { mutableStateOf(false) }
        ListItem(
            headlineContent = { Text("关闭时间") },
            supportingContent = { Text("%02d:%02d".format(dailyStop.hour, dailyStop.minute)) },
            leadingContent = { Icon(painterResource(R.drawable.ic_schedule), contentDescription = null) },
            trailingContent = {
                OutlinedButton(onClick = { showDailyStopPicker = true }) { Text("设定时间") }
            }
        )

        if (showDailyStopPicker) {
            ClockTimePickerDialog(
                initialHour = dailyStop.hour,
                initialMinute = dailyStop.minute,
                onDismiss = { showDailyStopPicker = false },
                onConfirm = { hour, minute ->
                    onDailyStopChanged(dailyStop.copy(minuteOfDay = hour * 60 + minute))
                    showDailyStopPicker = false
                }
            )
        }

        MultiChoiceSegmentedButtonRow(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium,
                        top = MaterialTheme.spacing.small
                    )
        ) {
            DayOfWeek.entries.forEachIndexed { index, day ->
                SegmentedButton(
                    checked = day in dailyStop.days,
                    onCheckedChange = { checked ->
                        val days = if (checked) dailyStop.days + day else dailyStop.days - day
                        onDailyStopChanged(dailyStop.copy(days = days))
                    },
                    shape =
                        SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = DayOfWeek.entries.size
                        ),
                    icon = {}
                ) {
                    Text(dayLabels[index])
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium))

        Text(
            text = "高级功能",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.padding(
                    start = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.small
                )
        )

        ListItem(
            modifier =
                Modifier.toggleable(
                    value = isSpatialAudioEnabled,
                    role = Role.Switch,
                    onValueChange = onSpatialAudioEnabledChanged
                ),
            headlineContent = { Text("3D 空间音频 (双耳渲染)") },
            leadingContent = {
                Icon(painter = painterResource(R.drawable.ic_surround_sound), contentDescription = null)
            },
            trailingContent = { Switch(checked = isSpatialAudioEnabled, onCheckedChange = null) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium))

        Text(
            text = "外观与显示",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.padding(
                    start = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.medium
                )
        )

        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.medium),
            contentAlignment = Alignment.Center
        ) {
            val options = listOf("System" to "跟随系统", "Light" to "浅色", "Dark" to "深色")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChanged(mode) },
                        shape =
                            SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}

private val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

private fun dailyStopSummary(schedule: DailyStopSchedule): String {
    if (!schedule.enabled) return "到点自动停止播放，无需每天设置"
    val nextStopAt =
        schedule.nextStopAt(System.currentTimeMillis()) ?: return "请至少选择一天"
    val nextDate = Instant.ofEpochMilli(nextStopAt).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    val dayText =
        when (nextDate) {
            today -> "今天"
            today.plusDays(1) -> "明天"
            else -> "周" + dayLabels[nextDate.dayOfWeek.ordinal]
        }
    return "下次：$dayText %02d:%02d 停止播放".format(schedule.hour, schedule.minute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClockTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState =
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
    var displayMode by remember { mutableStateOf(TimePickerDisplayMode.Picker) }
    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { TimePickerDialogDefaults.Title(displayMode = displayMode) },
        modeToggleButton = {
            TimePickerDialogDefaults.DisplayModeToggle(
                onDisplayModeChange = {
                    displayMode =
                        if (displayMode == TimePickerDisplayMode.Picker)
                            TimePickerDisplayMode.Input
                        else TimePickerDisplayMode.Picker
                },
                displayMode = displayMode
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text("确定")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    ) {
        if (displayMode == TimePickerDisplayMode.Picker) {
            TimePicker(state = timePickerState)
        } else {
            TimeInput(state = timePickerState)
        }
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 1400)
@Preview(
    name = "Dark",
    showBackground = true,
    heightDp = 1400,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsScreenPreview() {
    WhiteNoiseTheme {
        Surface {
            SettingsScreen(
                innerPadding = PaddingValues(),
                volume = 0.6f,
                onVolumePreview = {},
                onVolumeChanged = {},
                balance = -0.2f,
                onBalancePreview = {},
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
                onSpatialAudioEnabledChanged = {},
                dailyStop = DailyStopSchedule(enabled = true),
                onDailyStopChanged = {}
            )
        }
    }
}
