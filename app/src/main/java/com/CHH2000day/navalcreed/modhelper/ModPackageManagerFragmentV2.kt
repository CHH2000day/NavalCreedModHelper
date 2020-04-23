package com.CHH2000day.navalcreed.modhelper

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ModPackageManagerFragmentV2 : Fragment(), ModPackageManagerV2.OnDataChangedListener {
    private lateinit var fragmentView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var ovrdSwitch: ToggleButton
    private lateinit var listener: OnLongClickListener
    private lateinit var modList: List<ModPackageManagerV2.ModInstallationInfo>
    private lateinit var adapter: MyAdapter
    override fun onChange() {
        adapter.onDataChange(ModPackageManagerV2.getMods().toMutableList())
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // TODO: Implement this method
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentView = inflater.inflate(R.layout.modmanagerfragmemt, null)
        recyclerView = fragmentView.findViewById(R.id.modmanagerfragmemtRecyclerView) as RecyclerView
        ovrdSwitch = fragmentView.findViewById(R.id.modmanagerswitcherToggleButton1) as ToggleButton
        modList = ModPackageManagerV2.getMods()
        return fragmentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (ModPackageManagerV2.override) {
            ovrdSwitch.isChecked = true
        }
        adapter = MyAdapter(activity!!)
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration())
        recyclerView.adapter = adapter
        ModPackageManagerV2.registerOnDataChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ModPackageManagerV2.unregisterOnDataChangeListener()
    }

    private fun getLocalizedStatus(status: ModPackageManagerV2.Status): String {
        return when (status) {
            ModPackageManagerV2.Status.UNKNOWN -> {
                getString(R.string.unknown)
            }
            ModPackageManagerV2.Status.INSTALLING -> {
                getString(R.string.installing)
            }
            ModPackageManagerV2.Status.INSTALLED -> {
                getString(R.string.installed)
            }
            ModPackageManagerV2.Status.PARTLY_WORKING -> {
                getString(R.string.partly_working)
            }
        }
    }
    private fun getLocalizedModType(type: String, subType: String): String {
        return when (type) {
            ModPackageInfo.MODTYPE_CV -> {
                when (subType) {
                    ModPackageInfo.SUB_MODTYPE_CV_CN -> {
                        getString(R.string.modtype_captainvoice_cn)
                    }
                    ModPackageInfo.SUB_MODTYPE_CV_EN -> {
                        getString(R.string.modtype_captainvoice_en)
                    }
                    ModPackageInfo.SUB_MODTYPE_CV_DE -> {
                        getString(R.string.modtype_captainvoice_de)
                    }
                    ModPackageInfo.SUB_MODTYPE_CV_RU -> {
                        getString(R.string.modtype_captainvoice_ru)
                    }
                    ModPackageInfo.SUB_MODTYPE_CV_RU_BEARD -> {
                        getString(R.string.modtype_captainvoice_ru_beard)
                    }
                    ModPackageInfo.SUB_MODTYPE_CV_RU_VLAD -> {
                        getString(R.string.modtype_captainvoice_ru_vlad)
                    }
                    ModPackageInfo.SUB_MODTYPE_CV_JP_BB -> {
                        getString(R.string.modtype_captainvoice_ja_bb)
                    }
                    ModPackageInfo.SUB_MODTYPE_CV_JP_CA -> {
                        getString(R.string.modtype_captainvoice_ja_ca)
                    }
                    ModPackageInfo.SUB_MODTYPE_CV_JP_CV -> {
                        getString(R.string.modtype_captainvoice_ja_cv)
                    }
                    ModPackageInfo.SUB_MODTYPE_CV_JP_DD -> {
                        getString(R.string.modtype_captainvoice_ja_dd)
                    }
                    else -> {
                        getString(R.string.modtype_captainvoice)
                    }
                }
            }
            ModPackageInfo.MODTYPE_BACKGROUND -> {
                getString(R.string.modtype_background)
            }
            ModPackageInfo.MODTYPE_BGM -> {
                getString(R.string.modtype_backgroundmusic)
            }
            ModPackageInfo.MODTYPE_CREWPIC -> {
                getString(R.string.modtype_crewpic)
            }
            ModPackageInfo.MODTYPE_OTHER -> {
                getString(R.string.unknown)
            }
            ModPackageInfo.MODTYPE_SOUNDEFFECT -> {
                getString(R.string.modtype_soundeffect)
            }
            ModPackageInfo.MODTYPE_SOUNDEFFECT_PRIM -> {
                getString(R.string.modtype_soundeffect_prim)
            }
            ModPackageInfo.MODTYPE_SOUNDEFFECT_SEC -> {
                getString(R.string.modtype_soundeffect_sec)
            }
            else -> {
                getString(R.string.unknown)
            }
        }
    }

    private class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private inner class MyAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        private val listener: OnLongClickListener

        init {
            listener = UninstallListener()
        }

        @SuppressLint("InflateParams")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val vi = layoutInflater.inflate(R.layout.modmanagerv2_item, null)
            return ViewHolder(vi)
        }


        override fun onBindViewHolder(@NonNull holder: RecyclerView.ViewHolder, position: Int): Unit {
            val v = (holder as ViewHolder).view
            val relativeLayout = v.findViewById<RelativeLayout>(R.id.modmanageritemV2RelativeLayout)
            val modName = v.findViewById<TextView>(R.id.modmanageritemV2ModName)
            val modType = v.findViewById<TextView>(R.id.modmanageritemV2ModType)
            val modStatus = v.findViewById<TextView>(R.id.modmanageritemV2ModStatus)
            relativeLayout.tag = modList[position].name
            relativeLayout.setOnLongClickListener(listener)
            modName.text = modList[position].name
            modType.text = getLocalizedModType(modList[position].type, modList[position].subType)
            modStatus.text = getLocalizedStatus(modList[position].status)
        }

        override fun getItemCount(): Int {
            return if (ModPackageManagerV2.override) {
                0
            } else modList.size
        }

        @Synchronized
        fun onDataChange(mods: List<ModPackageManagerV2.ModInstallationInfo>) {
            //Get differences
            var diffMap = mutableMapOf<ModPackageManagerV2.ModInstallationInfo, DiffReason>()
            if (mods.size >= modList.size) {
                mods.forEach {
                    var work = false
                    for (mod in modList) {
                        if (mod.name == it.name) {
                            diffMap[it] = DiffReason.REASON_MODIFY
                            work = true
                            break
                        }
                    }
                    if (!work) {
                        diffMap[it] = DiffReason.REASON_ADD
                    }
                }
            } else {
                modList.forEach {
                    var work = false
                    for (mod in mods) {
                        if (mod.name == it.name) {
                            diffMap[it] = DiffReason.REASON_MODIFY
                            work = true
                            break
                        }
                    }
                    if (!work) {
                        diffMap[it] = DiffReason.REASON_REMOVE
                    }
                }
            }
            diffMap = diffMap.entries.sortedBy {
                it.value.reason
            }.associateBy(
                    { it.key }, { it.value }
            ).toMutableMap()
            val cacheList = modList.toMutableList()
            modList = mods
            diffMap.forEach {
                when (it.value) {
                    DiffReason.REASON_REMOVE -> {
                        for (pos in cacheList.indices) {
                            if (cacheList[pos].name == it.key.name) {
                                notifyItemRemoved(pos)
                                break
                            }
                        }
                    }
                    DiffReason.REASON_ADD -> {
                        notifyItemInserted(itemCount - 1)
                    }
                    DiffReason.REASON_MODIFY -> {
                        for (pos in cacheList.indices) {
                            if (cacheList[pos].name == it.key.name) {
                                notifyItemChanged(pos)
                                break
                            }
                        }
                    }
                }
            }

        }
    }

    enum class DiffReason(val reason: Int) {
        REASON_ADD(10), REASON_REMOVE(20), REASON_MODIFY(30);
    }

    private inner class UninstallListener : View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            val name = v?.tag as String
            val adb = context?.let { AlertDialog.Builder(it) }
            adb?.setTitle(R.string.notice)
                    ?.setMessage(getString(R.string.mod_uninstall_warning, name))
                    ?.setNegativeButton(R.string.cancel, null)
                    ?.setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                        run {
                            ModPackageManagerV2.uninstall(name)
                        }
                    }
                    ?.create()
                    ?.show()
            return true
        }
    }
}