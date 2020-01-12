package com.CHH2000day.navalcreed.modhelper

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import okio.source
import java.io.File

object ModPackageManagerV2 {
    private lateinit var dataFile: File;
    private var inited = false;
    private var modList = mutableListOf<ModInstallationInfo>()
    public fun config(file: File): Boolean {
        dataFile = file
        GlobalScope.launch {
            init()
        }
        return true
    }

    public fun checkInstall(name: String, type: String, subType: String, version: Int, files: Array<String>) {

    }

    public fun uninstall(name: String) {

    }

    public fun postInstall(name: String, type: String, subType: String, version: Int, files: Array<String>) {

    }

    private fun init() {
        val source = dataFile.source().buffer()
        val type = object : TypeToken<List<ModInstallationInfo>>() {}.type
        modList = GsonHelper.getGson().fromJson(source.readUtf8(), type)
        source.close()
        inited = true
    }

    private fun writeConfig() {
        val sink = dataFile.sink().buffer()
        sink.writeUtf8(GsonHelper.getGson().toJson(modList))
        sink.close()
    }

    data class ModInstallationInfo(val name: String, var type: String, var subType: String, var version: Int, var files: Array<String>)
}