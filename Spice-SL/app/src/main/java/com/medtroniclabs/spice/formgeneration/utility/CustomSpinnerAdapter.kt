package com.medtroniclabs.spice.formgeneration.utility

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.SpinnerDropDownBinding
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.cultureValue


class CustomSpinnerAdapter(context: Context,val translate:Boolean =false) :
    ArrayAdapter<String>(context, R.layout.spinner_drop_down_item) {

    var itemList = ArrayList<Map<String, Any>>()
    var emptyLayoutResourceId: Int = R.layout.spinner_drop_down_item
    override fun getCount(): Int = itemList.size

    fun setData(listItems: ArrayList<Map<String, Any>>) {
        itemList = listItems
        notifyDataSetChanged()
    }

    fun getData(position: Int) : Map<String, Any>? {
        return if(position<itemList.size) itemList[position] else null
    }

    override fun getItem(position: Int): String {
        return if (translate) {
            (itemList[position][cultureValue] as String?)?:(itemList[position]["name"] as String)
        }else {
            itemList[position]["name"] as String
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    private fun createView(position: Int, viewGroup: ViewGroup?): View {

        val binding = SpinnerDropDownBinding.inflate(
            LayoutInflater.from(viewGroup?.context)
        )
        binding.tvTitle.text = getItem(position)

        return binding.root
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getDropDownView(position: Int, mView: View?, parentGroup: ViewGroup): View {
        if (itemList.isEmpty()) {
            // If itemList is empty, inflate the empty state layout
            return LayoutInflater.from(parentGroup.context).inflate(emptyLayoutResourceId, parentGroup, false)
        } else {
            // If itemList is not empty, create and return the regular item view
            return createView(position, parentGroup)
        }
    }

    fun getIndexOfItem(id: Long): Int {
        itemList.forEachIndexed { index, map ->
            if (map[DefinedParams.ID] is Double && map[DefinedParams.ID] == id.toDouble()) {
                return index
            }
            if (map[DefinedParams.ID] is Long && map[DefinedParams.ID] == id) {
                return index
            }
        }
        return -1
    }

    fun getIndexOfItemByName(name: String): Int {
        itemList.forEachIndexed { index, map ->
            if (map[DefinedParams.NAME] == name) {
                return index
            }
        }
        return -1
    }

    fun getIndexOfItemById(id: String): Int {
        itemList.forEachIndexed { index, map ->
            if (map[DefinedParams.ID] == id) {
                return index
            }
        }
        return -1
    }

    fun removeItemById(id: String) {
        val removeIndex  = itemList.indexOfFirst { it[DefinedParams.ID] == id }
        if (removeIndex != -1) {
            itemList.removeAt(removeIndex)
        }
    }
}