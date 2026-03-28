package com.ricardocosteira.habitlock.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.nav_history
import habitlock.composeapp.generated.resources.nav_settings
import habitlock.composeapp.generated.resources.nav_today
import org.jetbrains.compose.resources.stringResource

enum class BottomNavTab {
    TODAY,
    HISTORY,
    SETTINGS
}

@Composable
fun HabitLockBottomNav(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItemColors =
        NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary
        )

    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = currentTab == BottomNavTab.TODAY,
            onClick = { onTabSelected(BottomNavTab.TODAY) },
            colors = navItemColors,
            icon = {
                Icon(
                    imageVector =
                        if (currentTab ==
                            BottomNavTab.TODAY
                        ) {
                            Icons.Filled.CalendarToday
                        } else {
                            Icons.Outlined.CalendarToday
                        },
                    contentDescription = stringResource(Res.string.nav_today)
                )
            },
            label = { Text(stringResource(Res.string.nav_today)) }
        )
        NavigationBarItem(
            selected = currentTab == BottomNavTab.HISTORY,
            onClick = { onTabSelected(BottomNavTab.HISTORY) },
            colors = navItemColors,
            icon = {
                Icon(
                    imageVector =
                        if (currentTab ==
                            BottomNavTab.HISTORY
                        ) {
                            Icons.Filled.History
                        } else {
                            Icons.Outlined.History
                        },
                    contentDescription = stringResource(Res.string.nav_history)
                )
            },
            label = { Text(stringResource(Res.string.nav_history)) }
        )
        NavigationBarItem(
            selected = currentTab == BottomNavTab.SETTINGS,
            onClick = { onTabSelected(BottomNavTab.SETTINGS) },
            colors = navItemColors,
            icon = {
                Icon(
                    imageVector =
                        if (currentTab ==
                            BottomNavTab.SETTINGS
                        ) {
                            Icons.Filled.Settings
                        } else {
                            Icons.Outlined.Settings
                        },
                    contentDescription = stringResource(Res.string.nav_settings)
                )
            },
            label = { Text(stringResource(Res.string.nav_settings)) }
        )
    }
}
