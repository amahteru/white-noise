package top.ekiz.whitenoise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.ekiz.whitenoise.domain.NoiseType
import top.ekiz.whitenoise.ui.components.NoiseCard
import top.ekiz.whitenoise.ui.theme.spacing

// Extended FAB height (56dp) plus its 16dp margin and a 16dp gap, so the last row stays reachable.
private val FabClearance = 88.dp

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
                top = innerPadding.calculateTopPadding() + MaterialTheme.spacing.small,
                bottom = innerPadding.calculateBottomPadding() + FabClearance
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(noises, key = { it.first }) { (type, name) ->
            NoiseCard(
                name = name,
                leadingContent = { NoiseLeading(type) },
                isSelected = type == currentType,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@Composable
private fun NoiseLeading(type: NoiseType) {
    val swatch = noiseSwatch(type)
    if (swatch != null) {
        // A filled swatch with an outline keeps light and dark swatches visible on any surface.
        Box(
            modifier =
                Modifier.size(24.dp)
                    .clip(CircleShape)
                    .background(swatch)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
    } else {
        Icon(imageVector = noiseIcon(type), contentDescription = null)
    }
}

private fun noiseSwatch(type: NoiseType): Color? =
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

private fun noiseIcon(type: NoiseType) =
    when (type) {
        NoiseType.OCEAN_WAVES -> Icons.Filled.Waves
        NoiseType.WIND -> Icons.Filled.Air
        NoiseType.HEARTBEAT -> Icons.Filled.Favorite
        NoiseType.AIRPLANE_CABIN -> Icons.Filled.Flight
        NoiseType.VELVET -> Icons.Filled.Grain
        NoiseType.WOMB_SOUNDS -> Icons.Filled.ChildCare
        NoiseType.ISOCHRONIC_TONES -> Icons.Filled.GraphicEq
        NoiseType.SOLFEGGIO_FREQUENCIES -> Icons.Filled.Spa
        NoiseType.RAIN -> Icons.Filled.WaterDrop
        NoiseType.CAMPFIRE -> Icons.Filled.LocalFireDepartment
        else -> Icons.Filled.Headset
    }
