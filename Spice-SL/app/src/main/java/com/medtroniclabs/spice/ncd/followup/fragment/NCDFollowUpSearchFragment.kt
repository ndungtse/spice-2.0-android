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
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentFollowUpSearchBinding
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.FollowUpUpdateRequest
import com.medtroniclabs.spice.ncd.data.PatientFollowUpEntity
import com.medtroniclabs.spice.ncd.followup.adapter.NCDPatientFollowUPListAdapter
import com.medtroniclabs.spice.ncd.followup.viewmodel.NCDFollowUpViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.home.AssessmentToolsActivity
import com.medtroniclabs.spice.ui.mypatients.PatientSelectionListenerForFollowUp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NCDFollowUpSearchFragment : BaseFragment(), PatientSelectionListenerForFollowUp {
    private lateinit var binding: FragmentFollowUpSearchBinding
    private val followUpAdapter: NCDPatientFollowUPListAdapter by lazy { NCDPatientFollowUPListAdapter(this) }

    private val viewModel: NCDFollowUpViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFollowUpSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "NCDFollowUpSearchFragment"
        fun newInstance(type: String) =
            NCDFollowUpSearchFragment().apply {
                arguments = Bundle().apply {
                    putString(Screening.type, type)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        setAdapterViews()
    }

    private fun attachObservers() {
        viewModel.getPatientRegisterResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {

                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    if (resourceState.data?.id != null) {
                        NCDCallResultBottomDialog.newInstance()
                            .show(childFragmentManager, NCDCallResultBottomDialog.TAG)
                    } else {
                        withNetworkAvailability(online = {
                            getPatientList()
                        })
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        viewModel.statusUpdateResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    (childFragmentManager.findFragmentByTag(NCDCallResultBottomDialog.TAG) as? NCDCallResultBottomDialog)?.dismiss()

                    val dataMap = resourceState.data as? HashMap<String, Any>
                    val isInitiated = dataMap?.get("isInitiated") as? Boolean ?: false

                    if (!isInitiated) {
                        getPatientList()
                    } else {
                        val phoneNumber = dataMap?.get("phoneNumber") as? String
                        if (!phoneNumber.isNullOrBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phoneNumber")
                            }
                            startActivity(intent)
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                    resourceState.message?.let { message ->
                        (activity as BaseActivity).showErrorDialogue(
                            getString(R.string.error),
                            message,
                            isNegativeButtonNeed = false
                        ) {}
                    }
                }
            }
        }

        viewModel.searchLiveData.observe(viewLifecycleOwner) {
            getPatientList()
        }
    }

    private fun initView() {
        viewModel.type = arguments?.getString(Screening.type) ?: ""
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        if (tabletSize) {
            requireActivity().requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            viewModel.spanCount = DefinedParams.span_count_3
        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            viewModel.spanCount = DefinedParams.span_count_1
        }
    }

    private fun setAdapterViews() {
        binding.rvPatientList.apply {
            layoutManager =
                GridLayoutManager(requireContext(), viewModel.spanCount)
            adapter = followUpAdapter
        }
        followUpAdapter.addLoadStateListener {
            val isLoading = it.refresh is LoadState.Loading
            if (isLoading) binding.loadingProgress.visible()
            else binding.loadingProgress.gone()
            if (it.append is LoadState.Loading) {
                binding.pageProgress.visible()
            } else {
                binding.pageProgress.gone()
            }
        }
    }

    override fun onSelectedPatientForCall(item: PatientFollowUpEntity) {
        if (hasTelephonyFeature()) {
            item.id?.let { id ->
                val request = FollowUpUpdateRequest(
                    id = id,
                    patientId = item.patientId,
                    attempts = null,
                    type = viewModel.type,
                    referredSiteId = item.referredSiteId,
                    villageId = item.villageId,
                    memberId = item.memberId,
                    isInitiated = true,
                    phoneNumber = item.phoneNumber,
                    provenance = ProvanceDto()
                )
                withNetworkAvailability(online = {
                    viewModel.updatePatientCallRegister(request)
                })
            }
        } else {
            (activity as BaseActivity).showErrorDialogue(
                message = getString(R.string.device_phone_info)
            ) {
                requireActivity().finish()
            }
        }
    }

    override fun onSelectedPatientForAssessment(item: PatientFollowUpEntity) {
        val intent = Intent(requireContext(), AssessmentToolsActivity::class.java)
        intent.putExtra(DefinedParams.FhirId, item.memberId)
        intent.putExtra(DefinedParams.PatientId, item.patientId)
        intent.putExtra(DefinedParams.ORIGIN, MenuConstants.ASSESSMENT.lowercase())
        intent.putExtra(DefinedParams.Gender, item.gender)
        startActivity(intent)
    }

    private fun hasTelephonyFeature(): Boolean {
        val packageManager = requireContext().packageManager
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    private fun getPatientList() {
        followUpAdapter.submitData(viewLifecycleOwner.lifecycle, PagingData.empty())
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.patientsDataSource.collectLatest { pagedData ->
                followUpAdapter.submitData(pagedData)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        withNetworkAvailability(online = {
            viewModel.getPatientCallRegister()
        })
    }
}