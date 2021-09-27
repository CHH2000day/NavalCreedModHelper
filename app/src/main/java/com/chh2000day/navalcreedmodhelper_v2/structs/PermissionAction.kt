package com.chh2000day.navalcreedmodhelper_v2.structs

/**
 * @Author CHH2000day
 * @Date 2020/6/7
 **/
sealed class PermissionAction {
    data class ModifyMod(val mid: Int)
    data class RemoveMod(val mid: Int)
    class AddMod()

}