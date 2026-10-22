package top.ekiz.whitenoise.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import top.ekiz.whitenoise.R
import top.ekiz.whitenoise.domain.NoiseType
import top.ekiz.whitenoise.ui.components.NoiseCard
import top.ekiz.whitenoise.ui.theme.WhiteNoiseTheme
import top.ekiz.whitenoise.ui.theme.spacing

@Composable
fun SoundsScreen(
    innerPadding: PaddingValues,
    currentType: NoiseType,
    noises: List<Pair<NoiseType, String>>,
    onTypeSelected: (NoiseType) -> Unit
) {

    LazyColumn(
        contentPadding =
            PaddingValues(
                start = MaterialTheme.spacing.medium,
                end = MaterialTheme.spacing.medium,
                top = innerPadding.calculateTopPadding() + MaterialTheme.spacing.medium,
                bottom = innerPadding.calculateBottomPadding() + MaterialTheme.spacing.medium
            ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(noises) { pair ->
            val type = pair.first
            val name = pair.second

            val icon =
                when (type) {
                    NoiseType.OCEAN_WAVES -> R.drawable.ic_waves
                    NoiseType.WIND -> R.drawable.ic_air
                    NoiseType.HEARTBEAT -> R.drawable.ic_favorite
                    NoiseType.AIRPLANE_CABIN -> R.drawable.ic_flight
                    NoiseType.VELVET -> R.drawable.ic_grain
                    NoiseType.WOMB_SOUNDS -> R.drawable.ic_child_care
                    NoiseType.ISOCHRONIC_TONES -> R.drawable.ic_graphic_eq
                    NoiseType.SOLFEGGIO_FREQUENCIES -> R.drawable.ic_spa
                    NoiseType.RAIN -> R.drawable.ic_water_drop
                    NoiseType.CAMPFIRE -> R.drawable.ic_local_fire_department
                    NoiseType.WHITE,
                    NoiseType.PINK,
                    NoiseType.BROWN,
                    NoiseType.DEEP_BROWN,
                    NoiseType.BLUE,
                    NoiseType.VIOLET,
                    NoiseType.GREY,
                    NoiseType.GREEN,
                    NoiseType.BLACK -> R.drawable.ic_circle
                    else -> R.drawable.ic_headphones
                }

            val iconTint =
                when (type) {
                    NoiseType.WHITE -> Color(0xFFE6E4E1)
                    NoiseType.PINK -> Color(0xFFC6A4A4)
                    NoiseType.BROWN -> Color(0xFFA38C7D)
                    NoiseType.DEEP_BROWN -> Color(0xFF75665E)
                    NoiseType.BLUE -> Color(0xFF92A8B5)
                    NoiseType.VIOLET -> Color(0xFFA69FAD)
                    NoiseType.GREY -> Color(0xFF9E9E9E)
                    NoiseType.GREEN -> Color(0xFF9EAD9B)
                    NoiseType.BLACK -> Color(0xFF4A4F54)
                    else -> null
                }

            NoiseCard(
                name = name,
                icon = icon,
                iconTint = iconTint,
                isSelected = type == currentType,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SoundsScreenPreview() {
    WhiteNoiseTheme {
        Surface {
            SoundsScreen(
                innerPadding = PaddingValues(),
                currentType = NoiseType.PINK,
                noises =
                    listOf(
                        NoiseType.BLACK to "黑噪音",
                        NoiseType.BROWN to "棕噪音",
                        NoiseType.PINK to "粉噪音",
                        NoiseType.WHITE to "白噪音",
                        NoiseType.RAIN to "雨声",
                        NoiseType.CAMPFIRE to "营火声"
                    ),
                onTypeSelected = {}
            )
        }
    }
}
