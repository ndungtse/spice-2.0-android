package com.medtroniclabs.spice.ui.patientTransfer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.appextensions.textOrEmpty
import com.medtroniclabs.spice.databinding.RowIncomingRequestMessageBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.patientTransfer.NCDApproveRejectListener
import com.medtroniclabs.spice.ncd.data.PatientTransfer
import com.medtroniclabs.spice.common.TransferStatusEnum

class NCDIncomingRequestAdapter(
    val list: ArrayList<PatientTransfer>,
    val listener: NCDApproveRejectListener
) :
    RecyclerView.Adapter<NCDIncomingRequestAdapter.IncomingRequestViewHolder>() {

    class IncomingRequestViewHolder(val binding: RowIncomingRequestMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingRequestViewHolder {
        return IncomingRequestViewHolder(
            RowIncomingRequestMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: IncomingRequestViewHolder, position: Int) {
        val model = list[position]
        holder.binding.tvReason.text = model.transferReason
        holder.binding.btnAccept.safeClickListener {
            listener.onTransferStatusUpdate(
                TransferStatusEnum.ACCEPTED.name,
                model
            )
        }
        holder.binding.btnReject.safeClickListener {
            listener.onTransferStatusUpdate(
                TransferStatusEnum.REJECTED.name,
                model
            )
        }

        holder.binding.tvViewDetail.safeClickListener {
            listener.onViewDetail(model.id)
        }

        holder.binding.tvPatientName.text = "${model.patient.firstName} ${model.patient.lastName.textOrEmpty()}"
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

