package com.chh2000day.navalcreed.modhelper.enums

import com.chh2000day.navalcreed.modhelper.R

sealed class Screen(val route: String, val stringResId: Int) {
    object BackGround : Screen("BackGround", R.string.background_img)
    object BackGroundMusic : Screen("BackGroundMusic", R.string.background_music)
    object CrewPic : Screen("CrewPic", R.string.custom_ship_name)
    object CustomShipName : Screen("CustomShipName", R.string.modtype_customshipname)
    object LoginMovie : Screen("LoginMovie", R.string.login_movie)
    object ModInstaller : Screen("ModInstaller", R.string.mod_installer)
    object ModManager : Screen("ModManager", R.string.mod_manager)
    object Settings : Screen("Settings", R.string.settings)
    object About : Screen("About", R.string.about)
}

val screenList = listOf(
    Screen.BackGround, Screen.BackGroundMusic, Screen.CrewPic,
    Screen.CustomShipName, Screen.LoginMovie, Screen.ModInstaller, Screen.ModManager,
    Screen.Settings,
    Screen.About
)