package com.chh2000day.navalcreed.modhelper

import android.text.TextUtils
import com.chh2000day.navalcreed.modhelper.toBufferedSink
import com.chh2000day.navalcreed.modhelper.toBufferedSource
import com.orhanobut.logger.Logger
import okio.BufferedSource
import java.io.File
import java.io.IOException

object CustomShipNameHelper {

    private const val HOF = "return\n{\n"
    private const val EOC = "}"
    private const val EOF = "--[[ Generated by NavalCreedModHelper ]]"
    private const val STATEMENT = " [%d] = \"%s\";"
    private var initialized = false
    //Contains all ships' id

    private var shipNamesMap: MutableMap<Int, String> = mutableMapOf()
    private var idList: MutableList<Int> = mutableListOf()
    private var luaFile: File? = null
    fun init(src: File) {
        if (initialized) return
        try {
            doInit(src)
        } catch (e: IOException) {
            Logger.e(e, "Failed to load ship names")
        }
    }

    /**
     * @param src  patch data
     * @param dest path of customshipnames.lua
     * @return isSuccess
     */
    @Suppress("SpellCheckingInspection")
    @JvmOverloads
    fun patch(src: BufferedSource, dest: File? = luaFile): Boolean {
        var isSuccess = false
        try {
            isSuccess = execPatch(src) == 0
            if (isSuccess && dest != null) {
                writeToFile(dest)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return isSuccess
    }

    /**
     * @param src patch data
     * @return Line number of wrong statement,return 0 if everything works
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun execPatch(src: BufferedSource): Int {
        var errPos = 0
        //Read patch
        val lines = src.readUtf8().split("\n".toRegex()).toTypedArray()
        src.close()
        //Explain and execute it
        synchronized(shipNamesMap) {
            var cmd: Array<String>
            val buffer = shipNamesMap.toMutableMap()
            var i = 0
            while (i < lines.size && errPos < 1) {
                try {
                    lines[i] = lines[i].trim()
                    //"#" is a mark of comment,no need to process it
                    if (lines[i].length < 2 || lines[i].startsWith("#")) {
                        i++
                        continue
                    }
                    cmd = lines[i].split(",".toRegex(), 3).toTypedArray()
                    when (cmd[0]) {
                        "set", "def" -> buffer[Integer.valueOf(cmd[1])] = cmd[2]
                        "del" -> buffer.remove(Integer.valueOf(cmd[1]))
                        else -> throw UnknownActionException("Unknown action at line " + i + 1 + " :" + cmd[0])
                    }
                } catch (t: Throwable) {
                    errPos = i + 1
                    Logger.e(t, "Error at line:$errPos")
                }
                i++
            }
            if (errPos == 0) {
                shipNamesMap = buffer
                syncIDList()
            }
        }
        return errPos
    }

    private fun syncIDList() {
        synchronized(idList) {
            idList = ArrayList(shipNamesMap.keys)
            idList.sort()
        }
    }

    @Throws(IOException::class)
    private fun doInit(src: File) {
        luaFile = src
        shipNamesMap.clear()
        idList.clear()
        val source = src.toBufferedSource()
        val rawData = source.readUtf8()
        source.close()
        source.close()
        if (TextUtils.isEmpty(rawData)) {
            return
        }
        val raw = rawData.split("\n".toRegex()).toTypedArray()
        val ids = mutableListOf<Int>()
        for (i in raw.indices) {
            try {
                val line = raw[i].trim()
                if (line.startsWith("--") || line.startsWith("]]") || line.startsWith("return") || line.startsWith("{") || line.startsWith("}")) {
                    continue
                }
                if (line.indexOf('[') < 0) {
                    continue
                }
                val id = line.substring(line.indexOf('[') + 1, line.indexOf(']'))
                val name = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('"'))
                //end of each line's resolve
                val shipId = id.toInt()
                ids.add(shipId)
                shipNamesMap[shipId] = name
            } catch (e: RuntimeException) {
                Logger.e(e, "Error reading line " + (i + 1))
            }
        }
        //end of all loops
        idList = ids
        syncIDList()
        initialized = true
    }

    @Throws(IOException::class)
    private fun writeToFile(dest: File) {
        val sink = dest.toBufferedSink()
        val li: ListIterator<Int> = idList.listIterator()
        var name: String?
        sink.writeUtf8(HOF)
        while (li.hasNext()) {
            val i = li.next()
            name = shipNamesMap[i]
            if (name.isNullOrBlank()) {
                continue
            }
            sink.writeUtf8(String.format(STATEMENT, i, shipNamesMap[i]))
            sink.writeUtf8("\n")
        }
        sink.writeUtf8(EOC)
        sink.writeUtf8("\n")
        sink.writeUtf8(EOF)
        sink.close()
    }

    private class UnknownActionException(msg: String?) : Exception(msg)

}