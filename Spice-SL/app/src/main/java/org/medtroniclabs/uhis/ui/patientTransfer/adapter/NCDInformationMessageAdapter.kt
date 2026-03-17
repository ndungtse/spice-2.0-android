package org.medtroniclabs.uhis.ui.patientTransfer.adapter

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.TransferStatusEnum
import org.medtroniclabs.uhis.databinding.RowInformationMessageAdapterBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ncd.data.PatientTransfer
import org.medtroniclabs.uhis.ui.patientTransfer.NCDApproveRejectListener

class NCDInformationMessageAdapter(
    private val patientList: ArrayList<PatientTransfer>,
    val listener: NCDApproveRejectListener,
) :
    RecyclerView.Adapter<NCDInformationMessageAdapter.InformationMessageViewHolder>() {
    class InformationMessageViewHolder(val binding: RowInformationMessageAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): InformationMessageViewHolder =
        InformationMessageViewHolder(
            RowInformationMessageAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: InformationMessageViewHolder,
        position: Int,
    ) {
        val model = patientList[position]
        holder.binding.tvInformation.text = getInformationText(model, holder.context)
        holder.binding.btnCancel.visibility = getButtonCancelViewVisibility(model)
        holder.binding.btnCancel.safeClickListener {
            listener.onTransferStatusUpdate(
                TransferStatusEnum.CANCELED.name,
                model,
            )
        }
    }

    private fun getButtonCancelViewVisibility(model: PatientTransfer): Int =
        if (model.transferStatus == TransferStatusEnum.PENDING.name) {
            View.VISIBLE
        } else {
            View.GONE
        }

    private fun getInformationText(
        model: PatientTransfer,
        context: Context,
    ): CharSequence {
        val informationTextBuilder: SpannableStringBuilder
        when (model.transferStatus) {
            TransferStatusEnum.PENDING.name -> {
                informationTextBuilder = SpannableStringBuilder(
                    context.getString(
                        R.string.pending_message_format,
                        model.patient.firstName,
                        model.transferSite?.name ?: "",
                    ),
                )
            }

            TransferStatusEnum.ACCEPTED.name -> {
                informationTextBuilder = SpannableStringBuilder(
                    context.getString(
                        R.string.accepted_message_format,
                        model.patient.firstName,
                        model.transferSite?.name ?: "",
                    ),
                )
            }

            TransferStatusEnum.REJECTED.name -> {
                informationTextBuilder = SpannableStringBuilder(
                    context.getString(
                        R.string.rejected_message_format,
                        model.patient.firstName,
                        model.transferSite?.name ?: "",
                    ),
                )
            }

            else -> {
                informationTextBuilder = SpannableStringBuilder(
                    context.getString(
                        R.string.information_message_template,
                        model.patient.firstName,
                        model.transferSite?.name ?: "",
                        model.transferStatus.lowercase(),
                    ),
                )
            }
        }

        model.transferSite?.name?.let { name ->
            informationTextBuilder.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.site_notification)),
                informationTextBuilder.indexOf(name),
                informationTextBuilder.indexOf(name) + name.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }

        return informationTextBuilder
    }

    override fun getItemCount(): Int = patientList.size
}
