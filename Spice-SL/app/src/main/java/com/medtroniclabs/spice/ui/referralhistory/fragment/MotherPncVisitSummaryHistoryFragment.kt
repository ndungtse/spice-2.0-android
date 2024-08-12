package com.medtroniclabs.spice.ui.referralhistory.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setExpandableText
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.CommonUtils.getBooleanAsString
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.PncChildMedicalReview
import com.medtroniclabs.spice.databinding.PncMedicalHistoryFragmentBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.referralhistory.viewmodel.ReferralHistoryViewModel

class MotherPncVisitSummaryHistoryFragment(private var pncDetails: PncChildMedicalReview?) : BaseFragment() {
    lateinit var binding: PncMedicalHistoryFragmentBinding
    val viewModel: ReferralHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PncMedicalHistoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "MotherPncVisitSummaryHistoryFragment"
        fun newInstance(pncDetails: PncChildMedicalReview?): MotherPncVisitSummaryHistoryFragment {
            return MotherPncVisitSummaryHistoryFragment(pncDetails)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       if (pncDetails?.id!=null){
           initializeMotherSummaryDetails(pncDetails)
           initializeNeonateSummaryDetails(pncDetails)
       }
    }


    private fun initializeMotherSummaryDetails(data: PncChildMedicalReview?) {
        binding.motherSummary.apply {
            tvTitle.text = getString(R.string.pnc_visit_summary_mother)
            tvPncVisitNoText.text =
                ((data?.reviewDetails?.pncMother?.visitNumber) ?: getString(R.string.empty__)).toString()
            tvPresentingComplaintsText.text =
                (data?.reviewDetails?.pncMother?.presentingComplaints?.joinToString(", "))
                    ?: getString(R.string.empty__)
            tvClinicalNotesText.setExpandableText(
                fullText = (data?.reviewDetails?.pncMother?.clinicalNotes) ?: getString(R.string.empty__),
                moreColorResId = R.color.purple_700,
                title = tvClinicalNotesLabel.text.toString(),
                activity = (requireActivity() as BaseActivity)
            )
            val list = mutableListOf<HashMap<String, Pair<String?, Any?>>>()
            data?.reviewDetails?.pncMother?.breastCondition?.let { breastCondition ->
                data.reviewDetails.pncMother.breastConditionNotes.let { breastConditionNotes ->
                    list.add(
                        hashMapOf(
                            getString(R.string.breast_condition) to Pair(
                                breastCondition,
                                breastConditionNotes
                            )
                        )
                    )
                }
            }
            data?.reviewDetails?.pncMother?.involutionsOfTheUterus?.let { involutionsOfTheUterus ->
                data.reviewDetails.pncMother.involutionsOfTheUterusNotes.let { involutionsOfTheUterusNotes ->
                    list.add(
                        hashMapOf(
                            getString(R.string.involutions_of_the_nuterus_summary) to Pair(
                                involutionsOfTheUterus,
                                involutionsOfTheUterusNotes
                            )
                        )
                    )
                }
            }
            tvExaminationsText.text =
                list.let { CommonUtils.createMotherNeonateExamination(it, requireContext(), true) }
                    ?.takeIf { it.isNotEmpty() }
                    ?: requireContext().getString(R.string.empty__)


            tvPrescriptionsText.text = data?.reviewDetails?.pncMother?.prescriptions.let {
                CommonUtils.createPrescription(
                    it,
                    requireContext()
                )
            }?.takeIf { it.isNotEmpty() }
                ?: requireContext().getString(R.string.empty__)

            binding.motherSummary.tvNextMedicalReviewLabelTextNot.text=data?.nextVisitDate?:getString(R.string.empty__)
            binding.motherSummary.historyFlow.visible()
            val textView = binding.motherSummary.tvPncVisitNoLabel
            val params = textView.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = R.id.tvPatientStatusLabel // First view
            textView.layoutParams = params
            binding.motherSummary.historyFlowNot.gone()
            binding.motherSummary.tvPatientStatusText.text=data?.patientStatus?:getString(R.string.empty__)

            tvAncVisitText.text =
                data?.reviewDetails?.pncMother?.diagnosis?.let { list ->
                    if (list.isNotEmpty()) {
                        binding.motherSummary.tvAncVisitText.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.a_red_error
                            )
                        )
                    }
                    convertListToString(
                        ArrayList(list.map { it.diseaseCategory }.distinct())
                    )
                } ?: requireContext().getString(R.string.empty__)

        }
    }


    private fun initializeNeonateSummaryDetails(data: PncChildMedicalReview?) {
        binding.neonateSummary.neonateflow.gone()
        binding.neonateSummary.apply {
            tvTitle.text = getString(R.string.pnc_visit_summary_neonate)
            tvPncVisitNoText.text = (data?.reviewDetails?.pncChild?.visitNumber.toString())
            tvPresentingComplaintsText.text =
                (data?.reviewDetails?.pncChild?.presentingComplaints?.joinToString(", "))
                    ?: getString(R.string.empty__)
            tvClinicalNotesText.setExpandableText(
                fullText = (data?.reviewDetails?.pncChild?.clinicalNotes) ?: getString(R.string.empty__),
                moreColorResId = R.color.purple_700,
                title = tvClinicalNotesLabel.text.toString(),
                activity = (requireActivity() as BaseActivity)
            )
            val list = mutableListOf<HashMap<String, Pair<String?, Any?>>>()
            data?.reviewDetails?.pncChild?.congenitalDetect?.let { congenitalDetect ->
                list.add(
                    hashMapOf(
                        getString(R.string.congenital_detect) to Pair(
                            getBooleanAsString(congenitalDetect.toBoolean()).capitalizeFirstChar(),
                            null
                        )
                    )
                )
            }
            data?.reviewDetails?.pncChild?.cordExamination?.let { cordExamination ->
                list.add(
                    hashMapOf(
                        getString(R.string.cord_examination) to Pair(
                            cordExamination,
                            null
                        )
                    )
                )
            }
            tvExaminationsLabel.text = getString(R.string.physical_examinations)

            tvExaminationsText.text = list.let {
                CommonUtils.createMotherNeonateExamination(
                    it,
                    requireContext(),
                    false
                )
            }?.takeIf { it.isNotEmpty() }
                ?: requireContext().getString(R.string.empty__)
        }
    }

}



