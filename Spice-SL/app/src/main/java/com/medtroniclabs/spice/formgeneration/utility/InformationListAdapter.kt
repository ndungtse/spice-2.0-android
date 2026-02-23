package com.medtroniclabs.spice.formgeneration.utility

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.databinding.InformationItemListBinding
import com.medtroniclabs.spice.formgeneration.model.InformationModel

class InformationListAdapter(
    private val infoList: ArrayList<InformationModel>,
) : RecyclerView.Adapter<InformationListAdapter.InformationListViewHolder>() {
    inner class InformationListViewHolder(val binding: InformationItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): InformationListAdapter.InformationListViewHolder =
        InformationListViewHolder(
            InformationItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(
        holder: InformationListViewHolder,
        position: Int,
    ) {
        val infoModel = infoList[position]
        if (infoModel.imageId != null) {
            holder.binding.ivItem.setImageResource(infoModel.imageId)
        } else {
            holder.binding.ivItem.visibility = View.GONE
        }
        holder.binding.apply {
            llContainer.setVisible(infoModel.inputText.isNotBlank())
            tvInfo.text = infoModel.inputText
        }
    }

    override fun getItemCount(): Int = infoList.size
}
