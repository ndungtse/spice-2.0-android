package com.medtroniclabs.spice.ui.medicalreview.familyplan.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentContraceptivesBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel.ContraceptivesViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ClientType
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CombineOralContraceptive
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CombinedOralContraceptiveComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Condoms
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.EmergencyContraceptive
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Implants
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Injectables
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Microlut
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.MicrolutQuantity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherFPMethod
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherImplantComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherImplantSpecify
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherInjectableComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherInjectablesSpecify
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherOralSpecify
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherPermanentMethodComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherProgestinOnlyOralsComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherSpecify
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PermanentMethod
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PostPartum
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ProgestinOnlyOrals
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContraceptivesFragment : BaseFragment() {
    private lateinit var binding: FragmentContraceptivesBinding
    private lateinit var iucdTagView: TagListCustomView
    private val viewModel: ContraceptivesViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContraceptivesBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initializeListener()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.contraceptiveMetaList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resource.data?.let {
                        initializeMetaItems(it)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun initializeMetaItems(metaList: List<MedicalReviewMetaItems>) {
        addCustomView(
            getSingleSelectionViewData(metaList.filter { it.category.equals(MedicalReviewTypeEnums.client_type.name, true) }.sortedBy { it.displayOrder }),
            ClientType,
            viewModel.resultHashMap,
            clientTypeSelectionCallBack,
            binding.ClientTypeRoot
        )

        addCustomView(
            getSingleSelectionViewData(metaList.filter { it.category.equals(MedicalReviewTypeEnums.combined_oral_contraceptive.name, true) }.sortedBy { it.displayOrder }),
            CombineOralContraceptive,
            viewModel.resultHashMap,
            combinedOralSelectionCallBack,
            binding.CombinedOralContraceptiveRoot
        )
        addCustomView(
            getSingleSelectionViewData(metaList.filter { it.category.equals(MedicalReviewTypeEnums.progestin.name, true) }.sortedBy { it.displayOrder }),
            ProgestinOnlyOrals,
            viewModel.resultHashMap,
            progestinSelectionCallBack,
            binding.ProgestinOnlyOralsRoot
        )

        addCustomView(
            getSingleSelectionViewData(metaList.filter { it.category.equals(MedicalReviewTypeEnums.injectables.name, true) }.sortedBy { it.displayOrder }),
            Injectables,
            viewModel.resultHashMap,
            injectableSelectionCallBack,
            binding.InjectableRoot
        )

        addCustomView(
            getSingleSelectionViewData(metaList.filter { it.category.equals(MedicalReviewTypeEnums.implants.name, true) }.sortedBy { it.displayOrder }),
            Implants,
            viewModel.resultHashMap,
            implantsSelectionCallBack,
            binding.ImplantsRoot
        )

        addCustomView(
            getSingleSelectionViewData(metaList.filter { it.category.equals(MedicalReviewTypeEnums.condoms.name, true) }.sortedBy { it.displayOrder }),
            Condoms,
            viewModel.resultHashMap,
            condomsSelectionCallBack,
            binding.CondomsRoot
        )

        addCustomView(
            getSingleSelectionViewData(metaList.filter { it.category.equals(MedicalReviewTypeEnums.emergency_contraceptive.name, true) }.sortedBy { it.displayOrder }),
            EmergencyContraceptive,
            viewModel.resultHashMap,
            emergencyContraSelectionCallBack,
            binding.EmergencyContraceptiveRoot
        )

        addCustomView(
            getSingleSelectionViewData(metaList.filter { it.category.equals(MedicalReviewTypeEnums.permanent_method.name, true) }.sortedBy { it.displayOrder }),
            PermanentMethod,
            viewModel.resultHashMap,
            permanentMethodSelectionCallBack,
            binding.PermanentMethodRoot
        )

        val iucdListItem =
            metaList.filter { it.category.equals(MedicalReviewTypeEnums.iucd.name, true) }
        val chipItemList = mutableListOf<ChipViewItemModel>()
        iucdListItem.forEach {
            chipItemList.add(
                ChipViewItemModel(
                    id = it.id,
                    name = it.name,
                    value = it.value
                )
            )
            iucdTagView.addChipItemList(chipItemList, viewModel.selectedIUCD)
        }
    }

    private fun initializeListener() {
        binding.etOtherInjectableComments.addTextChangedListener { notes ->
            viewModel.otherInjectableComments = if (notes.isNullOrBlank()) null else notes.toString()
        }
        binding.etOtherImplantsComments.addTextChangedListener { notes ->
            viewModel.otherImplantComments = if (notes.isNullOrBlank()) null else notes.toString()
        }
        binding.etOtherPermanentMethodComments.addTextChangedListener { notes ->
            viewModel.otherPermanentMethodComments = if (notes.isNullOrBlank()) null else notes.toString()
        }
        binding.etProgestinOnlyOralsComments.addTextChangedListener { notes ->
            viewModel.otherProgestinOnlyOralsComments = if (notes.isNullOrBlank()) null else notes.toString()
        }
        binding.etCombinedOralContraceptiveComments.addTextChangedListener { notes ->
            viewModel.combinedOralContraceptiveComments = if (notes.isNullOrBlank()) null else notes.toString()
        }
    }

    private fun initViews() {
        viewModel.getMetaList(MedicalReviewTypeEnums.FAMILY_PLANNING_REVIEW.name)

        iucdTagView = TagListCustomView(
            binding.root.context,
            binding.tagViewIUCD
        ) { _, _, _ ->
            viewModel.selectedIUCD = ArrayList(iucdTagView.getSelectedTags())
        }

        binding.etQuantity.addTextChangedListener { editable ->
            val quantityString = editable.toString()
            viewModel.quantity = if (quantityString.isBlank()) null else try {
                quantityString.toLong()
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getSingleSelectionViewData(metaList: List<MedicalReviewMetaItems>): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        metaList.forEach {
            flowList.add(
                getOptionMap(
                    it.value ?: it.name,
                    it.name
                )
            )
        }
        return flowList
    }

    private fun addCustomView(
        data: ArrayList<Map<String, Any>>,
        tag: String,
        hashMap: HashMap<String, Any>,
        callback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)?,
        container: ViewGroup?
    ) {
        SingleSelectionCustomView(binding.root.context).apply {
            this.tag = tag
            addViewElements(
                data,
                false,
                hashMap,
                Pair(tag, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                callback
            )
            container?.addView(this)
        }
    }

    private var clientTypeSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[ClientType] = selectedID as String
            if(selectedID.equals(PostPartum, true)){
                binding.tvPostPartum.visible()
                viewModel.contraceptiveMetaList.value?.data?.let { metaList ->
                    addCustomView(
                        getSingleSelectionViewData(metaList.filter {
                            it.category.equals(
                                MedicalReviewTypeEnums.post_partum.name,
                                true
                            )
                        }.sortedBy { it.displayOrder }),
                        PostPartum,
                        viewModel.resultHashMap,
                        postPartumSelectionCallBack,
                        binding.PostPartumRoot
                    )
                }
                binding.PostPartumRoot.visible()
            }else{
                viewModel.resultHashMap.remove(PostPartum)
                binding.tvPostPartum.gone()
                binding.PostPartumRoot.removeAllViews()
                binding.PostPartumRoot.gone()
            }
            resultMapChanged()
        }

    private var postPartumSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[PostPartum] = selectedID as String
            resultMapChanged()
        }

    private var combinedOralSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[CombineOralContraceptive] = selectedID as String
            if(selectedID.equals(OtherOralSpecify, true)){
                binding.etCombinedOralContraceptiveComments.visible()
            }else{
                binding.etCombinedOralContraceptiveComments.gone()
                binding.etCombinedOralContraceptiveComments.text?.clear()
                viewModel.resultHashMap.remove(CombinedOralContraceptiveComments)
            }
            resultMapChanged()
        }

    private var progestinSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[ProgestinOnlyOrals] = selectedID as String
            when(selectedID.lowercase()){
                Microlut.lowercase() -> {
                    binding.tvQuantityLabel.visible()
                    binding.etQuantity.visible()
                    binding.etProgestinOnlyOralsComments.text?.clear()
                    binding.etProgestinOnlyOralsComments.gone()
                    viewModel.resultHashMap.remove(OtherProgestinOnlyOralsComments)
                }
                OtherSpecify.lowercase() -> {
                    binding.etProgestinOnlyOralsComments.visible()
                    binding.etQuantity.text?.clear()
                    viewModel.resultHashMap.remove(MicrolutQuantity)
                    binding.tvQuantityLabel.gone()
                    binding.etQuantity.gone()
                }
            }
            resultMapChanged()
        }

    private var injectableSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[Injectables] = selectedID as String
            if(selectedID .equals(OtherInjectablesSpecify, true)){
                binding.etOtherInjectableComments.visible()
            }else{
                binding.etOtherInjectableComments.gone()
                binding.etOtherInjectableComments.text?.clear()
                viewModel.resultHashMap.remove(OtherInjectableComments)
            }
            resultMapChanged()
        }

    private var implantsSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[Implants] = selectedID as String
            if(selectedID.equals(OtherImplantSpecify, true)){
                binding.etOtherImplantsComments.visible()
            }else{
                binding.etOtherImplantsComments.gone()
                binding.etOtherImplantsComments.text?.clear()
                viewModel.resultHashMap.remove(OtherImplantComments)
            }
            resultMapChanged()
        }

    private var condomsSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[Condoms] = selectedID as String
            resultMapChanged()
        }

    private var emergencyContraSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[EmergencyContraceptive] = selectedID as String
            resultMapChanged()
        }

    private var permanentMethodSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[PermanentMethod] = selectedID as String
            if (selectedID.equals(OtherFPMethod, true)) {
                binding.etOtherPermanentMethodComments.visible()
            } else {
                binding.etOtherPermanentMethodComments.gone()
                binding.etOtherPermanentMethodComments.text?.clear()
                viewModel.resultHashMap.remove(OtherPermanentMethodComments)
            }
            resultMapChanged()
        }

    private fun resultMapChanged(){
        setFragmentResult(
            MedicalReviewDefinedParams.CONTRACEPTIVES_ITEMS, bundleOf(
                MedicalReviewDefinedParams.CONTRACEPTIVES_VALUES to true)
        )
    }

    fun validInputs():Boolean{
        var isValid = true

        isValid = checkAndToggleError(ClientType, binding.tvClientTypeErrorMessage)
        if ((viewModel.resultHashMap[ClientType] as? String)?.equals(PostPartum, true) == true) {
            checkAndToggleError(PostPartum, binding.tvPostPartumErrorMessage)
        } else {
            binding.tvPostPartumErrorMessage.gone()
        }

        checkAndToggleError(ProgestinOnlyOrals, binding.tvProgestinErrorMessage).let {
            isValid = if(it){
                checkMicrolut()
            }else{
                false
            }
        }

        listOf(
            checkAndToggleError(CombineOralContraceptive, binding.tvContraceptivesErrorMessage),
            checkAndToggleError(Injectables, binding.tvInjectablesErrorMessage),
            checkAndToggleError(Implants, binding.tvImplantErrorMessage),
            checkAndToggleError(Condoms, binding.tvCondomsErrorMessage),
            checkAndToggleError(
                EmergencyContraceptive,
                binding.tvEmergencyContraceptiveErrorMessage
            ),
            checkAndToggleError(PermanentMethod, binding.tvPermanentMethodErrorMessage)
        ).forEach {
            if (!it) {
                isValid = false
            }
        }

        return isValid
    }

    private fun checkAndToggleError(key: String, errorView: View) :Boolean{
        if (viewModel.resultHashMap.containsKey(key)) {
            errorView.gone()
            return true
        } else {
            errorView.visible()
            return false
        }
    }

    private fun checkMicrolut():Boolean{
       if ((viewModel.resultHashMap[ProgestinOnlyOrals] as? String)?.equals(Microlut, true) == true) {
            if (viewModel.quantity == null) {
                binding.tvQuantityErrorMessage.visible()
                return false
            } else {
                binding.tvQuantityErrorMessage.gone()
                return true
            }
        } else {
            binding.tvQuantityErrorMessage.gone()
           return true
        }
    }


    companion object {
        const val TAG = "ContraceptivesFragment"
    }
}