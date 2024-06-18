package com.medtroniclabs.spice.ui.referralhistory.fragment

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
import com.medtroniclabs.spice.ui.referralhistory.adapter.ReferralHistoryAdapter
import com.medtroniclabs.spice.ui.referralhistory.viewmodel.ReferralHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralTicketFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentReferralTicketBinding
    private var listPopupWindow: PopupWindow? = null
    private lateinit var dateListAdapter: DateListAdapter
    private lateinit var adapters: ReferralHistoryAdapter
    val viewModel: ReferralHistoryViewModel by activityViewModels()

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
            bundle.putString(DefinedParams.FhirId, patientId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun getPatientId(): String? {
        return arguments?.getString(DefinedParams.FhirId, "")
    }

    private fun getInitialReferralTickets() {
        getPatientId()?.takeIf { it.isNotBlank() }
            ?.let { viewModel.getReferralTicket(patientId = it) }
    }
    private fun initView() {
        if (getPatientId().isNullOrBlank()) {
            binding.tvNoHistory.visible()
            binding.llHistoryAction.ivNext.gone()
            binding.llHistoryAction.ivPrevious.gone()
            binding.llHistoryAction.ivReload.gone()
        } else {
            binding.tvNoHistory.gone()
            adapters = ReferralHistoryAdapter()
            getInitialReferralTickets()
            setupClickListeners()
            binding.retryButtonBp.safeClickListener(this)
            setupPopupWindow()
        }
    }

    private fun setupPopupWindow() {
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
                    patientId = getPatientId(),
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
    }

    private fun setupClickListeners() {
        with(binding.llHistoryAction) {
            ivReload.safeClickListener(this@ReferralTicketFragment)
            ivNext.safeClickListener(this@ReferralTicketFragment)
            ivPrevious.safeClickListener(this@ReferralTicketFragment)
        }
    }

    private fun attachObservers() {
        viewModel.referralTicketLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let {
                        handleSuccess()
                        setReferralTicket(it)
                    } ?: kotlin.run {
                        binding.groupHistoryList.gone()
                        binding.clLoaderProgress.gone()
                        binding.tvNoHistory.visible()
                    }
                }

                ResourceState.ERROR -> {
                    handleError()
                }
            }
        }
    }

    private fun showLoading() {
        binding.clLoaderProgress.visible()
        binding.loaderProgress.visible()
        binding.retryButtonBp.gone()
        binding.tvErrorLabel.gone()
        binding.tvNoHistory.gone()
        binding.groupHistoryList.gone()
    }

    private fun handleSuccess() {
        binding.groupHistoryList.visible()
        binding.clLoaderProgress.gone()
        binding.llHistoryAction.ivNext.visible()
        binding.llHistoryAction.ivPrevious.visible()
        binding.llHistoryAction.ivReload.visible()
    }

    private fun handleError() {
        binding.clLoaderProgress.visible()
        binding.retryButtonBp.visible()
        binding.tvErrorLabel.visible()
        binding.groupHistoryList.gone()
    }

    private fun setReferralTicket(referralData: ReferralData) {
        binding.tvNoHistory.gone()
        with(DefinedParams) {
            var referredBy = referralData.referredBy
            referralData.phoneNumber.takeIf { it?.isNotBlank() == true }?.trim()?.let {
                referredBy = requireContext().getString(R.string.referral_by_phone_number, referredBy, it)
            }
            adapters.updateList(listOf(
                mapOf(
                    label to requireContext().getString(R.string.patient_status),
                    value to referralData.patientStatus
                ),
                mapOf(
                    label to requireContext().getString(R.string.referral_by),
                    value to referredBy
                ),
                mapOf(
                    label to requireContext().getString(R.string.referral_to),
                    value to referralData.referredTo
                ),
                mapOf(
                    label to requireContext().getString(R.string.referral_reason),
                    value to referralData.referredReason,
                    valueColor to R.color.red_risk_moderate
                ),
                mapOf(
                    label to requireContext().getString(R.string.date_of_onset),
                    value to referralData.dateOfOnset?.let {
                        DateUtils.convertDateFormat(
                            it,
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                            DateUtils.DATE_ddMMyyyy
                        )
                    }
                ),
                mapOf(
                    label to requireContext().getString(R.string.referral_date),
                    value to referralData.referreddate?.let {
                        DateUtils.convertDateFormat(
                            it,
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                            DateUtils.DATE_ddMMyyyy
                        )
                    }
                )
            ))
        }
        adjustGuideline()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapters
        setReferralDates(referralData.referredDates, referralData.id)
    }

    private fun adjustGuideline() {
        val params = binding.centerGuideline.layoutParams as ConstraintLayout.LayoutParams
        params.guidePercent =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 0.5f else 0.8f
        binding.centerGuideline.layoutParams = params
    }

    private fun checkNextPreviousVisibility() {
        binding.llHistoryAction.ivPrevious.isEnabled = checkForPreviousItem() != -1
        binding.llHistoryAction.ivNext.isEnabled = checkNextItem() != -1
    }

    private fun setReferralDates(referredDates: List<ReferredDate>?, id: String?) {
        if (referredDates != null && viewModel.ticketId == null) {
            dateListAdapter.submitData(referredDates, id)
            viewModel.ticketId = id
            viewModel.referralDates = referredDates
        } else {
            dateListAdapter.updateSelectedId(id)
        }
        checkNextPreviousVisibility()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.llHistoryAction.ivReload.id -> {
                listPopupWindow?.isOutsideTouchable = true
                listPopupWindow?.isFocusable = true
                listPopupWindow?.showAsDropDown(binding.llHistoryAction.ivReload)
            }

            binding.llHistoryAction.ivPrevious.id -> {
                getPreviousItemToCurrent()
            }

            binding.llHistoryAction.ivNext.id -> {
                getNextItemToCurrent()
            }
            binding.retryButtonBp.id -> {
              handleRetry()
            }
        }
    }

    private fun handleRetry() {
        if (connectivityManager.isNetworkAvailable()) {
            if (!viewModel.ticketId.isNullOrBlank()) {
                getPatientId()?.takeIf { it.isNotBlank() }
                    ?.let {
                        viewModel.getReferralTicket(
                            patientId = it,
                            ticketId = viewModel.ticketId
                        )
                    }
            } else {
                getInitialReferralTickets()
            }
        } else {
            showErrorDialog(getString(R.string.error), getString(R.string.no_internet_error))
        }
    }

    private fun checkForPreviousItem(): Int {
        var selectedIndex = -1
        viewModel.referralDates.let {
            it.forEachIndexed { index, referralModel ->
                if (referralModel.id == viewModel.ticketId) {
                    selectedIndex = index - 1
                }
            }
        }
        return selectedIndex
    }

    private fun checkNextItem(): Int {
        var selectedIndex = -1
        viewModel.referralDates.let {
            it.forEachIndexed { index, referralModel ->
                if ((referralModel.id == viewModel.ticketId) && ((index + 1) < it.size)) {
                    selectedIndex = index + 1
                }
            }
        }
        return selectedIndex
    }

    private fun getPreviousItemToCurrent() {
        val selectedIndex = checkForPreviousItem()
        if (selectedIndex != -1) {
            viewModel.referralDates[selectedIndex].id?.let {
                viewModel.getReferralTicket(patientId = getPatientId(), ticketId = it)
                viewModel.ticketId = it
            }
        }
    }

    private fun getNextItemToCurrent() {
        val selectedIndex = checkNextItem()
        if (selectedIndex != -1) {
            viewModel.referralDates[selectedIndex].id?.let {
                viewModel.getReferralTicket(patientId = getPatientId(), ticketId = it)
                viewModel.ticketId = it
            }
        }
    }

}
