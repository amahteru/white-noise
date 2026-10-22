package top.ekiz.whitenoise.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import top.ekiz.whitenoise.domain.NoiseType

@Composable
fun SoundsScreen(
    innerPadding: PaddingValues,
    currentType: NoiseType,
    noises: List<Pair<NoiseType, String>>,
    onTypeSelected: (NoiseType) -> Unit
) {
    LazyColumn(
        contentPadding = innerPadding,
        modifier = Modifier.fillMaxSize().selectableGroup()
    ) {
        items(noises, key = { it.first }) { (type, name) ->
            val selected = type == currentType
            val icon = noiseIcon(type)
            ListItem(
                modifier =
                    Modifier.selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onTypeSelected(type) }
                    ),
                headlineContent = { Text(name) },
                leadingContent =
                    icon?.let { { Icon(imageVector = it, contentDescription = null) } },
                trailingContent = { RadioButton(selected = selected, onClick = null) }
            )
        }
    }
}

private fun noiseIcon(type: NoiseType): ImageVector? =
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
        NoiseType.BINAURAL_BEATS -> Icons.Filled.Headset
        else -> null
    }
