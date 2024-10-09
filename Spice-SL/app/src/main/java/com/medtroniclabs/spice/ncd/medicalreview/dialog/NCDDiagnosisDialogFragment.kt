package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentNcdDiagnosisBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetRequest
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisItem
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisRequestResponse
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.CONFIRM_DIAGNOSIS_TYPE
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDDiagnosisViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDDiagnosisDialogFragment : DialogFragment(), View.OnClickListener {
    var listener: NCDDialogDismissListener? = null
    private lateinit var binding: FragmentNcdDiagnosisBinding
    private val viewModel: NCDDiagnosisViewModel by viewModels()
    private lateinit var tagListCustomView: TagListCustomView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdDiagnosisBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "NCDDiagnosisDialogFragment"
        fun newInstance(patientId: String, types: ArrayList<String>,isFemale: Boolean) =
            NCDDiagnosisDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(DefinedParams.PatientId, patientId)
                    putStringArrayList(CONFIRM_DIAGNOSIS_TYPE, types)
                    putBoolean(NCDMRUtil.IS_FEMALE, isFemale)
                }
            }
    }

    private fun getTypes(): ArrayList<String> {
        return arguments?.getStringArrayList(CONFIRM_DIAGNOSIS_TYPE) ?: arrayListOf()
    }

    private fun getGender(): String {
        return if (arguments?.getBoolean(NCDMRUtil.IS_FEMALE) == true) {
            Screening.Female.lowercase()
        } else {
            Screening.Male.lowercase()
        }
    }

    fun getPatientId(): String? {
        return arguments?.getString(DefinedParams.PatientId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.getChipLiveData.observe(viewLifecycleOwner) {
            val complaintList = it.map { item ->
                ChipViewItemModel(
                    id = item.id,
                    name = item.name,
                    type = item.type,
                    value = item.value
                )
            } as ArrayList<ChipViewItemModel>
            setChipItems(complaintList)
            getDiagonsis()
        }

        viewModel.createConfirmDiagonsis.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {

                }

                ResourceState.SUCCESS -> {
                    dismiss()
                    listener?.onDialogDismissed()
                }

                ResourceState.ERROR -> {
                    // error dialog
                }
            }
        }

        viewModel.getConfirmDiagonsis.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {

                }

                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        viewModel.getChipLiveData.value?.let { liveData ->
                            data.confirmDiagnosis?.mapNotNull { it.value }?.let { values ->
                                val filteredChips = liveData.filter { db ->
                                    values.any { db.value.equals(it, true) }
                                }.map { item ->
                                    ChipViewItemModel(
                                        id = item.id,
                                        name = item.name,
                                        type = item.type,
                                        value = item.value
                                    )
                                }

                                if (filteredChips.isNotEmpty()) {
                                    viewModel.selectedChips.apply {
                                        clear()
                                        addAll(filteredChips)
                                    }
                                }
                            }
                        }
                    }
                }

                ResourceState.ERROR -> {

                }
            }
        }
    }

    private fun getDiagonsis() {
        getPatientId()?.let { patientId ->
            viewModel.getConfirmDiagonsis(
                NCDDiagnosisGetRequest(
                    patientReference = patientId,
                    diagnosisType = getTypes()
                )
            )
        }
    }

    private fun setChipItems(complaintList: ArrayList<ChipViewItemModel>) {
        tagListCustomView =
            TagListCustomView(
                binding.root.context,
                binding.cgDiagnosis,
                callBack = { _, _, _ ->
                    viewModel.selectedChips =
                        ArrayList(tagListCustomView.getSelectedTags())
                }
            )

        tagListCustomView.addChipItemList(
            complaintList,
            viewModel.selectedChips,
            diagnosisGrouping(complaintList)
        )
    }

    private fun diagnosisGrouping(list: List<ChipViewItemModel>?): HashMap<String, MutableList<ChipViewItemModel>>? {
        return list?.groupByTo(HashMap(), { it.type.toString() }, { it })
    }

    private fun initView() {
        viewModel.getChip(getTypes().map { it.lowercase() }, getGender())
        binding.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnConfirm.safeClickListener(this)
        MotherNeonateUtil.initTextWatcherForString(binding.etCommentDiagnosis) {
            viewModel.comments = it
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivClose.id, binding.btnCancel.id -> dismiss()
            binding.btnConfirm.id -> {
                if (validateInput()) {
                    createDiagonsis()
                }
            }
        }
    }

    private fun createDiagonsis() {
        getPatientId()?.let { patientId ->
            val request = NCDDiagnosisRequestResponse(
                ProvanceDto(),
                diagnosisNotes = viewModel.comments.takeIf { it.isNotBlank() },
                confirmDiagnosis = viewModel.selectedChips.map { chip ->
                    NCDDiagnosisItem(chip.type, chip.value)
                },
                patientReference = patientId
            )
            viewModel.createConfirmDiagonsis(request)
        }
    }

    fun validateInput(): Boolean {
        val isValid = viewModel.selectedChips.isNotEmpty()
        if (isValid) {
            binding.tvErrorMessage.gone()
        } else {
            binding.tvErrorMessage.visible()
        }
        return isValid
    }
}