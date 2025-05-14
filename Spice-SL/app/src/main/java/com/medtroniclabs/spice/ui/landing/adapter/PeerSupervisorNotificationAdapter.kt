
package com.medtroniclabs.spice.ui.landing.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ItemCbsNotificationBinding
import com.medtroniclabs.spice.ncd.data.PeerSupervisorNotificationResponse

class PeerSupervisorNotificationAdapter() :
    RecyclerView.Adapter<PeerSupervisorNotificationAdapter.NotificationViewHolder>() {

    private var notificationList: List<PeerSupervisorNotificationResponse>? = null

    inner class NotificationViewHolder(val binding: ItemCbsNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            ItemCbsNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList?.get(position) ?: return
        with(holder.binding) {
            tvAlertType.text = if (notification.formType.equals(
                    DefinedParams.CBS,
                    true
                )
            ) holder.binding.root.context.getString(R.string.cbs_alert) else notification.formType
            val conditionList = notification.formData.notifiableConditions
            val otherCondition = notification.formData.otherNotifiableConditions.trim()
            val condition = buildString {
                append(conditionList.joinToString("/"))
                if (!otherCondition.isNullOrEmpty()) {
                    append(" - $otherCondition")
                }
            }
            tvCondition.text = condition
            tvDate.text = DateUtils.convertDateFormat(
                notification.formData.assessmentDate,
                DateUtils.CALENDAR_FORMAT,
                DateUtils.DATE_ddMMyyyy
            )
            tvVillage.text = notification.formData.villageName
            tvMemberName.text = notification.formData.memberName
            tvMemberNumber.text = "+${notification.formData.countryCode} ${notification.formData.memberPhoneNumber}"
            tvChwName.text = notification.formData.chwName
            tvChwNumber.text = "+${notification.formData.countryCode} ${notification.formData.chwPhoneNumber}"

            val context = root.context
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