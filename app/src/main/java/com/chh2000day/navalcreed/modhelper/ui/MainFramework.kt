package com.chh2000day.navalcreed.modhelper.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chh2000day.navalcreed.modhelper.MainActivity
import com.chh2000day.navalcreed.modhelper.R
import com.chh2000day.navalcreed.modhelper.enums.Screen
import com.chh2000day.navalcreed.modhelper.enums.screenList
import com.chh2000day.navalcreed.modhelper.ui.theme.BluePrimaryDark


@Composable
internal fun MainActivity.MainTopBar() {
    val shouldShowExitDialog = remember { mutableStateOf(false) }
    TopAppBar {
        Text(
            getString(R.string.app_name), modifier = Modifier
                .weight(90.0F)
                .padding(5.dp)
        )
        Text(getString(R.string.exit), textAlign = TextAlign.Right, modifier = Modifier
            .weight(20.0F)
            .clickable {
                shouldShowExitDialog.value = true
            }
            .padding(5.dp)
        )
    }
    if (shouldShowExitDialog.value) {
        AlertDialog(
            onDismissRequest = {
                shouldShowExitDialog.value = false
            },
            title = { Text(getString(R.string.notice)) },
            text = { Text(getString(R.string.exitmsg)) },
            buttons = {
                Row(horizontalArrangement = Arrangement.End) {
                    Spacer(modifier = Modifier.weight(60.0f))
                    Text(
                        text = stringResource(R.string.cancel),
                        modifier = Modifier
                            .clickable {
                                shouldShowExitDialog.value = false
                            }
                            .weight(20.0f)
                            .padding(10.dp),
                        textAlign = TextAlign.Center,
                        color = BluePrimaryDark
                    )
                    Text(
                        text = stringResource(R.string.exit),
                        modifier = Modifier
                            .clickable { finish() }
                            .weight(20.0f)
                            .padding(10.dp),
                        textAlign = TextAlign.Center,
                        color = BluePrimaryDark
                    )
                }
            }
        )
    }
}


@Composable
internal fun MainActivity.ActivityContent(navController: NavHostController) {
    Column {
        NavBar(navController)
        NavHost(
            navController = navController,
            startDestination = Screen.About.route,
            modifier = Modifier.padding(5.dp)
        ) {
            composable(Screen.About.route) {
                AboutPage()
            }
            composable(Screen.BackGround.route) {

            }
            composable(Screen.BackGroundMusic.route) {

            }
            composable(Screen.CrewPic.route) {

            }
            composable(Screen.CustomShipName.route) {

            }
            composable(Screen.LoginMovie.route) {

            }
            composable(Screen.ModInstaller.route) {

            }
            composable(Screen.ModManager.route) {

            }
            composable(Screen.Settings.route) {

            }
        }
    }
}

@Composable
internal fun NavBar(navController: NavController) {
    val selectedIndex = remember { mutableStateOf(screenList.lastIndex) }
    ScrollableTabRow(selectedTabIndex = selectedIndex.value) {
        screenList.forEachIndexed { index, screen ->
            Tab(selected = navController.currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                    selectedIndex.value = index
                }, text = { Text(text = stringResource(id = screen.stringResId)) })
        }
    }

}

@Preview
@Composable
fun ShowNavBar() {
    val navController = rememberNavController()
    NavBar(navController)
}