package com.chh2000day.navalcreedmodhelper_v2.structs

import kotlinx.serialization.Serializable

/**
 * @Author CHH2000day
 * @Date 2020/6/12
 **/

@Serializable
data class VersionInfo(val buildType: String, val type: String, val buildCode: Int, val versionName: String, var url: String, var changelog: String, var minVer: Int)
