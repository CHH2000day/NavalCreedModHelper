package com.chh2000day.navalcreedmodhelper_v2.structs

import com.chh2000day.navalcreedmodhelper_v2.enums.ResultCode

/**
 * @Author CHH2000day
 * @Date 2020/6/5
 **/
sealed class LoginTokenCheckResult {
    data class Fail(val resultCode: ResultCode) : LoginTokenCheckResult()
    data class Success(val uid: Int, val createTime: Long) : LoginTokenCheckResult()
}