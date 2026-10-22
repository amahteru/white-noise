package top.ekiz.whitenoise.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.ekiz.whitenoise.domain.NoiseType
import top.ekiz.whitenoise.ui.theme.WhiteNoiseTheme

@Composable
fun SoundsScreen(
    innerPadding: PaddingValues,
    currentType: NoiseType,
    noises: List<Pair<NoiseType, String>>,
    onTypeSelected: (NoiseType) -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.selectableGroup()) {
                noises.forEach { (type, name) ->
                    val selected = type == currentType
                    val icon = noiseIcon(type)
                    ListItem(
                        modifier =
                            Modifier.selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { onTypeSelected(type) }
                            ),
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(name) },
                        leadingContent =
                            icon?.let { { Icon(imageVector = it, contentDescription = null) } },
                        trailingContent = { RadioButton(selected = selected, onClick = null) }
                    )
                }
            }
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

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SoundsScreenPreview() {
    WhiteNoiseTheme {
        Surface {
            SoundsScreen(
                innerPadding = PaddingValues(),
                currentType = NoiseType.RAIN,
                noises =
                    listOf(
                        NoiseType.OCEAN_WAVES to "海浪声",
                        NoiseType.WIND to "风声",
                        NoiseType.RAIN to "雨声",
                        NoiseType.CAMPFIRE to "营火声"
                    ),
                onTypeSelected = {}
            )
        }
    }
}
