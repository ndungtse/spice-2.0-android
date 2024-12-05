package com.medtroniclabs.spice.ncd.followup.fragment

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.FragmentNcdFollowUpOfflineSearchBinding
import com.medtroniclabs.spice.db.entity.NCDFollowUp
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.setOrientationAndSpanCount
import com.medtroniclabs.spice.ncd.followup.adapter.NCDFollowUpOfflineListAdapter
import com.medtroniclabs.spice.ncd.followup.viewmodel.NCDFollowUpViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.home.AssessmentToolsActivity
import com.medtroniclabs.spice.ui.mypatients.PatientSelectionListenerForFollowUpOffline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NCDFollowUpOfflineSearchFragment : BaseFragment(),
    PatientSelectionListenerForFollowUpOffline {

    private lateinit var binding: FragmentNcdFollowUpOfflineSearchBinding
    private val viewModel: NCDFollowUpViewModel by activityViewModels()
    private val followUpAdapter: NCDFollowUpOfflineListAdapter by lazy {
        NCDFollowUpOfflineListAdapter(
            this
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdFollowUpOfflineSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(type: String) =
            NCDFollowUpOfflineSearchFragment().apply {
                arguments = Bundle().apply {
                    putString(Screening.type, type)
                }
            }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }


    private fun hideProgressAfterDelay() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500) // Wait for 3 seconds
            hideProgress()
        }
    }

    private fun attachObserver() {
        viewModel.getFollowUpData.observe(viewLifecycleOwner) { data ->
            if (data.isEmpty()) {
                binding.rvPatientList.gone()
                binding.tvPatientNoFound.gone()
            } else {
                binding.rvPatientList.visible()
                binding.tvPatientNoFound.gone()
                followUpAdapter.submitData(data)
            }
            viewModel.totalPatientCountOffline.postValue(data.size)
            hideProgressAfterDelay()
        }
        viewModel.updateCallLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                  showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resources.data?.let { data ->
                        data.phoneNumber?.let { phoneNumber ->
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                SecuredPreference.putBoolean(
                                    SecuredPreference.EnvironmentKey.INITIAL_CALL.name,
                                    true
                                )
                                this.data = Uri.parse("tel:${phoneNumber}")
                            }
                            startActivity(intent)
                        }
                    }
                    viewModel.updateCallLiveData.postError()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun initView() {
        setOrientationAndSpanCount(
            requireActivity(),
            resources,
            viewModel,
            binding = binding,
            context = requireContext(),
            followUpAdapter = followUpAdapter
        )
    }

    override fun onSelectedPatientForCall(item: NCDFollowUp) {
        if (hasTelephonyFeature()) {
            viewModel.updateInitial(
                item.copy(
                    isInitiated = true,
                    retryAttempts = item.retryAttempts?.minus(1) ?: 0,
                    isCompleted = item.retryAttempts == 1L
                )
            )
        } else {
            showCallDialError()
        }
    }

    private fun hasTelephonyFeature(): Boolean {
        val packageManager = requireContext().packageManager
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    override fun onSelectedPatientForAssessment(item: NCDFollowUp) {
        val intent = Intent(requireContext(), AssessmentToolsActivity::class.java)
        intent.putExtra(DefinedParams.FhirId, item.memberId)
        intent.putExtra(DefinedParams.PatientId, item.patientId)
        intent.putExtra(DefinedParams.ORIGIN, MenuConstants.ASSESSMENT.lowercase())
        intent.putExtra(DefinedParams.Gender, item.gender)
        startActivity(intent)
    }


    override fun onSelectedPatientCard(item: NCDFollowUp) {
        viewModel.selectedFollowUpPatient = item
        val fragment = childFragmentManager.findFragmentByTag(NCDFollowUpDialogFragment.TAG)
        if (fragment == null) {
            NCDFollowUpDialogFragment.newInstance(this)
                .show(childFragmentManager, NCDFollowUpDialogFragment.TAG)
        }
    }

    override fun onResume() {
        super.onResume()
        showProgress()
        followUpAdapter.submitData(listOf())
        viewModel.searchLiveDataForOffline("")
        if (SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.INITIAL_CALL.name)) {
            viewModel.getInitial()
        }
    }
}