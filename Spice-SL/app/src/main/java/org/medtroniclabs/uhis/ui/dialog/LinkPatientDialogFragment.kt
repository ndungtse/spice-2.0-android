package org.medtroniclabs.uhis.ui.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.databinding.FragmentLinkpatientDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ui.household.HouseholdActivity
import org.medtroniclabs.uhis.ui.household.HouseholdDefinedParams
import org.medtroniclabs.uhis.ui.household.viewmodel.HouseHoldSummaryViewModel
import org.medtroniclabs.uhis.ui.landing.OnDialogDismissListener
import org.medtroniclabs.uhis.ui.phuwalkins.listener.LinkSuccessListener

class LinkPatientDialogFragment : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentLinkpatientDialogBinding
    private var onDismissListener: OnDialogDismissListener? = null
    private var onLinkSuccessListener: LinkSuccessListener? = null

    private val householdSummaryViewModel: HouseHoldSummaryViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onDismissListener = context as OnDialogDismissListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLinkpatientDialogBinding.inflate(layoutInflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "LinkPatientDialogFragment"

        fun newInstance(
            houseHoldID: Long,
            memberID: Long,
            fhirMemberId: Long,
        ): LinkPatientDialogFragment {
            val bundle = Bundle().apply {
                putLong(DefinedParams.householdId, houseHoldID)
                putLong(DefinedParams.MEMBER_ID, memberID)
                putLong(DefinedParams.FhirMemberID, fhirMemberId)
            } // Assuming memberID is passed as argument}
            val fragment = LinkPatientDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        householdSummaryViewModel.setUserJourney(AnalyticsDefinedParams.PHUWALKINSCREENLINKCONFIRMATION)
        setListener()
    }

    private fun setListener() {
        binding.btnLink.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnLink.id -> {
                householdSummaryViewModel.setUserJourney(AnalyticsDefinedParams.LINKPATIENT)
                val intent = Intent(requireActivity(), HouseholdActivity::class.java)
                intent.putExtra(
                    HouseholdDefinedParams.IS_PHU_WALK_INS_FLOW,
                    true,
                )
                intent.putExtra(DefinedParams.MEMBER_ID, arguments?.getLong(DefinedParams.MEMBER_ID, -1L))
                intent.putExtra(DefinedParams.FhirMemberID, arguments?.getLong(DefinedParams.FhirMemberID, -1L))
                intent.putExtra(
                    DefinedParams.householdId,
                    arguments?.getLong(DefinedParams.householdId, -1L),
                )
                startActivity(intent)
                dismiss()
            }

            binding.btnCancel.id -> {
                dismiss()
                householdSummaryViewModel.setUserJourney(AnalyticsDefinedParams.CANCELBUTTONTRIGGERED)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissListener = null
    }
}
