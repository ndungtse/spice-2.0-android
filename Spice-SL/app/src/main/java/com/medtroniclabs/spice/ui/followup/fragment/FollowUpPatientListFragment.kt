package com.medtroniclabs.spice.ui.followup.fragment

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.databinding.FragmentFollowUpMyPatientListBinding
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentActivity
import com.medtroniclabs.spice.ui.followup.adapter.PatientListAdapter
import com.medtroniclabs.spice.ui.followup.viewmodel.FollowUpViewModel

class FollowUpPatientListFragment: BaseFragment(), FollowUpDialogFragment.FollowUpClickListener {

    private lateinit var binding: FragmentFollowUpMyPatientListBinding
    private val viewModel: FollowUpViewModel by activityViewModels()
    private lateinit var adapter: PatientListAdapter

    companion object {
        const val TAG = "FollowUpPatientListFragment"
    }

    private val dialerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK || result.resultCode == RESULT_CANCELED) {
                CallResultDialogFragment.newInstance()
                    .show(childFragmentManager, CallResultDialogFragment.TAG)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowUpMyPatientListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        attachListener()
    }

    private fun attachListener() {
        viewModel.followUpPatientListLiveData.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.tvPatientNoFound.gone()
                binding.rvPatientList.visible()
                adapter.updateList(it)
            } else {
                binding.tvPatientNoFound.visible()
                binding.rvPatientList.gone()
                adapter.updateList(listOf())
            }
        }
    }

    private fun initAdapter() {
        adapter = PatientListAdapter { index, data ->
            viewModel.selectedFollowUpDetail = data
            when(index) {
                PatientListAdapter.ConstantPatientListAdapter.PATIENT_DETAIL -> {
                    FollowUpDialogFragment.newInstance(this).show(
                        parentFragmentManager, FollowUpDialogFragment.TAG
                    )
                }

                PatientListAdapter.ConstantPatientListAdapter.CALL -> {
                    onCallClicked()
                }

                PatientListAdapter.ConstantPatientListAdapter.ASSESSMENT -> {
                    onLaunchAssessment()
                }
            }
        }

        binding.rvPatientList.adapter = adapter
    }

    override fun onCallClicked() {
        viewModel.selectedFollowUpDetail?.let { data ->
            CallResultDialogFragment.newInstance()
                .show(childFragmentManager, CallResultDialogFragment.TAG)
            /*data.phoneNumber?.let { phoneNumber ->
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:$phoneNumber")
                dialerLauncher.launch(dialIntent)
            }*/
        }
    }

    override fun onLaunchAssessment() {
        viewModel.selectedFollowUpDetail?.let { data ->
            val intent = Intent(requireContext(), AssessmentActivity::class.java)
            intent.putExtra(DefinedParams.MemberID, data.localPatientId)
            intent.putExtra(DefinedParams.MenuId, data.encounterType?.lowercase())
            startActivity(intent)
        }
    }

}