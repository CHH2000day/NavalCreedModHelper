package com.chh2000day.navalcreedmodhelper_v2.enums

/**
 * @Author CHH2000day
 * @Project navalcreedmodhelper_v2
 * @Date 2020/5/3 21:43
 **/
enum class ResultCode(var code: Int) {
    OK(0),
    OK_WITH_MSG(1),
    FIELD_SHOULD_NOT_BE_EMPTY(1001),
    USERNAME_EXISTS(3000),
    USERNAME_NOT_EXIST(3001),
    WEAK_PASSWORD(3010),
    PASSWORD_DISAGREE(3011),
    INVALID_EMAIL(3020),
    EMAIL_ALREADY_USED(3021),
    TOKEN_EXPIRED(3030),
    INVALID_TOKEN(3031),

    /**
     * 用户进行普通操作时权限不足
     */
    USER_PERMISSION_LOW(3050),
    BANNED(3999),
    INVALID_KEY(5000),
    DEVICE_MISMATCH(5010),
    DATABASE_OP_FAULT(6010),

    /**
     * 进行管理操作时权限不足
     */
    PERMISSION_DENIED(7000),
    UNKNOWN_ERROR(9999);

    fun code() = code

}