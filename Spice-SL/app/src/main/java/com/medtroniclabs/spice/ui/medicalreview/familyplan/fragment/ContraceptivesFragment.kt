package com.medtroniclabs.spice.ui.medicalreview.familyplan.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentContraceptivesBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel.ContraceptivesViewModel
import com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel.FamilyPlanViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ClientType
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CombineOralContraceptive
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Condoms
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.EmergencyContraceptive
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Implants
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Injectables
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Microlut
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PermanentMethod
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PostPartum
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ProgestinOnlyOrals
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.truncate

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
    }

    private fun initViews() {
        addCustomView(
            getContraceptivesFlowData(),
            ClientType,
            viewModel.resultHashMap,
            clientTypeSelectionCallBack,
            binding.ClientTypeRoot
        )

        addCustomView(
            getCombinedOralContraceptiveData(),
            CombineOralContraceptive,
            viewModel.resultHashMap,
            combinedOralSelectionCallBack,
            binding.CombinedOralContraceptiveRoot
        )

        addCustomView(
            getProgestinOralData(),
            ProgestinOnlyOrals,
            viewModel.resultHashMap,
            progestinSelectionCallBack,
            binding.ProgestinOnlyOralsRoot
        )

        addCustomView(
            getInjectables(),
            Injectables,
            viewModel.resultHashMap,
            injectableSelectionCallBack,
            binding.InjectableRoot
        )

        addCustomView(
            getImplant(),
            Implants,
            viewModel.resultHashMap,
            implantsSelectionCallBack,
            binding.ImplantsRoot
        )

        addCustomView(
            getCondoms(),
            Condoms,
            viewModel.resultHashMap,
            condomsSelectionCallBack,
            binding.CondomsRoot
        )

        addCustomView(
            getEmergencyContraceptiveData(),
            EmergencyContraceptive,
            viewModel.resultHashMap,
            emergencyContraSelectionCallBack,
            binding.EmergencyContraceptiveRoot
        )

        addCustomView(
            getPermanentMethods(),
            PermanentMethod,
            viewModel.resultHashMap,
            permanentMethodSelectionCallBack,
            binding.PermanentMethodRoot
        )

        iucdTagView = TagListCustomView(
            binding.root.context,
            binding.tagViewIUCD
        ) { _, _, _ ->
            viewModel.selectedIUCD = ArrayList(iucdTagView.getSelectedTags())

        }

        addChipItem()
        binding.etQuantity.setOnEditorActionListener{
            _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                viewModel.quantity = binding.etQuantity.text.toString()
                hideKeyboard(binding.etQuantity)
                resultMapChanged()
                return@setOnEditorActionListener true
            }else{
                return@setOnEditorActionListener false
            }
        }
    }

    private fun addChipItem() {
        val chipItemList = mutableListOf<ChipViewItemModel>()
        chipItemList.add(
            ChipViewItemModel(
                id = 1,
                name = getString(R.string.copper_t),
                value = getString(R.string.copper_t)
            )
        )
        iucdTagView.addChipItemList(chipItemList, viewModel.selectedIUCD)
    }

    private fun getContraceptivesFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(
            getOptionMap(
                getString(R.string.new_family_planning_method),
                getString(R.string.new_family_planning_method)
            )
        )
        flowList.add(
            getOptionMap(
                getString(R.string.continuing_family_planning_method),
                getString(R.string.continuing_family_planning_method)
            )
        )
        flowList.add(getOptionMap(getString(R.string.post_partum), getString(R.string.post_partum)))
        return flowList
    }

    private fun getPostPartumData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(
            getOptionMap(
                getString(R.string.after_delivery),
                getString(R.string.after_delivery)
            )
        )
        flowList.add(
            getOptionMap(
                getString(R.string.forty_nine_hrs_siz_weeks),
                getString(R.string.forty_nine_hrs_siz_weeks)
            )
        )
        flowList.add(
            getOptionMap(
                getString(R.string.seven_week_one_year),
                getString(R.string.seven_week_one_year)
            )
        )
        return flowList
    }

    private fun getCombinedOralContraceptiveData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.microgynon), getString(R.string.microgynon)))
        flowList.add(
            getOptionMap(
                getString(R.string.other_oral_specify),
                getString(R.string.other_oral_specify)
            )
        )
        return flowList
    }

    private fun getProgestinOralData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.microlut), getString(R.string.microlut)))
        flowList.add(
            getOptionMap(
                getString(R.string.other_specify),
                getString(R.string.other_specify)
            )
        )
        return flowList
    }

    private fun getInjectables(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(
            getOptionMap(
                getString(R.string.depo_provera),
                getString(R.string.depo_provera)
            )
        )
        flowList.add(
            getOptionMap(
                getString(R.string.sayana_press),
                getString(R.string.sayana_press)
            )
        )
        flowList.add(
            getOptionMap(
                getString(R.string.other_injectables_specify),
                getString(R.string.other_injectables_specify)
            )
        )
        return flowList
    }

    private fun getImplant(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(
            getOptionMap(
                getString(R.string.five_year_implant),
                getString(R.string.five_year_implant)
            )
        )
        flowList.add(
            getOptionMap(
                getString(R.string.three_year_implant),
                getString(R.string.three_year_implant)
            )
        )
        flowList.add(
            getOptionMap(
                getString(R.string.other_implant),
                getString(R.string.other_implant)
            )
        )
        return flowList
    }

    private fun getCondoms(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.male), getString(R.string.male)))
        flowList.add(getOptionMap(getString(R.string.female), getString(R.string.female)))
        return flowList
    }

    private fun getEmergencyContraceptiveData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private fun getPermanentMethods(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(
            getOptionMap(
                getString(R.string.tubal_ligation),
                getString(R.string.tubal_ligation)
            )
        )
        flowList.add(getOptionMap(getString(R.string.vasectomy), getString(R.string.vasectomy)))
        flowList.add(
            getOptionMap(
                getString(R.string.other_fp_method),
                getString(R.string.other_fp_method)
            )
        )
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
            if(selectedID == getString(R.string.post_partum)){
                binding.tvPostPartum.visible()
                addCustomView(
                    getPostPartumData(),
                    PostPartum,
                    viewModel.resultHashMap,
                    postPartumSelectionCallBack,
                    binding.PostPartumRoot
                )
                binding.PostPartumRoot.visible()
            }else{
                binding.tvPostPartum.gone()
                binding.PostPartumRoot.removeAllViews()
                binding.PostPartumRoot.gone()
            }
        }

    private var postPartumSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[PostPartum] = selectedID as String
            resultMapChanged()
        }

    private var combinedOralSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[CombineOralContraceptive] = selectedID as String
            if(selectedID == getString(R.string.other_oral_specify)){
                binding.etCombinedOralContraceptiveComments.visible()
            }else{
                binding.etCombinedOralContraceptiveComments.gone()
            }
            resultMapChanged()
        }

    private var progestinSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[ProgestinOnlyOrals] = selectedID as String
            when(selectedID){
                getString(R.string.microlut) -> {
                    binding.tvQuantityLabel.visible()
                    binding.etQuantity.visible()
                    binding.etProgestinOnlyOralsComments.gone()
                }
                getString(R.string.other_specify) -> {
                    binding.etProgestinOnlyOralsComments.visible()
                    binding.tvQuantityLabel.gone()
                    binding.etQuantity.gone()
                }
            }
            resultMapChanged()
        }

    private var injectableSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[Injectables] = selectedID as String
            if(selectedID == getString(R.string.other_injectables_specify)){
                binding.etOtherInjectableComments.visible()
            }else{
                binding.etOtherInjectableComments.gone()
            }
            resultMapChanged()
        }

    private var implantsSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultHashMap[Implants] = selectedID as String
            if(selectedID == getString(R.string.other_implant)){
                binding.etOtherImplantsComments.visible()
            }else{
                binding.etOtherImplantsComments.gone()
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
            if (selectedID == getString(R.string.other_fp_method)) {
                binding.etOtherPermanentMethodComments.visible()
            } else {
                binding.etOtherPermanentMethodComments.gone()
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
        if (viewModel.resultHashMap[ClientType] == getString(R.string.post_partum)) {
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
       if (viewModel.resultHashMap[ProgestinOnlyOrals] == Microlut) {
            if (viewModel.quantity.isEmpty()) {
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