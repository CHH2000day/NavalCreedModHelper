package com.chh2000day.navalcreed.modhelper.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chh2000day.navalcreed.modhelper.MainActivity
import com.chh2000day.navalcreed.modhelper.R
import com.chh2000day.navalcreed.modhelper.enums.UiPages
import com.chh2000day.navalcreed.modhelper.ui.theme.BluePrimaryDark


@Preview
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
                        text = getString(R.string.cancel),
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
                        text = getString(R.string.exit),
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


@Preview
@Composable
internal fun MainActivity.ActivityContent() {
    val navController = rememberNavController()
    NavBar(navController)
    NavHost(navController = navController, startDestination = UiPages.About.path) {
        composable(UiPages.About.path) {
            AboutPage()
        }
    }
}

@Composable
internal fun MainActivity.NavBar(navController: NavController) {
    Row(Modifier.height(50.dp)) {

    }
}