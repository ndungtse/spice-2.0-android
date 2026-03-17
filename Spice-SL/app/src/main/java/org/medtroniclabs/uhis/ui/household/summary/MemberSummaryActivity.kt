package org.medtroniclabs.uhis.ui.household.summary

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.databinding.ActivityHouseholdMemberSummaryBinding
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.home.AssessmentToolsActivity
import org.medtroniclabs.uhis.ui.household.viewmodel.MemberSummaryViewModel

/**
 * Screen to display member details with services they have received
 * Ticket : https://mdtlabs.atlassian.net/browse/UHIS-282
 */
@AndroidEntryPoint
class MemberSummaryActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityHouseholdMemberSummaryBinding

    private val memberSummaryViewModel: MemberSummaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdMemberSummaryBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
        )
        memberSummaryViewModel.initialize(intent)
        initializeView()
        setListeners()
    }

    private fun initializeView() {
        supportFragmentManager
            .beginTransaction()
            .add(binding.fragmentContainer.id, MemberDetailsFragment())
            .commit()
    }

    private fun setListeners() {
        binding.fabServices.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.fabServices.id -> {
                val intent = Intent(this, AssessmentToolsActivity::class.java)
                intent.putExtra(DefinedParams.HOUSEHOLD_ID, memberSummaryViewModel.householdId)
                intent.putExtra(DefinedParams.MEMBER_ID, memberSummaryViewModel.memberId)
                intent.putExtra(DefinedParams.DOB, memberSummaryViewModel.dateOfBirth)
                startActivity(intent)
            }
        }
    }
}
