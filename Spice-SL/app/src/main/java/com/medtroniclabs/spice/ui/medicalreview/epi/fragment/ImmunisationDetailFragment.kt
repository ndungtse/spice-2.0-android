package com.medtroniclabs.spice.ui.medicalreview.epi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentImmunisationDetailsBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.epi.adapter.ImmunisationListAdapter
import com.medtroniclabs.spice.ui.medicalreview.epi.viewmodel.ImmunisationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImmunisationDetailFragment : BaseFragment() {
    private lateinit var binding: FragmentImmunisationDetailsBinding
    private lateinit var immunisationListAdapter: ImmunisationListAdapter
    private val viewModel: ImmunisationViewModel by activityViewModels()

    companion object {
        const val TAG = "ImmunisationDetailFragment"

        fun newInstance() = ImmunisationDetailFragment()

        fun newInstance(
            id: String?,
            patientId: String?,
            memberId: String?,
            dateOfBirth: String?,
        ): ImmunisationDetailFragment {
            val fragment = ImmunisationDetailFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.PatientId, patientId)
            bundle.putString(DefinedParams.ID, id)
            bundle.putString(DefinedParams.MemberID, memberId)
            bundle.putString(DefinedParams.DOB, dateOfBirth)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImmunisationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun initView() {
        arguments?.getString(DefinedParams.DOB)?.let {
            // binding.epiProgressIndicatorView.setPatientDetails(it)
        }

        immunisationListAdapter = ImmunisationListAdapter {
            // val patientId = arguments?.getString(DefinedParams.PatientId)
            val dialog = UpdateVaccinationStatusFragment(it)
            dialog.show(childFragmentManager, "dialog")
        }
        binding.rvImmunisationList.adapter = immunisationListAdapter
    }

    private fun attachObserver() {
        val memberId = arguments?.getString(DefinedParams.MemberID)
        val dob = arguments?.getString(DefinedParams.DOB)
        val patientId = arguments?.getString(DefinedParams.PatientId)
        val id = arguments?.getString(DefinedParams.ID)
        viewModel.getImmunisationDetails(id, memberId, patientId, dob)

        viewModel.immunisationDetailListLiveData.observe(viewLifecycleOwner) {
            when (it.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    val list = it.data ?: listOf()
                    binding.epiProgressIndicatorView.setVaccinationList(list)
                    immunisationListAdapter.setVaccinationGroupItems(list)
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        viewModel.addedVaccinationItemLiveData.observe(viewLifecycleOwner) { updatedItem ->
            val adapterList = immunisationListAdapter.getVaccinationGroupItems()
            viewModel.updateImmunisationDetails(adapterList)
            showProgress()
            // val index = adapterList.indexOfFirst { it.scheduleDate == updatedItem.scheduledDate }
            // immunisationListAdapter.notifyItemChanged(index)
        }

        viewModel.shouldRefreshListLiveData.observe(viewLifecycleOwner) {
            immunisationListAdapter.refreshAdapterList()
            hideProgress()
        }
    }
}
