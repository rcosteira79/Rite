package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionHeader(title: String, trailingLabel: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = RiteAppTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = RiteAppTheme.colors.primary
        )
        Text(
            text = trailingLabel.uppercase(),
            style = RiteAppTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            ),
            color = RiteAppTheme.colors.onSurfaceVariant
        )
    }
}
