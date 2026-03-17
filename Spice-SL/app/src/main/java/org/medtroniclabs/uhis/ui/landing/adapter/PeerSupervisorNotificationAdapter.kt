
package org.medtroniclabs.uhis.ui.landing.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.databinding.ItemCbsNotificationBinding
import org.medtroniclabs.uhis.ncd.data.PeerSupervisorNotificationResponse

class PeerSupervisorNotificationAdapter() :
    RecyclerView.Adapter<PeerSupervisorNotificationAdapter.NotificationViewHolder>() {
    private var notificationList: List<PeerSupervisorNotificationResponse>? = null

    inner class NotificationViewHolder(val binding: ItemCbsNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NotificationViewHolder =
        NotificationViewHolder(
            ItemCbsNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: NotificationViewHolder,
        position: Int,
    ) {
        val context = holder.binding.root.context
        val notification = notificationList?.getOrNull(position) ?: return

        with(holder.binding) {
            val formData = notification.formData

            // Alert Type
            val formType = notification.formType?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.hyphen_symbol)

            tvAlertType.text = if (formType.equals(DefinedParams.CBS, ignoreCase = true)) {
                context.getString(R.string.cbs_alert)
            } else {
                formType
            }

            // Member Name
            tvMemberName.text = formData?.memberName?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.hyphen_symbol)

            // Village
            tvVillage.text = formData?.villageName?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.hyphen_symbol)

            // CHW Name
            tvChwName.text = formData?.chwName?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.hyphen_symbol)

            // Member Number
            tvMemberNumber.text =
                if (!formData?.countryCode.isNullOrBlank() && !formData?.memberPhoneNumber.isNullOrBlank()) {
                    "+${formData!!.countryCode} ${formData.memberPhoneNumber}"
                } else {
                    context.getString(R.string.hyphen_symbol)
                }

            // CHW Number
            tvChwNumber.text =
                if (!formData?.countryCode.isNullOrBlank() && !formData?.chwPhoneNumber.isNullOrBlank()) {
                    "+${formData!!.countryCode} ${formData.chwPhoneNumber}"
                } else {
                    context.getString(R.string.hyphen_symbol)
                }

            // Assessment Date
            tvDate.text = formData?.assessmentDate?.takeIf { it.isNotBlank() }?.let {
                DateUtils.convertDateFormat(it, DateUtils.CALENDAR_FORMAT, DateUtils.DATE_ddMMyyyy)
            } ?: context.getString(R.string.hyphen_symbol)

            // Notifiable Conditions + Other Condition
            val conditionList = formData?.notifiableConditions?.filter { it.isNotBlank() }.orEmpty()
            val otherCondition = formData
                ?.otherNotifiableConditions
                ?.trim()
                ?.takeIf { it.isNotBlank() }

            val conditionText = buildString {
                if (conditionList.isNotEmpty()) append(conditionList.joinToString("/"))
                if (!otherCondition.isNullOrEmpty()) {
                    if (conditionList.isNotEmpty()) append(" - ")
                    append(otherCondition)
                }
            }.ifBlank { context.getString(R.string.hyphen_symbol) }

            tvCondition.text = conditionText

            // Alternate card background
            val notificationCardBGColour = ContextCompat.getColor(context, R.color.bg_peer_notification_card)
            val white = ContextCompat.getColor(context, R.color.white)
            cardView.setCardBackgroundColor(if (position % 2 == 0) notificationCardBGColour else white)
        }
    }

    override fun getItemCount(): Int = notificationList?.size ?: 0

    fun setData(notificationResponses: ArrayList<PeerSupervisorNotificationResponse>) {
        this.notificationList = notificationResponses
        notifyDataSetChanged()
    }
}
