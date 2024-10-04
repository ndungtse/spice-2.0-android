package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.hideKeyboard
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.SearchLength
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentPatientSearchBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.data.PatientVisitRequest
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMedicalReviewCMRActivity
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.home.AssessmentToolsActivity
import com.medtroniclabs.spice.ui.medicalreview.addnewmember.AddNewMemberActivity
import com.medtroniclabs.spice.ui.mypatients.PatientSelectionListener
import com.medtroniclabs.spice.ui.mypatients.PatientsListAdapter
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientListViewModel
import com.medtroniclabs.spice.ui.referralhistory.activity.ReferralHistoryActivity
import com.medtroniclabs.spice.ui.registration.RegistrationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PatientSearchFragment : BaseFragment(), PatientSelectionListener, View.OnClickListener {
    lateinit var binding: FragmentPatientSearchBinding
    private val patientListViewModel: PatientListViewModel by viewModels()
    private lateinit var patientsListAdapter: PatientsListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientSearchBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    companion object {
        const val TAG = "PatientSearchFragment"
        fun newInstance(): PatientSearchFragment {
            return PatientSearchFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setAdapterViews()
        attachObservers()
        getPatientList()
        patientListViewModel.setUserJourney(AnalyticsDefinedParams.PatientSearch)
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
    }

    private fun swipeRefresh() {
        withNetworkAvailability(online = {
            getPatientList()
            scrollTop()
        }, offline = {
            if (binding.refreshLayout.isRefreshing) {
                binding.refreshLayout.isRefreshing = false
            }
        })
    }

    private fun attachObservers() {
        patientListViewModel.totalPatientCount.observe(viewLifecycleOwner) { count ->
            if (!count.isNullOrBlank()) {
                val patientCount = count.toLong()
                if (patientCount > 0) {
                    binding.tvPatientCount.apply {
                        text = if(patientCount == 1L) getString(R.string.patient_found) else getString(R.string.patients_found, patientCount)
                        visibility = View.VISIBLE
                    }
                    binding.tvNoPatientsFound.gone()
                    binding.btnRegister.gone()
                } else {
                    binding.tvPatientCount.gone()
                    if (patientListViewModel.searchText.isBlank()) {
                        binding.tvNoPatientsFound.gone()
                        binding.btnRegister.gone()
                    } else {
                        binding.tvNoPatientsFound.visible()
                        binding.btnRegister.visibility =
                            if (patientListViewModel.origin.equals(MenuConstants.REGISTRATION, true)) View.VISIBLE else View.GONE
                    }
                }
                binding.llFilter.apply {
                    if (patientListViewModel.origin.equals(MenuConstants.MY_PATIENTS_MENU_ID, true)) {
                        root.invisible() //TODO: filter needs to be implemented later
                        if (patientListViewModel.filterCount() > 0) {
                            binding.llFilter.btnFilter.text =
                                getString(R.string.filter_count, patientListViewModel.filterCount())
                        } else
                            binding.llFilter.btnFilter.text = getString(R.string.filter)

                    } else
                        root.invisible()
                }
            }
            if (binding.refreshLayout.isRefreshing) {
                binding.refreshLayout.isRefreshing = false
            }
        }
        patientListViewModel.filterLiveData.observe(viewLifecycleOwner) {
            withNetworkAvailability(online = {
                if (it) {
                    getPatientList()
                    scrollTop()
                    patientListViewModel.setFilter(false)
                }
            })
        }
        patientListViewModel.patientVisitLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity).showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity).hideLoading()
                    resourceState.data?.let {
                        val destinationIntent =
                            if (patientListViewModel.selectedPatientDetails?.initialReviewed == true) {
                                NCDMedicalReviewCMRActivity::class.java
                            } else {
                                AssessmentToolsActivity::class.java
                            }
                        val intent =
                            Intent(requireContext(), destinationIntent).apply {
                                putExtra(NCDMRUtil.EncounterReference, it.encounterReference)
                                putExtra(
                                    DefinedParams.FhirId,
                                    patientListViewModel.selectedPatientDetails?.id
                                )
                                putExtra(
                                    DefinedParams.PatientId,
                                    patientListViewModel.selectedPatientDetails?.id
                                )
                                putExtra(DefinedParams.ORIGIN, patientListViewModel.origin)
                                putExtra(
                                    DefinedParams.Gender,
                                    patientListViewModel.selectedPatientDetails?.gender
                                )
                            }
                        startActivity(intent)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity).hideLoading()
                    showErrorDialog(getString(R.string.error), getString(R.string.no_internet_error))
                }
            }
        }
    }


    private fun initViews() {
        patientListViewModel.origin = arguments?.getString(DefinedParams.ORIGIN)
        binding.bottomCardView.gone()
        binding.llFilter.btnFilter.text = getString(R.string.filters)
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        if (tabletSize) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            patientListViewModel.spanCount = DefinedParams.span_count_3
        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            patientListViewModel.spanCount = DefinedParams.span_count_1
        }
        binding.llExactSearch.etPatientSearch.addTextChangedListener(searchListener)
        binding.llExactSearch.btnSearch.safeClickListener(this)
        binding.btnRegister.safeClickListener(this)
        binding.llFilter.btnFilter.safeClickListener(this)
        binding.loadingProgress.safeClickListener(this)
        binding.btnAddNewMember.safeClickListener(this)
        binding.btnAddNewMember.visibility = if (CommonUtils.isSL()) View.VISIBLE else View.GONE
    }

    private val searchListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            /**
             * this method is not used
             */
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            /**
             * this method is not used
             */
        }

        override fun afterTextChanged(s: Editable?) {
            val enteredChar = s?.toString() ?: ""
            val trimmedChar = enteredChar?.trim()
            val hasString = !trimmedChar.isNullOrBlank() && trimmedChar.length > SearchLength
            binding.llExactSearch.btnSearch.isEnabled = hasString
            if (enteredChar.isEmpty() && !hasString) {
                handleSearchBarAfterTextRemove()
            }
        }
    }

    private fun handleSearchBarAfterTextRemove() {
        withNetworkAvailability(online = {
            patientListViewModel.searchText = ""
            getPatientList()
        })
    }

    private fun setAdapterViews() {
        patientsListAdapter = PatientsListAdapter(this)
        binding.rvPatientsList.apply {
            layoutManager =
                GridLayoutManager(requireContext(), patientListViewModel.spanCount)
            adapter = patientsListAdapter
        }
        patientsListAdapter.addLoadStateListener {
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

    override fun onSelectedPatient(item: PatientListRespModel) {
        withNetworkAvailability(online = {
            if (CommonUtils.isSL()) {
                val intent = Intent(requireActivity(), ReferralHistoryActivity::class.java)
                intent.putExtra(DefinedParams.PatientId, item.patientId)
                intent.putExtra(DefinedParams.Gender, item.gender)
                intent.putExtra(DefinedParams.DOB, item.birthDate)
                intent.putExtra(DefinedParams.FhirId, item.id)
                intent.putExtra(DefinedParams.ORIGIN, patientListViewModel.origin)
                startActivity(intent)
            } else {
                val destinationIntent = when (patientListViewModel.origin?.lowercase()) {
                    MenuConstants.REGISTRATION.lowercase() -> RegistrationActivity::class.java
                    MenuConstants.ASSESSMENT.lowercase() -> AssessmentToolsActivity::class.java
                    MenuConstants.MY_PATIENTS_MENU_ID.lowercase() -> {
                        patientListViewModel.selectedPatientDetails = item
                        withNetworkAvailability(online = {
                            patientListViewModel.createPatientVisit(
                                PatientVisitRequest(
                                    patientReference = item.patientId,
                                    provenance = ProvanceDto(
                                    ),
                                    memberReference = item.id
                                )
                            )
                        })
                        null
                    }

                    else -> null
                }
                destinationIntent?.let { destIntent ->
                    val intent = Intent(requireContext(), destIntent)
                    intent.putExtra(DefinedParams.FhirId, item.id)
                    intent.putExtra(DefinedParams.PatientId, item.patientId)
                    intent.putExtra(DefinedParams.ORIGIN, patientListViewModel.origin)
                    intent.putExtra(DefinedParams.Gender,item.gender)
                    startActivity(intent)
                }
            }
        })
    }

    private fun getPatientList() {
        viewLifecycleOwner.lifecycleScope.launch {
            patientListViewModel.patientsDataSource.collectLatest { pagedData ->
                patientsListAdapter.submitData(pagedData)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.llExactSearch.btnSearch.id -> {
                requireContext().hideKeyboard(v)
                networkAvailability()
            }

            binding.llFilter.btnFilter.id -> {
                requireContext().hideKeyboard(v)
                handleFilterClick()
            }
            binding.loadingProgress.id -> {}

            binding.btnAddNewMember.id ->{
                val intent = Intent(requireContext(), AddNewMemberActivity::class.java)
                startActivity(intent)
            }
            binding.btnRegister.id -> {
                val intent = Intent(requireContext(), RegistrationActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun networkAvailability() {
        withNetworkAvailability(online = {
            patientListViewModel.searchText =
                binding.llExactSearch.etPatientSearch.text?.trim().toString()
            getPatientList()
            scrollTop()
        })
    }

    private fun scrollTop() {
        binding.rvPatientsList.scrollToPosition(0)
    }

    private fun handleFilterClick() {
        val existingFragment =
            childFragmentManager.findFragmentByTag(PatientSearchFilterDialog.TAG) as? PatientSearchFilterDialog
        if (existingFragment == null) {
            PatientSearchFilterDialog.newInstance()
                .show(childFragmentManager, PatientSearchFilterDialog.TAG)
        } else {
            existingFragment.show(childFragmentManager, PatientSearchFilterDialog.TAG)
        }
    }

    override fun onResume() {
        super.onResume()
        withNetworkAvailability(online = {
            getPatientList()
        })
    }
}