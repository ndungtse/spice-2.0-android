package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentReferralTicketBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.ReferralData
import com.medtroniclabs.spice.model.ReferredDate
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.adapter.DateListAdapter
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferralTicketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralTicketFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentReferralTicketBinding
    private var listPopupWindow: PopupWindow? = null
    private lateinit var dateListAdapter: DateListAdapter
    val viewModel: ReferralTicketViewModel by activityViewModels()
    var patientId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReferralTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "ReferralTicketFragment"
        fun newInstance(): ReferralTicketFragment {
            return ReferralTicketFragment()
        }

        fun newInstance(patientId: String?): ReferralTicketFragment {
            val fragment = ReferralTicketFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.PatientId, patientId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun initView() {
        binding.root.gone()
        patientId = arguments?.getString(DefinedParams.PatientId, "")
        if (patientId?.isNotBlank() == true) {
            viewModel.getReferralTicket(patientId = patientId)
        }
        val inflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_popup_window, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDateList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        dateListAdapter =
            DateListAdapter { referred ->
                viewModel.getReferralTicket(
                    patientId = patientId,
                    ticketId = referred.id
                )
                viewModel.ticketId = referred.id
            }
        recyclerView.adapter = dateListAdapter
        listPopupWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        binding.llPresActions.ivReload.safeClickListener(this)
        binding.llPresActions.ivNext.safeClickListener(this@ReferralTicketFragment)
        binding.llPresActions.ivPrevious.safeClickListener(this@ReferralTicketFragment)
    }

    private fun attachObservers() {
        viewModel.referralTicketLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resource.data?.let {
                        binding.root.visible()
                        setReferralTicket(it)
                    } ?: kotlin.run {
                        binding.root.gone()
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun setReferralTicket(referralData: ReferralData) {
        val adapters = PatientInfoAdapter(
            listOf(
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.patient_status),
                    DefinedParams.value to referralData.patientStatus
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.referral_by),
                    DefinedParams.value to referralData.referredBy
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.referral_to),
                    DefinedParams.value to referralData.referredTo
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.referral_reason),
                    DefinedParams.value to referralData.referredReason
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.date_of_onset),
                    DefinedParams.value to referralData.dateOfOnset?.let {
                        DateUtils.convertDateFormat(
                            it,
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                            DateUtils.DATE_ddMMyyyy
                        )
                    }
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.referral_date),
                    DefinedParams.value to referralData.referreddate?.let {
                        DateUtils.convertDateFormat(
                            it,
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                            DateUtils.DATE_ddMMyyyy
                        )
                    }
                )
            ),
            R.color.white
        )
        val params = binding.centerGuideline.layoutParams as ConstraintLayout.LayoutParams
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.guidePercent = 0.5f
        } else {
            params.guidePercent = 0.8f
        }
        binding.centerGuideline.layoutParams = params
        binding.rvPrescription.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrescription.adapter = adapters
        setReferralDates(referralData.referredDates, referralData.id)
    }

    private fun checkNextPreviousVisibility() {
        binding.llPresActions.ivPrevious.isEnabled = checkForPreviousItem() != -1
        binding.llPresActions.ivNext.isEnabled = checkNextItem() != -1
    }

    private fun setReferralDates(referredDates: List<ReferredDate>?, id: String?) {
        if (referredDates != null && viewModel.ticketId == null) {
            dateListAdapter.submitData(referredDates, id)
            viewModel.ticketId = id
            viewModel.referralDates = referredDates
        } else {
            dateListAdapter.submitData(id)
        }
        checkNextPreviousVisibility()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.llPresActions.ivReload.id -> {
                listPopupWindow?.isOutsideTouchable = true
                listPopupWindow?.isFocusable = true
                listPopupWindow?.showAsDropDown(binding.llPresActions.ivReload)
            }

            binding.llPresActions.ivPrevious.id -> {
                getPreviousItemToCurrent()
            }

            binding.llPresActions.ivNext.id -> {
                getNextItemToCurrent()
            }
        }
    }

    private fun checkForPreviousItem(): Int {
        var selectedIndex = -1
        viewModel.referralDates.let {
            it.forEachIndexed { index, labTestDateModel ->
                if (labTestDateModel.id == viewModel.ticketId) {
                    selectedIndex = index - 1
                }
            }
        }
        return selectedIndex
    }

    private fun checkNextItem(): Int {
        var selectedIndex = -1
        viewModel.referralDates.let {
            it.forEachIndexed { index, labTestDateModel ->
                if ((labTestDateModel.id == viewModel.ticketId) && ((index + 1) < it.size)) {
                    selectedIndex = index + 1
                }
            }
        }
        return selectedIndex
    }

    private fun getPreviousItemToCurrent() {
        val selectedIndex = checkForPreviousItem()
        viewModel.referralDates.let { referredDates ->
            if (selectedIndex != -1) {
                viewModel.getReferralTicket(
                    patientId = patientId,
                    ticketId =
                    referredDates[selectedIndex].id?.let {
                        viewModel.ticketId = it
                        it
                    }
                )
            }
        }
    }

    private fun getNextItemToCurrent() {
        val selectedIndex = checkNextItem()
        viewModel.referralDates.let { referredDates ->
            if (selectedIndex != -1) {
                viewModel.getReferralTicket(
                    patientId = patientId,
                    ticketId = referredDates[selectedIndex].id?.let {
                        viewModel.ticketId = it
                        it
                    }
                )
            }
        }
    }
}