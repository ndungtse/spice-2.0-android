package org.medtroniclabs.uhis.ui.household.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams.MemberDeceased
import org.medtroniclabs.uhis.appextensions.startBackgroundOfflineSync
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.databinding.FragmentMemberDeceasedDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.model.MemberDetailsSpinnerModel
import org.medtroniclabs.uhis.ui.household.MemberSelectionListener
import org.medtroniclabs.uhis.ui.household.viewmodel.HouseHoldSummaryViewModel
import timber.log.Timber

class MemberDeceasedDialogFragment() : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentMemberDeceasedDialogBinding
    private var listener: MemberSelectionListener? = null
    private val householdSummaryViewModel: HouseHoldSummaryViewModel by activityViewModels()

    constructor(listener: MemberSelectionListener) : this() {
        this.listener = listener
    }

    companion object {
        val TAG = "MemberEditDialogFragment"

        fun newInstance(listener: MemberSelectionListener): MemberDeceasedDialogFragment = MemberDeceasedDialogFragment(listener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMemberDeceasedDialogBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        householdSummaryViewModel.setUserJourney(MemberDeceased)
        attachListeners()
        loadSpinnerData()
        onEnterReason()
    }

    private fun onEnterReason() {
        binding.etReason.addTextChangedListener {
            Timber.d("onEnterReason = $it")
            binding.btnOkay.isEnabled = it.toString().isNotEmpty() && householdSummaryViewModel.hasDeceasedReason
        }
    }

    private fun loadSpinnerData() {
        val memberAdapter = EditMemberSpinnerAdapter(requireContext())
        val dropDownList = ArrayList<MemberDetailsSpinnerModel>()
        dropDownList.add(
            MemberDetailsSpinnerModel(
                id = DefinedParams.DefaultSelectID,
                name = DefinedParams.DefaultIDLabel,
            ),
        )
        householdSummaryViewModel.householdMembersLiveData.value?.let { data ->
            data.forEach { item ->
                if (item.isActive) {
                    dropDownList.add(
                        MemberDetailsSpinnerModel(
                            id = item.id,
                            name = item.name,
                            age = "",
                            gender = item.gender,
                            dob = item.dateOfBirth,
                        ),
                    )
                }
            }
        }
        memberAdapter.setData(dropDownList)
        binding.etMemberInput.adapter = memberAdapter
        binding.etMemberInput.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long,
                ) {
                    val selectedItem = memberAdapter.getData(position = p2)
                    Timber.d("onEnterReason 123= $selectedItem")

                    selectedItem?.let {
                        if (it.id != -1L) {
                            Timber.d("onEnterReason = $selectedItem")
                            householdSummaryViewModel.hasDeceasedReason = true
                            householdSummaryViewModel.selectedMemberId = it.id
                            householdSummaryViewModel.selectedMemberDob = it.dob
                            if (binding.etReason.text
                                    .toString()
                                    .isNotEmpty()
                            ) {
                                binding.btnOkay.isEnabled = true
                            }
                        } else {
                            Timber.d("onEnterReason = $selectedItem")
                            householdSummaryViewModel.hasDeceasedReason = false
                            binding.btnOkay.isEnabled = false
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    private fun attachListeners() {
        binding.btnOkay.safeClickListener(this)
        binding.imgClose.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnOkay.id -> {
                householdSummaryViewModel.updateMemberDeceasedReason(
                    householdSummaryViewModel.selectedMemberId,
                    false,
                    binding.etReason.text.toString(),
                )
                requireActivity().startBackgroundOfflineSync()
                householdSummaryViewModel.setUserJourney(AnalyticsDefinedParams.MEMBERDECEASEDSUMBITTRIGGERED)
                dismiss()
            }

            binding.imgClose.id -> {
                householdSummaryViewModel.setUserJourney(AnalyticsDefinedParams.MEMBERDECEASEDCLOSETRIGGERED)
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
    }
}
