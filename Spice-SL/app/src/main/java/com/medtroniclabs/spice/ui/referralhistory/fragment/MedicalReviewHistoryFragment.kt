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
import com.medtroniclabs.spice.common.DefinedParams.Above5MedicalReview
import com.medtroniclabs.spice.common.DefinedParams.PregnancyAncMedicalReview
import com.medtroniclabs.spice.data.history.MedicalReviewHistory
import com.medtroniclabs.spice.databinding.FragmentReferralTicketBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.ReferredDate
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.adapter.DateListAdapter
import com.medtroniclabs.spice.ui.referralhistory.adapter.ReferralHistoryAdapter
import com.medtroniclabs.spice.ui.referralhistory.viewmodel.ReferralHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicalReviewHistoryFragment : BaseFragment(), View.OnClickListener {
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
        const val TAG = "MedicalReviewHistoryFragment"
        fun newInstance(): MedicalReviewHistoryFragment {
            return MedicalReviewHistoryFragment()
        }

        fun newInstance(patientId: String?): MedicalReviewHistoryFragment {
            val fragment = MedicalReviewHistoryFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.FhirId, patientId)
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun getPatientId(): String? {
        return arguments?.getString(DefinedParams.FhirId, "")
    }

    private fun getInitialReferralTickets() {
        getPatientId()?.takeIf { it.isNotBlank() }
            ?.let { viewModel.getMedicalReviewHistory(patientId = it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun initView() {
        binding.tvNoHistory.text = requireContext().getString(R.string.no_medical_review_history)
        binding.tvReferralTicketTitle.text =
            requireContext().getString(R.string.medical_review_history)
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
                if (connectivityManager.isNetworkAvailable()) {
                    viewModel.getMedicalReviewHistory(
                        patientId = getPatientId(),
                        medicalTicketId = referred.id
                    )
                    viewModel.medicalTicketId = referred.id
                } else {
                    showErrorDialog(
                        getString(R.string.error),
                        getString(R.string.no_internet_error)
                    )
                }
                listPopupWindow?.dismiss()
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
            ivReload.safeClickListener(this@MedicalReviewHistoryFragment)
            ivNext.safeClickListener(this@MedicalReviewHistoryFragment)
            ivPrevious.safeClickListener(this@MedicalReviewHistoryFragment)
        }
    }

    private fun attachObservers() {
        viewModel.medicalReviewTicketLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    if (resource.data?.history.isNullOrEmpty() && viewModel.medicalTicketId.isNullOrBlank()) {
                        binding.groupHistoryList.gone()
                        binding.clLoaderProgress.gone()
                        binding.tvNoHistory.visible()
                    } else {
                        resource.data?.let {
                            handleSuccess()
                            setReferralTicket(it)
                        } ?: kotlin.run {
                            binding.groupHistoryList.gone()
                            binding.clLoaderProgress.gone()
                            binding.tvNoHistory.visible()
                        }
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

    private fun setReferralTicket(medicalReviewHistory: MedicalReviewHistory) {
        binding.tvNoHistory.gone()
        adapters.updateList(
            createMedicalReview(medicalReviewHistory)
        )
        adjustGuideline()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapters
        setReferralDates(medicalReviewHistory.history, medicalReviewHistory.id)
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
        if (referredDates != null && viewModel.medicalTicketId == null) {
            dateListAdapter.submitData(referredDates, id)
            viewModel.medicalTicketId = id
            viewModel.medicalReferralDates = referredDates
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
                if (connectivityManager.isNetworkAvailable()) {
                    getPreviousItemToCurrent()
                } else {
                    showErrorDialog(
                        getString(R.string.error),
                        getString(R.string.no_internet_error)
                    )
                }
            }

            binding.llHistoryAction.ivNext.id -> {
                if (connectivityManager.isNetworkAvailable()) {
                    getNextItemToCurrent()
                } else {
                    showErrorDialog(
                        getString(R.string.error),
                        getString(R.string.no_internet_error)
                    )
                }
            }

            binding.retryButtonBp.id -> {
                handleRetry()
            }
        }
    }

    private fun handleRetry() {
        if (connectivityManager.isNetworkAvailable()) {
            if (!viewModel.medicalTicketId.isNullOrBlank()) {
                getPatientId()?.takeIf { it.isNotBlank() }
                    ?.let {
                        viewModel.getMedicalReviewHistory(
                            patientId = it,
                            medicalTicketId = viewModel.medicalTicketId
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
        viewModel.medicalReferralDates.let {
            it.forEachIndexed { index, referredDate ->
                if (referredDate.id == viewModel.medicalTicketId) {
                    selectedIndex = index - 1
                }
            }
        }
        return selectedIndex
    }

    private fun checkNextItem(): Int {
        var selectedIndex = -1
        viewModel.medicalReferralDates.let {
            it.forEachIndexed { index, referredDate ->
                if ((referredDate.id == viewModel.medicalTicketId) && ((index + 1) < it.size)) {
                    selectedIndex = index + 1
                }
            }
        }
        return selectedIndex
    }

    private fun getPreviousItemToCurrent() {
        val selectedIndex = checkForPreviousItem()
        if (selectedIndex != -1) {
            viewModel.medicalReferralDates[selectedIndex].id?.let {
                viewModel.getMedicalReviewHistory(
                    patientId = getPatientId(),
                    medicalTicketId = it
                )
                viewModel.medicalTicketId = it
            }
        }
    }

    private fun getNextItemToCurrent() {
        val selectedIndex = checkNextItem()
        if (selectedIndex != -1) {
            viewModel.medicalReferralDates[selectedIndex].id?.let {
                viewModel.getMedicalReviewHistory(
                    patientId = getPatientId(),
                    medicalTicketId = it
                )
                viewModel.medicalTicketId = it
            }
        }
    }


    private fun createMedicalReview(medicalReviewHistory: MedicalReviewHistory): List<Map<String, String?>> {
        return when (medicalReviewHistory.type?.lowercase()) {
            Above5MedicalReview.lowercase() -> {
                listOf(
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.diagnosis),
                        DefinedParams.value to combineText(
                            medicalReviewHistory.reviewDetails?.diagnosis?.map { it.diseaseCategory },
                            ""
                        )
                    ),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.patient_status),
                        DefinedParams.value to (medicalReviewHistory.reviewDetails?.patientStatus?.takeIf { it.isNotBlank() }
                            ?: getString(R.string.separator_double_hyphen))
                    ),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.date_of_prescription),
                        DefinedParams.value to medicalReviewHistory.dateOfReview?.let {
                            DateUtils.convertDateFormat(
                                it,
                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                DateUtils.DATE_ddMMyyyy
                            )
                        }
                    ),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.presenting_complaints),
                        DefinedParams.value to combineText(
                            medicalReviewHistory.reviewDetails?.presentingComplaints,
                            medicalReviewHistory.reviewDetails?.presentingComplaintsNotes
                        )
                    ),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.clinical_notes),
                        DefinedParams.value to (medicalReviewHistory.reviewDetails?.clinicalNotes?.takeIf { it.isNotBlank() }
                            ?: getString(R.string.separator_double_hyphen)
                                )),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.systemic_examinations),
                        DefinedParams.value to combineText(
                            medicalReviewHistory.reviewDetails?.systemicExaminations,
                            medicalReviewHistory.reviewDetails?.systemicExaminationsNotes
                        )
                    )
                )
            }

            PregnancyAncMedicalReview.lowercase() -> {
                listOf(
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.diagnosis),
                        DefinedParams.value to combineText(
                            medicalReviewHistory.reviewDetails?.diagnosis?.map { it.diseaseCategory },
                            ""
                        )
                    ),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.patient_status),
                        DefinedParams.value to (medicalReviewHistory.reviewDetails?.patientStatus?.takeIf { it.isNotBlank() }
                            ?: getString(R.string.separator_double_hyphen))
                    ),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.date_of_prescription),
                        DefinedParams.value to medicalReviewHistory.dateOfReview?.let {
                            DateUtils.convertDateFormat(
                                it,
                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                DateUtils.DATE_ddMMyyyy
                            )
                        }
                    ),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.presenting_complaints),
                        DefinedParams.value to combineText(
                            medicalReviewHistory.reviewDetails?.presentingComplaints,
                            medicalReviewHistory.reviewDetails?.presentingComplaintsNotes
                        )
                    ),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.clinical_notes),
                        DefinedParams.value to (medicalReviewHistory.reviewDetails?.clinicalNotes?.takeIf { it.isNotBlank() }
                            ?: getString(R.string.separator_double_hyphen)
                                )),
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.obstetric_examination),
                        DefinedParams.value to combineText(
                            medicalReviewHistory.reviewDetails?.obstetricExaminations,
                            medicalReviewHistory.reviewDetails?.obstetricExaminationsNotes
                        )
                    )
                )
            }

            else -> {
                listOf<Map<String, String>>()
            }
        }
    }

    private fun combineText(items: List<String?>?, notes: String?): String {
        val combinedText = StringBuilder()
        items?.filterNotNull()?.takeIf { it.isNotEmpty() }?.joinToString(separator = ",")?.let {
            combinedText.append(it)
        }
        if (!notes.isNullOrEmpty()) {
            if (combinedText.isNotEmpty()) {
                combinedText.append(",")
            }
            combinedText.append(notes)
        }
        return if (combinedText.isNotEmpty()) combinedText.toString() else getString(R.string.separator_double_hyphen)
    }
}