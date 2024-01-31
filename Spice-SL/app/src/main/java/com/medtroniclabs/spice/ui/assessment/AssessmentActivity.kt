package com.medtroniclabs.spice.ui.assessment

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityAssessmentBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentActivity : BaseActivity(){
    private lateinit var binding: ActivityAssessmentBinding
    private val viewModel: AssessmentViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssessmentBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.assessment)
        )
        getIntentValue()
        loadFragment(1)
        attachObservers()
    }

    private fun loadFragment(status: Int) {
        when (status){
            1 -> {
                replaceFragmentInId<AssessmentFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentFragment.TAG
                )
            }

            2 -> {

            }
        }
    }

    private fun attachObservers() {

    }

    private fun getIntentValue() {
        viewModel.selectedHouseholdMemberId = intent.getLongExtra(DefinedParams.MemberID, -1L)
     }

    private inline fun <reified fragment: Fragment> replaceFragmentInId (
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null
    ){
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(
                id ?: binding.formsFragmentContainer.id,
                args = bundle,
                tag = tag
            )
        }
    }
}