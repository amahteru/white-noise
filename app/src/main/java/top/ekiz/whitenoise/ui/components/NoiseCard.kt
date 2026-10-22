package top.ekiz.whitenoise.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import top.ekiz.whitenoise.R

@Composable
fun NoiseCard(
    name: String,
    @DrawableRes icon: Int,
    iconTint: androidx.compose.ui.graphics.Color? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier =
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        colors =
            ListItemDefaults.colors(
                containerColor =
                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surface,
                headlineColor =
                    if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurface
            ),
        headlineContent = { Text(text = name) },
        leadingContent = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint =
                    iconTint
                        ?: if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
