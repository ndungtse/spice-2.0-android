package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.loadAsGif
import com.medtroniclabs.spice.appextensions.resetImageView
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentNcdDiagnosisBinding
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetRequest
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisItem
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisRequestResponse
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.CONFIRM_DIAGNOSIS_TYPE
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.CONFIRM_DIAGNOSIS_TYPE_GET
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.IsPregnant
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDDiagnosisViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDDiagnosisDialogFragment : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentNcdDiagnosisBinding
    private val viewModel: NCDDiagnosisViewModel by viewModels()
    private lateinit var tagListCustomView: TagListCustomView
    var listener: NCDDialogDismissListener? = null

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
        fun newInstance(
            patientId: String?,
            memberId: String?,
            types: ArrayList<String>,
            isFemale: Boolean,
            getTypes: ArrayList<String>,
            isPregnant: Boolean,
            type: String? = null
        ) = NCDDiagnosisDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(DefinedParams.PatientId, patientId)
                    putString(DefinedParams.MemberID, memberId)
                    putStringArrayList(CONFIRM_DIAGNOSIS_TYPE, types)
                    putStringArrayList(CONFIRM_DIAGNOSIS_TYPE_GET, getTypes)
                    putBoolean(NCDMRUtil.IS_FEMALE, isFemale)
                    putBoolean(IsPregnant, isPregnant)
                    putString(Screening.Type,type)
                }
            }
    }

    private fun getTypes(): ArrayList<String> {
        return arguments?.getStringArrayList(CONFIRM_DIAGNOSIS_TYPE) ?: arrayListOf()
    }

    private fun getConfirmDiagnosisTypes(): ArrayList<String> {
        return arguments?.getStringArrayList(CONFIRM_DIAGNOSIS_TYPE_GET) ?: arrayListOf()
    }

    private fun isPregnant(): Boolean {
        return arguments?.getBoolean(IsPregnant) ?: false
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
    fun getMemberId(): String? {
        return arguments?.getString(DefinedParams.MemberID)
    }
    private fun getTypeForRequest(): String? {
        return NCDMRUtil.requestTypeForConfirmDiagnoses(arguments?.getString(Screening.Type))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.getChipLiveData.observe(viewLifecycleOwner) {
            setChipItems(it)
            getDiagonsis()
        }

        viewModel.createConfirmDiagonsis.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    dismiss()
                    listener?.onDialogDismissed(true)
                }

                ResourceState.ERROR -> {
                    // error dialog
                    hideLoading()
                }
            }
        }

        viewModel.getConfirmDiagonsis.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { data ->
                        viewModel.getChipLiveData.value?.let { liveData ->
                            data.diagnosis?.mapNotNull { it.value }?.let { values ->
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
                                if (!data.diagnosisNotes.isNullOrBlank()) {
                                    binding.etCommentDiagnosis.setText(data.diagnosisNotes.takeIf { it.isNotBlank() })
                                }

                                if (filteredChips.isNotEmpty()) {
                                    viewModel.selectedChips.apply {
                                        clear()
                                        addAll(filteredChips)
                                    }
                                    setChipItems(liveData)
                                }
                            }
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun getDiagonsis() {
        viewModel.getConfirmDiagonsis(
            NCDDiagnosisGetRequest(
                patientReference = getPatientId(),
                memberReference = getMemberId(),
                diagnosisType = getConfirmDiagnosisTypes()
            )
        )
    }

    private fun setChipItems(ncdDiagnosisEntities: List<NCDDiagnosisEntity>) {
        val complaintList = ncdDiagnosisEntities.map { item ->
            ChipViewItemModel(
                id = item.id,
                name = item.name,
                type = item.type,
                value = item.value
            )
        } as ArrayList<ChipViewItemModel>
        addChip(complaintList)
    }

    private fun addChip(complaintList: ArrayList<ChipViewItemModel>) {
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
        viewModel.getChip(getTypes().map { it.lowercase() }, getGender(),isPregnant())
        tagListCustomView =
            TagListCustomView(
                binding.root.context,
                binding.cgDiagnosis,
                callBack = { _, _, _ ->
                    viewModel.selectedChips =
                        ArrayList(tagListCustomView.getSelectedTags())
                }
            )
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
        val request = NCDDiagnosisRequestResponse(
            ProvanceDto(),
            diagnosisNotes = viewModel.comments.takeIf { it.isNotBlank() },
            confirmDiagnosis = viewModel.selectedChips.map { chip ->
                NCDDiagnosisItem(type = chip.type, value = chip.value)
            },
            patientReference = getPatientId(),
            memberReference = getMemberId(),
            type = getTypeForRequest()
        )
        viewModel.createConfirmDiagonsis(request)
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

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
        binding.loaderImage.apply {
            loadAsGif(R.drawable.loader_spice)
        }
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
        binding.loaderImage.apply {
            resetImageView()
        }
    }
}