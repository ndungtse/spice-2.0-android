package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentEmctVisitStatusDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.medicalreview.EMTCTVisitStatusRequest
import com.medtroniclabs.spice.model.medicalreview.HivVitalsRequest
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.EMTCCT_VISIT_STATUS
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums

class EmctVisitStatusDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentEmctVisitStatusDialogBinding
    private lateinit var emctTagView: TagListCustomView
    private val hivViewModel: HivViewModel by activityViewModels()
    var listener: DialogDismissListener? = null
    private val diagnosisViewModel: DiagnosisViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmctVisitStatusDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    companion object {
        const val TAG = "EmctVisitStatusDialogFragment"
        fun newInstance() = EmctVisitStatusDialogFragment()
    }

    private fun initView() {
        hivViewModel.getHivEmtctVistStatusByCategory(MedicalReviewTypeEnums.emtct_visit_status.name)
        binding.btnCancel.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
        binding.btnOkay.safeClickListener(this)
        binding.pageProgressBp.safeClickListener(this)
        emctTagView =
            TagListCustomView(binding.root.context, binding.emctChipGroup) { name, _, isChecked ->
                if (isChecked) {
                    binding.btnOkay.isEnabled = true
                    hivViewModel.selectedemtctVisitStatus =  emctTagView.getSelectedTags().firstOrNull()?.value
                } else {
                    binding.btnOkay.isEnabled = false
                    hivViewModel.selectedemtctVisitStatus = null
                }
            }
    }

    private fun attachObserver() {
        hivViewModel.hivEmtctStatusLiveData.observe(viewLifecycleOwner) { statusList ->
            hivViewModel.getHivVitalsDetailsbyType(
                request = HivVitalsRequest(
                    patientReference = hivViewModel.id,
                    memberId = hivViewModel.memberId,
                    types = listOf(EMTCCT_VISIT_STATUS)
                )
            )
            val emTctVisitStatusList = statusList.map {
                ChipViewItemModel(name = it.name, value = it.value)
            }
            emctTagView.addChipItemList(
                ArrayList(emTctVisitStatusList.distinct()),
                null
            )
        }

        hivViewModel.createEMTCTVistStatusLiveData.observe(viewLifecycleOwner) { response ->
            when (response.state) {
                ResourceState.LOADING -> {
                    binding.loader.visible()
                }

                ResourceState.SUCCESS -> {
                    binding.loader.gone()
                    dismiss()
                    listener?.onDialogDismissed(isBp = false, true)
                    hivViewModel.createEMTCTVistStatusLiveData.postError()
                }

                ResourceState.ERROR -> {
                    binding.loader.gone()
                }
            }
        }


        hivViewModel.hivVitalsByTypeLiveData.observe(viewLifecycleOwner) { response ->
            when (response.state) {
                ResourceState.LOADING -> {
                    binding.loader.visible()
                }

                ResourceState.SUCCESS -> {
                    binding.loader.gone()
                    response.data?.let { response ->
                        hivViewModel.hivEmtctStatusLiveData.value?.let { meta ->
                            val emTctVisitStatusList = meta.map {
                                ChipViewItemModel(name = it.name, value = it.value)
                            }
                            val selectedEmtctVisitStatus = ArrayList<ChipViewItemModel>()
                            if (!response.emtctVisitStatus.isNullOrEmpty()) {
                                selectedEmtctVisitStatus.add(
                                    ChipViewItemModel(
                                        name = response.emtctVisitStatus,
                                        value = response.emtctVisitStatus
                                    )
                                )
                            }
                            emctTagView.addChipItemList(
                                emTctVisitStatusList.distinct(),
                                selectedEmtctVisitStatus.distinct()
                            )
                        }
                    }

                }

                ResourceState.ERROR -> {
                    binding.loader.gone()
                }
            }
        }


    }


    override fun onClick(view: View?) {
        when (view?.id) {
            binding.btnCancel.id -> dismiss()
            binding.btnOkay.id -> createEmtctVisitStatus()
            binding.ivClose.id -> dismiss()
        }
    }

    private fun createEmtctVisitStatus() {
        val request = hivViewModel.selectedemtctVisitStatus?.let { emtctVisitStatus ->
            EMTCTVisitStatusRequest(
                MedicalReviewEncounter(
                    patientId = hivViewModel.patientId,
                    provenance = ProvanceDto(),
                    villageId = hivViewModel.villageId,
                    longitude = hivViewModel.lastLocation?.longitude,
                    latitude = hivViewModel.lastLocation?.latitude,
                    startTime = DateUtils.getCurrentDateAndTime(DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
                    endTime = DateUtils.getCurrentDateAndTime(DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
                ),
                stringValue = emtctVisitStatus
            )
        }
        if (request != null) {
            hivViewModel.createEMTCT(request)
        }
    }


}