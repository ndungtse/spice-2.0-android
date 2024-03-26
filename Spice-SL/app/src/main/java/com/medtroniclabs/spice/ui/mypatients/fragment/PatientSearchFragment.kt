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
import com.medtroniclabs.spice.appextensions.hideKeyboard
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentPatientSearchBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.PatientSelectionListener
import com.medtroniclabs.spice.ui.mypatients.PatientsListAdapter
import com.medtroniclabs.spice.ui.mypatients.ReferralTicketActivity
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientListViewModel
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
    }

    private fun attachObservers() {
        patientListViewModel.totalPatientCount.observe(viewLifecycleOwner) { count ->
            if (!count.isNullOrBlank()) {
                binding.tvPatientCount.text =
                    getString(R.string.patients_found, count.toLong())
                binding.llFilter.btnFilter.visibility = if (count.toLong() != 0L) View.VISIBLE else View.INVISIBLE
                binding.tvNoPatientsFound.visibility = if (count.toLong() != 0L) View.GONE else View.VISIBLE
            }
        }
    }


    private fun initViews() {
        binding.llFilter.btnFilter.text = getString(R.string.filters)
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        if (tabletSize) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            patientListViewModel.spanCount = DefinedParams.span_count_3
        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            patientListViewModel.spanCount = DefinedParams.span_count_1
        }
        binding.llExactSearch.etPatientSearch.addTextChangedListener(searchListener)
        binding.llExactSearch.btnSearch.safeClickListener(this)
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
            val hasString = (s?.trim()?.count() ?: 0) > 0
            binding.llExactSearch.btnSearch.isEnabled = hasString
            if (!hasString) {
                patientListViewModel.searchText = ""
                getPatientList()
            }
        }
    }

    private fun setAdapterViews() {
        patientsListAdapter = PatientsListAdapter(this)
        binding.rvPatientsList.apply {
            layoutManager =
                GridLayoutManager(requireContext(), patientListViewModel.spanCount)
            adapter = patientsListAdapter
        }
        patientsListAdapter.addLoadStateListener {
            if (it.append is LoadState.Loading) {
                binding.pageProgress.visibility = View.VISIBLE
            } else {
                binding.pageProgress.visibility = View.GONE
            }
        }
    }

    override fun onSelectedPatient(item: PatientListRespModel) {
        if (connectivityManager.isNetworkAvailable()) {
            val intent = Intent(requireActivity(), ReferralTicketActivity::class.java)
            intent.putExtra(DefinedParams.PatientId, item.id)
            startActivity(intent)
        } else {
            showErrorDialog(getString(R.string.error),getString(R.string.no_internet_error))
        }
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
        }
    }

    private fun networkAvailability() {
        if (connectivityManager.isNetworkAvailable()) {
            patientListViewModel.searchText =
                binding.llExactSearch.etPatientSearch.text?.trim().toString()
            getPatientList()
        } else {
            showErrorDialog(getString(R.string.error),getString(R.string.no_internet_error))
        }
    }
}