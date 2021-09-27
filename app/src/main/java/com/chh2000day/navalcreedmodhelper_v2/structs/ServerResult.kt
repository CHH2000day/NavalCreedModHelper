package com.chh2000day.navalcreedmodhelper_v2.structs

import com.chh2000day.navalcreedmodhelper_v2.enums.ResultCode
import kotlinx.serialization.Serializable

/**
 * @Author CHH2000day
 * @Date 2020/6/7
 **/
@Serializable
sealed class ServerResult {
    @Serializable
    data class Success(val message: String = "") : ServerResult()

    @Serializable
    data class Fail(val errorCode: ResultCode, val message: String = "") : ServerResult()
}

@Serializable
sealed class LoginResult : ServerResult() {
    @Serializable
    data class Success(val token: String, val uid: Int, val expTime: Long, val nickname: String) : LoginResult()

    @Serializable
    data class Fail(val resultCode: ResultCode, val message: String = "") : LoginResult()
}

@Serializable
sealed class AnnouncementResult : ServerResult() {
    @Serializable
    data class Success(val id: Int, val announcement: String, val toCopy: String, val title: String) : AnnouncementResult()
}

@Serializable
sealed class VersionCheckResult : ServerResult() {
    @Serializable
    data class Success(var commonInfo: VersionInfo?, var ffmpegInfo: VersionInfo?) : VersionCheckResult()
}