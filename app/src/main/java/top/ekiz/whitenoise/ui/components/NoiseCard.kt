package top.ekiz.whitenoise.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role

@Composable
fun NoiseCard(
    name: String,
    leadingContent: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier =
            Modifier.fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        colors =
            ListItemDefaults.colors(
                containerColor =
                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surface,
                headlineColor =
                    if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                leadingIconColor =
                    if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
            ),
        headlineContent = { Text(text = name) },
        leadingContent = leadingContent,
        trailingContent = { RadioButton(selected = isSelected, onClick = null) }
    )
}
