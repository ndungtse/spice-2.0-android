package org.medtroniclabs.uhis.ui.patientTransfer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.appextensions.textOrEmpty
import org.medtroniclabs.uhis.common.TransferStatusEnum
import org.medtroniclabs.uhis.databinding.RowIncomingRequestMessageBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ncd.data.PatientTransfer
import org.medtroniclabs.uhis.ui.patientTransfer.NCDApproveRejectListener

class NCDIncomingRequestAdapter(
    val list: ArrayList<PatientTransfer>,
    val listener: NCDApproveRejectListener,
) :
    RecyclerView.Adapter<NCDIncomingRequestAdapter.IncomingRequestViewHolder>() {
    class IncomingRequestViewHolder(val binding: RowIncomingRequestMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): IncomingRequestViewHolder =
        IncomingRequestViewHolder(
            RowIncomingRequestMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: IncomingRequestViewHolder,
        position: Int,
    ) {
        val model = list[position]
        holder.binding.tvReason.text = model.transferReason
        holder.binding.btnAccept.safeClickListener {
            listener.onTransferStatusUpdate(
                TransferStatusEnum.ACCEPTED.name,
                model,
            )
        }
        holder.binding.btnReject.safeClickListener {
            listener.onTransferStatusUpdate(
                TransferStatusEnum.REJECTED.name,
                model,
            )
        }

        holder.binding.tvViewDetail.safeClickListener {
            listener.onViewDetail(model.id)
        }

        holder.binding.tvPatientName.text = "${model.patient.firstName} ${model.patient.lastName.textOrEmpty()}"
    }

    override fun getItemCount(): Int = list.size
}
