package org.medtroniclabs.uhis.ui.household.summary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.databinding.FragmentMemberDetailsBinding
import org.medtroniclabs.uhis.databinding.SummaryListItemBinding
import org.medtroniclabs.uhis.ui.assessment.utils.AssessmentUtil
import org.medtroniclabs.uhis.ui.household.HouseholdActivity
import org.medtroniclabs.uhis.ui.household.viewmodel.MemberSummaryViewModel

class MemberDetailsFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentMemberDetailsBinding
    private val memberSummaryViewModel: MemberSummaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMemberDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        attachObservers()
        setListeners()
    }

    private fun attachObservers() {
        memberSummaryViewModel.memberDetails.observe(viewLifecycleOwner) { memberDetails ->
            memberDetails ?: return@observe
            val recentHistoryDate = memberDetails.history.firstOrNull()?.let {
                DateUtils.getLastMenstrualDate(it.visitDate).timeInMillis
            } ?: 0
            val lastUpdated = memberDetails.member.lastUpdated?.let {
                DateUtils.getLastMenstrualDate(it).timeInMillis
            } ?: 0
            val lastActivity = when {
                recentHistoryDate == 0L && lastUpdated == 0L -> {
                    null
                }

                recentHistoryDate < lastUpdated -> {
                    DateUtils.formatDateToDisplayFormat(lastUpdated)
                }

                else -> {
                    DateUtils.formatDateToDisplayFormat(recentHistoryDate)
                }
            } ?: getString(R.string.separator_double_hyphen)

            val registeredAt = DateUtils.formatDateToDisplayFormat(memberDetails.member.createdAt)
            val servicesProvided = if (memberDetails.history.isNotEmpty()) {
                memberDetails.history.joinToString { AssessmentUtil.mapServiceToServiceName(it.serviceProvided) }
            } else {
                getString(R.string.separator_double_hyphen)
            }
            addSummaryView(getString(R.string.registration_date), registeredAt ?: getString(R.string.separator_double_hyphen))
            addSummaryView(getString(R.string.patient_id), memberDetails.member.patientId ?: getString(R.string.separator_double_hyphen))
            addSummaryView(getString(R.string.mobile_number), memberDetails.member.phoneNumber ?: getString(R.string.separator_double_hyphen))
            addSummaryView(getString(R.string.last_visit_date), lastActivity)
            addSummaryView(getString(R.string.services_provided), servicesProvided)
            addSummaryView(
                getString(R.string.recent_status),
                memberDetails.history.firstOrNull()?.let {
                    AssessmentUtil.mapServiceToServiceName(it.serviceProvided)
                } ?: getString(R.string.separator_double_hyphen),
            )
            if (memberDetails.member.isActive) {
                binding.tvEdit.visible()
            } else {
                binding.tvEdit.gone()
            }
        }
    }

    private fun setListeners() {
        binding.tvEdit.setOnClickListener(this)
    }

    private fun addSummaryView(
        name: String,
        value: String,
    ) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = name
        view.tvValue.text = value
        binding.llDetails.addView(view.root)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.tvEdit.id -> {
                val intent = Intent(requireContext(), HouseholdActivity::class.java)
                intent.putExtra(DefinedParams.MEMBER_ID, memberSummaryViewModel.memberId)
                intent.putExtra(DefinedParams.householdId, memberSummaryViewModel.householdId)
                startActivity(intent)
            }
        }
    }
}
