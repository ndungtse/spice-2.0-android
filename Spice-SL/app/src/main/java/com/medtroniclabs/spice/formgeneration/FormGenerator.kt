package com.medtroniclabs.spice.formgeneration

import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils.displayAge
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.AgeDobLayoutBinding
import com.medtroniclabs.spice.databinding.CardLayoutBinding
import com.medtroniclabs.spice.databinding.CheckboxDialogSpinnerLayoutBinding
import com.medtroniclabs.spice.databinding.CustomSpinnerBinding
import com.medtroniclabs.spice.databinding.DatepickerLayoutBinding
import com.medtroniclabs.spice.databinding.EdittextLayoutBinding
import com.medtroniclabs.spice.databinding.InstructionLayoutBinding
import com.medtroniclabs.spice.databinding.LayoutInformationLabelBinding
import com.medtroniclabs.spice.databinding.LayoutSingleSelectionBinding
import com.medtroniclabs.spice.databinding.MentalHealthLayoutBinding
import com.medtroniclabs.spice.databinding.NoOfDaysLayoutBinding
import com.medtroniclabs.spice.databinding.RadioGroupLayoutBinding
import com.medtroniclabs.spice.databinding.TextLabelLayoutBinding
import com.medtroniclabs.spice.formgeneration.FormSupport.getSpannableString
import com.medtroniclabs.spice.formgeneration.FormSupport.isTranslatedOrNot
import com.medtroniclabs.spice.formgeneration.FormSupport.translateTitle
import com.medtroniclabs.spice.formgeneration.FormSupport.updateTitle
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.DefaultIDLabel
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.GONE
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.INVISIBLE
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Month
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.SSP16
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.VISIBLE
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Week
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Year
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.value
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_INFORMATION_LABEL
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_DIALOG_CHECKBOX
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_AGE
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_CARD_FAMILY
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_DATEPICKER
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_EDITTEXT
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_RADIOGROUP
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_SPINNER
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_TEXTLABEL
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_INSTRUCTION
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_METAL_HEALTH
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_NO_OF_DAYS
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_SINGLE_SELECTION
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_TIME
import com.medtroniclabs.spice.formgeneration.extension.dp
import com.medtroniclabs.spice.formgeneration.extension.hideKeyboard
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.extension.textSizeSsp
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.ConditionModelConfig
import com.medtroniclabs.spice.formgeneration.model.ConditionalModel
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.DigitsInputFilter
import com.medtroniclabs.spice.formgeneration.utility.FormFieldValidator
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.headPhoneNumber
import com.medtroniclabs.spice.mappingkey.MemberRegistration.phoneNumber
import java.util.Calendar


class FormGenerator(
    var context: Context,
    private val parentLayout: LinearLayout,
    private val resultLauncher: ActivityResultLauncher<Intent>? = null,
    private val listener: FormEventListener,
    var scrollView: NestedScrollView? = null,
    val translate: Boolean = false
) : ContextWrapper(context) {

    private var serverData: List<FormLayout>? = null
    private val rootSuffix = "rootView"
    private val titleSuffix = "titleTextView"
    private val errorSuffix = "errorMessageView"
    private val resultHashMap = HashMap<String, Any>()
    private val tvKey = "summaryKey"
    private val tvValue = "summaryValue"
    private val rootSummary = "summaryRoot"
    private var editScreen: Boolean? = null
    private var focusNeeded: View? = null


    fun populateViews(
        serverData: List<FormLayout>,
    ) {
        this.serverData = serverData
        parentLayout.removeAllViews()
        serverData.forEach { serverViewModel ->
            when (serverViewModel.viewType) {
                VIEW_TYPE_FORM_CARD_FAMILY -> createCardViewFamily(serverViewModel)
                VIEW_TYPE_FORM_EDITTEXT -> createEditText(serverViewModel)
                VIEW_TYPE_FORM_RADIOGROUP -> createRadioGroup(serverViewModel)
                VIEW_TYPE_SINGLE_SELECTION -> createSingleSelectionView(serverViewModel)
                VIEW_TYPE_FORM_SPINNER -> createCustomSpinner(serverViewModel)
                VIEW_TYPE_DIALOG_CHECKBOX -> createCheckboxDialogView(serverViewModel)
                VIEW_INFORMATION_LABEL -> createInformationLabel(serverViewModel)
                VIEW_TYPE_INSTRUCTION -> createInstructionView(serverViewModel)
                VIEW_TYPE_FORM_TEXTLABEL -> createTextLabel(serverViewModel)
                VIEW_TYPE_METAL_HEALTH -> createMentalHealthView(serverViewModel)
                VIEW_TYPE_FORM_AGE -> createAgeView(serverViewModel)
                VIEW_TYPE_NO_OF_DAYS -> createNoOfDaysView(serverViewModel)
                VIEW_TYPE_FORM_DATEPICKER -> createDatePicker(serverViewModel)
            }
        }
        listener.onRenderingComplete()
    }

    private fun createCardViewFamily(serverViewModel: FormLayout) {
        val binding = CardLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            setViewVisibility(visibility, binding.root)
            binding.root.tag = id + rootSuffix
            binding.llFamilyRoot.tag = id
            if (translate) {
                binding.cardTitle.text = titleCulture ?: title
            } else {
                binding.cardTitle.text = title
            }
            if (parentLayout.findViewWithTag<LinearLayout>(id) == null) {
                parentLayout.addView(binding.root)
            }
        }
    }

    private fun createNoOfDaysView(serverViewModel: FormLayout) {
        val binding = NoOfDaysLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvTitle.tag = id + titleSuffix
            binding.etUserInput.tag = id
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvTitle.text = title
            binding.tvInfo.text = information
            if (isMandatory) {
                binding.tvTitle.markMandatory()
            }
            isEnabled?.let {
                binding.etUserInput.isEnabled = it
            }
            inputType?.let {
                binding.etUserInput.inputType = it
            }
            hint?.let {
                if (translate) {
                    binding.etUserInput.hint = hintCulture ?: it
                } else {
                    binding.etUserInput.hint = it
                }
            }
            binding.etUserInput.addTextChangedListener { input ->
                input?.let {
                    val resultValue = input.trim().toString()
                    if (resultValue.isNotBlank()) {
                        resultHashMap[id] = resultValue
                    } else {
                        if (resultHashMap.containsKey(id))
                            resultHashMap.remove(id)
                    }
                }
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
        }
    }


    private fun createEditText(serverViewModel: FormLayout) {
        val binding = EdittextLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvTitle.tag = id + titleSuffix
            binding.etUserInput.tag = id
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvNationalIdAction.visibility = View.GONE
            binding.tvKey.tag = id + tvKey
            binding.tvValue.tag = id + tvValue
            binding.bgLastMeal.tag = id + rootSummary
            checkGenerateAction(this, binding)
            binding.tvTitle.text = updateTitle(title, translate, titleCulture, unitMeasurement)

            if (serverViewModel.id.contains(phoneNumber) || serverViewModel.id.contains(headPhoneNumber)) {
                SecuredPreference.getPhoneNumberCode()?.let { phoneNumberCode ->
                    binding.llCountryCode.visibility = View.VISIBLE
                    binding.tvCountryCode.text = phoneNumberCode
                }
            }

            maxLines?.let { binding.etUserInput.setLines(it) }
                ?: binding.etUserInput.setSingleLine()

            if (isMandatory) {
                binding.tvTitle.markMandatory()
            }

            defaultValue?.let {
                binding.etUserInput.setText(it)
                resultHashMap[id] = it
            }

            hint?.let {
                if (translate) {
                    binding.etUserInput.hint = hintCulture ?: it
                } else {
                    binding.etUserInput.hint = it
                }
            }


            isEnabled?.let {
                binding.etUserInput.isEnabled = it
            }

            val inputFilter = arrayListOf<InputFilter>()
            maxLength?.let {
                inputFilter.add(InputFilter.LengthFilter(it))
            }

            contentLength?.let {
                inputFilter.add(InputFilter.LengthFilter(it))
            }

            if (applyDecimalFilter == true)
                inputFilter.add(DigitsInputFilter())

            if (id == DefinedParams.NationalId) {
                inputFilter.add(InputFilter.AllCaps())
            }

            if (inputFilter.isNotEmpty()) {
                try {
                    binding.etUserInput.filters = inputFilter.toTypedArray()
                } catch (_: Exception) {
                    //Exception - Catch block
                }
            }

            inputType?.let {
                when (it) {
                    InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_NUMBER -> binding.etUserInput.inputType =
                        InputType.TYPE_CLASS_NUMBER

                    InputType.TYPE_NUMBER_FLAG_DECIMAL -> binding.etUserInput.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

                    else -> {
                        binding.etUserInput.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    }
                }
            }

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }

            binding.etUserInput.addTextChangedListener { editable: Editable? ->
                when {
                    editable.isNullOrBlank() -> {
                        if (editScreen == true) {
                            if ((inputType != null && (inputType == InputType.TYPE_CLASS_NUMBER ||
                                        inputType == InputType.TYPE_NUMBER_FLAG_DECIMAL))
                            ) {
                                resultHashMap.remove(id)
                            } else {
                                resultHashMap[id] = ""
                            }
                        } else {
                            resultHashMap.remove(id)
                        }
                        setConditionalVisibility(serverViewModel, null)
                    }

                    else -> {
                        if ((inputType != null && (inputType == InputType.TYPE_CLASS_NUMBER ||
                                    inputType == InputType.TYPE_NUMBER_FLAG_DECIMAL))
                        ) {
                            val resultValue = editable.trim().toString().toDoubleOrNull()
                            resultValue?.let {
                                resultHashMap[id] = resultValue
                            }
                        } else
                            resultHashMap[id] = editable.trim().toString()
                        setConditionalVisibility(serverViewModel, editable.trim().toString())
                    }
                }
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun checkGenerateAction(serverViewModel: FormLayout, binding: EdittextLayoutBinding) {
        serverViewModel.apply {
            if (isNeedAction) {
                when (id) {
                    DefinedParams.NationalId -> {
                        binding.tvNationalIdAction.visibility = View.VISIBLE
                        val clickableSpan = object : ClickableSpan() {
                            override fun onClick(mView: View) {
                                // action click
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                super.updateDrawState(ds)
                                ds.isUnderlineText = false
                            }
                        }
                        val text = context.getString(R.string.don_t_have_id_generate_id)
                        var index = text.indexOf("?")
                        index = if (index >= 0) index + 1 else 0
                        binding.tvNationalIdAction.text = getSpannableString(
                            clickableSpan,
                            text, index
                        )
                        binding.tvNationalIdAction.movementMethod = LinkMovementMethod.getInstance()
                    }
                }
            }
        }
    }


    private fun createRadioGroup(serverViewModel: FormLayout) {
        val binding = RadioGroupLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.rgGroup.tag = id
            binding.tvTitle.tag = id + titleSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvTitle.text = translateTitle(titleCulture, title, translate)
            optionsList?.forEachIndexed { index, map ->
                val name = map[DefinedParams.NAME]
                if (name != null && name is String) {
                    val radioButton = RadioButton(binding.rgGroup.context)
                    radioButton.id = index
                    radioButton.tag = map[DefinedParams.ID]
                    radioButton.setPadding(20.dp, 0, 20.dp, 0)
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-android.R.attr.state_enabled),
                            intArrayOf(-android.R.attr.state_checked),
                            intArrayOf(android.R.attr.state_checked)
                        ), intArrayOf(
                            ContextCompat.getColor(
                                context,
                                R.color.navy_blue_20_alpha
                            ),  // disabled
                            ContextCompat.getColor(context, R.color.purple),  // disabled
                            ContextCompat.getColor(context, R.color.purple) // enabled
                        )
                    )
                    val textColorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-android.R.attr.state_enabled),
                            intArrayOf(-android.R.attr.state_checked),
                            intArrayOf(android.R.attr.state_checked)
                        ), intArrayOf(
                            ContextCompat.getColor(
                                context,
                                R.color.navy_blue_20_alpha
                            ),  // disabled
                            ContextCompat.getColor(context, R.color.navy_blue), // enabled
                            ContextCompat.getColor(context, R.color.purple)
                        )
                    )
                    radioButton.setTextColor(textColorStateList)
                    radioButton.buttonTintList = colorStateList
                    radioButton.invalidate()
                    radioButton.textSizeSsp = SSP16
                    radioButton.text = isTranslatedOrNot(map, name, translate)
                    val optionVisibility = map[DefinedParams.VISIBILITY]
                    setOptionVisibility(optionVisibility, radioButton)
                    radioButton.layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.WRAP_CONTENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    radioButton.typeface = ResourcesCompat.getFont(context, R.font.inter_regular)
                    binding.rgGroup.addView(radioButton)
                }
            }
            binding.rgGroup.orientation = orientation ?: LinearLayout.HORIZONTAL

            if (isMandatory)
                binding.tvTitle.markMandatory()

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }

            binding.rgGroup.setOnCheckedChangeListener { _, checkedId ->
                checkRadioGroupId(checkedId, optionsList, id, serverViewModel)
                changeRadioGroupTypeFace(checkedId, binding.rgGroup)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun checkRadioGroupId(
        checkedId: Int,
        optionsList: java.util.ArrayList<Map<String, Any>>?,
        id: String,
        serverViewModel: FormLayout
    ) {
        if (checkedId >= 0) {
            optionsList?.let {
                val map = it[checkedId]
                resultHashMap[id] = map[DefinedParams.ID] as Any
                setConditionalVisibility(
                    serverViewModel,
                    it[checkedId][DefinedParams.NAME] as String? ?: ""
                )
            }
        }
    }

    private fun changeRadioGroupTypeFace(radioButtonId: Int, rootView: RadioGroup) {

        rootView.let {
            rootView.children.iterator().forEach {
                val child = it as RadioButton
                if (child.id == radioButtonId) {
                    child.typeface = ResourcesCompat.getFont(this, R.font.inter_bold)
                } else {
                    child.typeface = ResourcesCompat.getFont(this, R.font.inter_regular)
                }
            }
        }
    }

    private fun setOptionVisibility(optionVisibility: Any?, radioButton: View) {
        if (optionVisibility is String) {
            setViewVisibility(optionVisibility, radioButton)
        }
    }


    private fun createSingleSelectionView(serverViewModel: FormLayout) {
        serverViewModel.apply {
            val binding = LayoutSingleSelectionBinding.inflate(LayoutInflater.from(context))
            binding.root.tag = id + rootSuffix
            binding.tvTitle.tag = id + titleSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvTitle.text = translateTitle(titleCulture, title, translate)
            optionsList?.let {
                val view = SingleSelectionCustomView(context)
                view.tag = id
                view.addViewElements(
                    it,
                    translate,
                    resultHashMap,
                    id,
                    serverViewModel,
                    singleSelectionCallback
                )
                binding.selectionGroup.addView(view)
            }
            if (isMandatory)
                binding.tvTitle.markMandatory()

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }

            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: String, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedId, elementID, serverViewModel, name ->
            saveSelectedOptionValue(elementID, selectedId, serverViewModel, name)
        }

    private fun saveSelectedOptionValue(
        id: String,
        idValue: Any?,
        serverViewModel: FormLayout,
        name: String?
    ) {
        idValue?.let {
            resultHashMap[id] = it
            setConditionalVisibility(
                serverViewModel,
                name
            )
        } ?: kotlin.run {
            setConditionalVisibility(
                serverViewModel,
                null
            )
        }
    }

    private fun createCustomSpinner(serverViewModel: FormLayout) {
        val binding = CustomSpinnerBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.etUserInput.tag = id
            binding.tvTitle.tag = id + titleSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvTitle.text = translateTitle(titleCulture, title, translate)
            val dropDownList = java.util.ArrayList<Map<String, Any>>()
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to DefaultIDLabel,
                    DefinedParams.ID to "-1"
                )
            )
            if (isMandatory) {
                binding.tvTitle.markMandatory()
            }
            val adapter = CustomSpinnerAdapter(context, translate)
            optionsList?.let { list ->
                addDropDownList(list, dropDownList)
            }
            adapter.setData(dropDownList)
            binding.etUserInput.adapter = adapter
            binding.etUserInput.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>?,
                        view: View?,
                        pos: Int,
                        itemId: Long
                    ) {
                        handleSelectedItem(
                            adapter.getData(position = pos),
                            id,
                            dependentID,
                            serverViewModel
                        )
                        onPopulateCondition(condition)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        /**
                         * usage of this method is not required
                         */
                    }
                }

            defaultValue?.let { value ->
                val selectedMapIndex =
                    dropDownList.indexOfFirst { it.containsKey(DefinedParams.ID) && it[DefinedParams.ID] != null && it[DefinedParams.ID] == value }

                if (selectedMapIndex > 0) {
                    val selectedMap = dropDownList[selectedMapIndex]
                    selectedMap.let { map ->
                        if (map.isNotEmpty()) {
                            binding.etUserInput.setSelection(selectedMapIndex, true)
                            resultHashMap[id] = value
                        }
                    }
                }
            }

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
            disableSpinner?.let {
                if (it)
                    binding.etUserInput.isEnabled = false
            }
            localDataCache?.let { localCache ->
                listener.loadLocalCache(id, localCache)
            }
        }
    }

    private fun handleSelectedItem(
        selectedItem: Map<String, Any>?,
        id: String,
        dependentID: String?,
        serverViewModel: FormLayout
    ) {
        selectedItem?.let {
            val selectedId = it[DefinedParams.ID]
            val selectedName = it[DefinedParams.NAME]
            if ((selectedId is String && selectedId == "-1")) {
                if (resultHashMap.containsKey(id)) {
                    handleId(id)
                    dependentID?.let { deptId ->
                        resetDependantSpinnerView(deptId)
                    }
                } else {
                    if (editScreen == true)
                        resultHashMap[id] = ""
                }
            } else {
                resultHashMap[id] =
                    it[DefinedParams.ID] as Any
                dependentID?.let { deptId ->
                    resetDependantSpinnerView(deptId)
                    listener.loadLocalCache(
                        deptId,
                        deptId,
                        it[DefinedParams.ID] as Long
                    )
                }
            }
            selectedIdVisibility(selectedId, serverViewModel, selectedName)
        }
    }

    private fun handleId(id: String) {
        if (editScreen == true) {
            resultHashMap[id] = ""
        } else {
            resultHashMap.remove(id)
        }
    }

    private fun selectedIdVisibility(
        selectedId: Any?,
        serverViewModel: FormLayout,
        selectedName: Any?
    ) {
        if (selectedId is String)
            setConditionalVisibility(serverViewModel, selectedId)
        else if (selectedId is Long && selectedName is String)
            setConditionalVisibility(serverViewModel, selectedName)
    }

    private fun onPopulateCondition(condition: java.util.ArrayList<ConditionalModel>?) {
        if (!condition.isNullOrEmpty()) {
            val id = condition[0].targetId
            if (!id.isNullOrBlank())
                listener.onPopulate(id)
        }
    }

    private fun addDropDownList(
        list: ArrayList<Map<String, Any>>,
        dropDownList: ArrayList<Map<String, Any>>
    ) {
        if (list.isNotEmpty()) {
            dropDownList.addAll(list)
        }
    }

    private fun resetDependantSpinnerView(dependentID: String) {
        val view = getViewByTag(dependentID)
        if (view is AppCompatSpinner && view.adapter is CustomSpinnerAdapter) {
            val dropDownList = java.util.ArrayList<Map<String, Any>>()
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to DefaultIDLabel,
                    DefinedParams.ID to "-1"
                )
            )
            (view.adapter as CustomSpinnerAdapter).setData(dropDownList)
            view.setSelection(0, true)
        }
    }


    private fun createCheckboxDialogView(serverViewModel: FormLayout) {
        val binding = CheckboxDialogSpinnerLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.etUserInput.tag = id
            binding.tvTitle.tag = id + titleSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvTitle.text = translateTitle(titleCulture, title, false)

            if (isMandatory) {
                binding.tvTitle.markMandatory()
            }

            binding.etUserInput.safeClickListener {
                listener.onCheckBoxDialogueClicked(id, serverViewModel, resultHashMap[id])
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun createInformationLabel(serverViewModel: FormLayout) {
        val binding = LayoutInformationLabelBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvValue.tag = id
            binding.tvKey.tag = id + tvKey
            binding.tvValue.text = getString(R.string.hyphen_symbol)
            binding.tvKey.text = updateTitle(title, translate, titleCulture, unitMeasurement)
            backgroundColor?.let { color ->
                if (color.startsWith(getString(R.string.hash_symbol))) {
                    binding.llBase.setBackgroundColor(
                        Color.parseColor(color)
                    )
                }
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }


    private fun createInstructionView(serverViewModel: FormLayout) {
        serverViewModel.apply {
            var instructionsList = instructions
            if (translate && !instructionsCulture.isNullOrEmpty())
                instructionsList = instructionsCulture

            val binding = InstructionLayoutBinding.inflate(LayoutInflater.from(context))
            binding.root.tag = id + rootSuffix
            binding.tvTitle.tag = id
            binding.tvTitle.text = translateTitle(titleCulture, title, false)
            binding.clInstructionRoot.safeClickListener {
                instructionsList?.let {
                    listener.onInstructionClicked(id, title, it)
                } ?: kotlin.run {
                    listener.onInstructionClicked(id, title)
                }
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)

        }
    }


    private fun createTextLabel(serverViewModel: FormLayout) {
        val binding = TextLabelLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvTitle.tag = id
            if (translate) {
                binding.tvTitle.text = titleCulture ?: title
            } else {
                binding.tvTitle.text = title
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }


    private fun createMentalHealthView(serverViewModel: FormLayout) {
        val binding = MentalHealthLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.rvMentalHealth.tag = id
            binding.rvMentalHealth.layoutManager = LinearLayoutManager(context)

            localDataCache?.let {
                listener.loadLocalCache(id, it)
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun createDatePicker(serverViewModel: FormLayout) {
        val binding = DatepickerLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.etUserInput.tag = id
            binding.tvErrorMessage.tag = id + errorSuffix
            if (translate) {
                binding.tvTitle.text = titleCulture ?: title
            } else {
                binding.tvTitle.text = title
            }
            binding.tvTitle.tag = id + titleSuffix

            binding.etUserInput.safeClickListener {
                val dateInput = if (binding.etUserInput.text.toString().isNotEmpty())
                    DateUtils.getYearMonthAndDate(binding.etUserInput.text.toString()) else null
                showDatePicker(
                    context = context,
                    disableFutureDate = disableFutureDate ?: false,
                    minDate = minDate,
                    maxDate = maxDate,
                    date = dateInput
                ) { _, year, month, dayOfMonth ->
                    val stringDate = "$dayOfMonth-$month-$year"
                    val parsedDate = DateUtils.getDatePatternDDMMYYYY().parse(stringDate)
                    parsedDate?.let {
                        binding.etUserInput.text = DateUtils.getDateDDMMYYYY().format(it)
                        resultHashMap[id] = DateUtils.getDateString(
                            parsedDate.time,
                            inputFormat = DateUtils.DATE_FORMAT_yyyyMMdd,
                            outputFormat = DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                        )
                    }
                }
            }

            if (isMandatory) {
                binding.tvTitle.markMandatory()
            }

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }

        }
    }

    private lateinit var textWatcher: TextWatcher
    private var isDOBUpdated = false
    private fun createAgeView(serverViewModel: FormLayout) {
        val binding = AgeDobLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag =id+rootSuffix
            binding.etDateOfBirth.tag = id
            binding.etYears.inputType = InputType.TYPE_CLASS_NUMBER
            binding.etMonths.inputType = InputType.TYPE_CLASS_NUMBER
            binding.etWeeks.inputType = InputType.TYPE_CLASS_NUMBER
            binding.etYears.tag = id + Year
            binding.etMonths.tag = id + Month
            binding.etWeeks.tag = id + Week
            binding.ageValue.tag = id + value
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvDateOfBirth.tag = id + titleSuffix
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    val year =  binding.etYears.text.toString().toIntOrNull() ?: 0
                    val month =  binding.etMonths.text.toString().toIntOrNull() ?: 0
                    val weeks =  binding.etWeeks.text.toString().toIntOrNull() ?: 0
                    if (!isDOBUpdated){
                        if (!(year == 0 && month == 0 && weeks == 0)) {
                            updateDateOfBirthFromFields(
                                binding.etYears,
                                binding.etMonths,
                                binding.etWeeks,
                                id,
                                binding.etDateOfBirth
                            )
                        } else {
                            resultHashMap[Year] = year
                            resultHashMap[Month] = month
                            resultHashMap[Week] = weeks
                            updateAgeView(id)
                            removeIfContains(id)
                            removeWatcher(binding.etYears,binding.etMonths,binding.etWeeks)
                            removeDOB(binding.etDateOfBirth,binding.etYears,binding.etMonths,binding.etWeeks)
                            addWatcher(binding.etYears,binding.etMonths,binding.etWeeks)
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            }

            addWatcher(binding.etYears,binding.etMonths,binding.etWeeks)
            binding.etDateOfBirth.safeClickListener {
                val yearMonthWeek = if (binding.etDateOfBirth.text.isNotEmpty()){
                    DateUtils.getYearMonthAndDate(binding.etDateOfBirth.text.toString())
                } else null
                removeWatcher(binding.etYears,binding.etMonths,binding.etWeeks)
                serverViewModel.run {
                    showDatePicker(
                        context = context,
                        disableFutureDate = disableFutureDate ?: false,
                        minDate = minDate,
                        maxDate = maxDate,
                        date = yearMonthWeek,
                        cancelCallBack = {
                            isDOBUpdated = false
                            addWatcher(binding.etYears,binding.etMonths,binding.etWeeks)
                        }
                    ) { _, year, month, dayOfMonth ->
                        val stringDate = "$dayOfMonth-$month-$year"
                        val parsedDate = DateUtils.getDatePatternDDMMYYYY().parse(stringDate)
                        parsedDate?.let {
                            binding.etDateOfBirth.text = DateUtils.getDateDDMMYYYY().format(it)
                            isDOBUpdated = true
                            val yearMonthWeeks = DateUtils.getYearMonthAndWeek(stringDate)
                            addOrUpdateDOB(
                                DateUtils.getDateString(
                                    parsedDate.time,
                                    inputFormat = DateUtils.DATE_FORMAT_yyyyMMdd,
                                    outputFormat = DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                ),
                                id
                            )
                            yearMonthWeeks.first?.let { year ->
                                binding.etYears.setText(year.toString())
                                resultHashMap[Year] = year
                            }
                            yearMonthWeeks.second?.let { month ->
                                binding.etMonths.setText(month.toString())
                                resultHashMap[Month] = month
                            }
                            yearMonthWeeks.third?.let { week ->
                                binding.etWeeks.setText(week.toString())
                                resultHashMap[Week] = week
                            }
                        }
                        updateAgeView(id)
                        addWatcher(binding.etYears,binding.etMonths,binding.etWeeks)
                    }
                }
            }

            if (isMandatory) {
                binding.tvYear.markMandatory()
                binding.tvWeeks.markMandatory()
                binding.tvMonths.markMandatory()
                binding.tvDateOfBirth.markMandatory()
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun removeWatcher(
        etYears: AppCompatEditText,
        etMonths: AppCompatEditText,
        etWeeks: AppCompatEditText
    ) {
        etYears.removeTextChangedListener(textWatcher)
        etMonths.removeTextChangedListener(textWatcher)
        etWeeks.removeTextChangedListener(textWatcher)
    }
    private fun addWatcher(
        etYears: AppCompatEditText,
        etMonths: AppCompatEditText,
        etWeeks: AppCompatEditText
    )
    {
        etYears.addTextChangedListener(textWatcher)
        etMonths.addTextChangedListener(textWatcher)
        etWeeks.addTextChangedListener(textWatcher)
        isDOBUpdated = false
    }
    private fun updateDateOfBirthFromFields(
        etYears: AppCompatEditText,
        etMonths: AppCompatEditText,
        etWeeks: AppCompatEditText,
        id: String,
        etDateOfBirth: AppCompatTextView
    ) {
        val year = etYears.text.toString().toIntOrNull() ?: 0
        val month = etMonths.text.toString().toIntOrNull() ?: 0
        val weeks = etWeeks.text.toString().toIntOrNull() ?: 0
        resultHashMap[Year] = year
        resultHashMap[Month] = month
        resultHashMap[Week] = weeks
        val calculatedBirthDate = DateUtils.calculateBirthDate(year, month, weeks)
        removeWatcher(etYears,etMonths,etWeeks)
        etDateOfBirth.text = DateUtils.convertDateFormat(
            calculatedBirthDate,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        addWatcher(etYears,etMonths,etWeeks)
        addOrUpdateDOB(calculatedBirthDate, id)
        updateAgeView(id)
    }

    private fun updateAgeView(id: String) {
        val ageView = getViewByTag(id+ value)
        val age = displayAge(resultHashMap, context)
        ageView?.let {view ->
            (view as? AppCompatTextView)?.text = age
        }
    }

    private fun removeIfContains(key: String) {
        if (resultHashMap.containsKey(key)) {
            resultHashMap.remove(key)
        }
    }

    private fun addOrUpdateAgeValue(value: String, key:String) {
        value.toInt().let {
            resultHashMap[key] = it
        }
    }

    private fun removeDOB(
        editText: AppCompatTextView,
        etYears: AppCompatEditText,
        etMonths: AppCompatEditText,
        etWeeks: AppCompatEditText
    ) {
        val year = resultHashMap[Year] as? Int ?: 0
        val month = resultHashMap[Month] as? Int ?: 0
        val weeks = resultHashMap[Week] as? Int ?: 0
        if (year == 0 && month == 0 && weeks == 0) {
            etYears.removeTextChangedListener(textWatcher)
            etMonths.removeTextChangedListener(textWatcher)
            etWeeks.removeTextChangedListener(textWatcher)
            editText.text = ""
            etYears.addTextChangedListener(textWatcher)
            etMonths.addTextChangedListener(textWatcher)
            etWeeks.addTextChangedListener(textWatcher)
        }
    }

    private fun addOrUpdateDOB(dateOfBirth: String, id: String) {
        resultHashMap[id] = dateOfBirth
    }

    private fun showDatePicker(
        context: Context,
        disableFutureDate: Boolean = false,
        minDate: Long? = null,
        maxDate: Long? = null,
        date : Triple<Int?,Int?,Int?>?=null,
        cancelCallBack: (() -> Unit)? = null,
        callBack: (dialog: DatePicker, year: Int, month: Int, dayOfMonth: Int) -> Unit,
    ): DatePickerDialog {

        val calendar = Calendar.getInstance()
        var thisYear = calendar.get(Calendar.YEAR)
        var thisMonth = calendar.get(Calendar.MONTH)
        var thisDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dialog: DatePickerDialog?

        if (date?.first != null && date.second != null && date.third != null) {
            thisYear = date.first!!
            thisMonth = date.second!!
            thisDay = date.third!!
        }

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
                callBack.invoke(datePicker, year, month + 1, dayOfMonth)
            }

        dialog = DatePickerDialog(
            context,
            dateSetListener,
            thisYear,
            thisMonth,
            thisDay
        )

        if (cancelCallBack != null) {
            dialog.setOnCancelListener {
                cancelCallBack.invoke()
            }
        }

        minDate?.let {
            dialog.datePicker.minDate = it
        }
        maxDate?.let {
            dialog.datePicker.maxDate = it
        }

        if (disableFutureDate) dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.setCancelable(false)

        dialog.show()

        return dialog

    }

    private fun getFamilyView(family: String?): LinearLayout? {
        family ?: return null
        return parentLayout.findViewWithTag(family)
    }


    private fun setConditionalVisibility(
        model: FormLayout,
        actualValue: String?,
        isCheckBox: Boolean = false
    ) {
        val conditionList = model.condition
        if (conditionList.isNullOrEmpty())
            return

        conditionList.forEach { conditionalModel ->
            validateConditionModel(conditionalModel, actualValue, isCheckBox)
        }
    }


    private fun validateConditionModel(
        conditionalModel: ConditionalModel,
        actualValue: String?,
        isCheckBox: Boolean
    ) {
        conditionalModel.apply {
            if (targetId != null && targetOption != null) {
                val targetedView = parentLayout.findViewWithTag<View>(targetOption)
                if (visibility != null) {
                    checkConditionBasedRendering(
                        conditionalModel,
                        ConditionModelConfig.VISIBILITY,
                        actualValue,
                        targetedView,
                        targetOption,
                        isCheckBox
                    )
                } else if (enabled != null) {
                    checkConditionBasedRendering(
                        conditionalModel,
                        ConditionModelConfig.ENABLED,
                        actualValue,
                        targetedView,
                        targetOption,
                        isCheckBox
                    )
                }
            } else if (targetId != null) {
                val targetedView = parentLayout.findViewWithTag<View>(targetId + rootSuffix)
                visibleEnableConditionRendering(
                    visibility,
                    enabled,
                    conditionalModel,
                    actualValue,
                    targetedView,
                    isCheckBox
                )
            } else {
                return@apply
            }
        }
    }


    private fun checkConditionBasedRendering(
        conditionalModel: ConditionalModel,
        config: ConditionModelConfig,
        actualValue: String?,
        targetedView: View?,
        targetOption: String? = null,
        isCheckBox: Boolean
    ) {
        conditionalModel.apply {
            targetedView ?: return@apply
            if (!eq.isNullOrBlank()) {
                if (eq == actualValue)
                    handleConfig(true, config, visibility, enabled, targetedView)
                else if (!isCheckBox)
                    handleTargetConfig(serverData, targetId, targetedView, config, targetOption)
            } else if (lengthGreaterThan != null) {
                isLengthGreater(
                    conditionalModel,
                    actualValue,
                    targetOption,
                    targetedView,
                    config,
                    isCheckBox
                )
            } else if (!eqList.isNullOrEmpty()) {
                if (eqList!!.contains(actualValue))
                    handleConfig(true, config, visibility, enabled, targetedView)
                else if (!isCheckBox)
                    handleTargetConfig(serverData, targetId, targetedView, config, targetOption)
            }
        }
    }

    private fun isLengthGreater(
        conditionalModel: ConditionalModel,
        actualValue: String?,
        targetOption: String?,
        targetedView: View,
        config: ConditionModelConfig,
        isCheckBox: Boolean
    ) {
        conditionalModel.apply {
            if (actualValue != null && actualValue.length > lengthGreaterThan!!)
                handleConfig(false, config, visibility, enabled, targetedView)
            else if (!isCheckBox)
                handleTargetConfig(serverData, targetId, targetedView, config, targetOption)
        }
    }

    private fun handleConfig(
        resetValue: Boolean,
        config: ConditionModelConfig,
        visibility: String?,
        enabled: Boolean?,
        targetedView: View
    ) {
        if (config == ConditionModelConfig.VISIBILITY)
            setViewVisibility(visibility, targetedView, resetValue)
        else
            setViewEnableDisable(enabled, targetedView, resetValue)
    }

    private fun handleTargetConfig(
        serverData: List<FormLayout>?,
        targetId: String?,
        targetedView: View,
        config: ConditionModelConfig,
        targetOption: String?
    ) {
        val targetModel = serverData?.find { it.id == targetId }
        if (config == ConditionModelConfig.VISIBILITY) {
            var visibility: String? = null
            if (targetOption != null) {
                visibility = targetModelVisibility(targetModel, targetOption, visibility)
                removeTargetId(targetId, visibility, targetedView)
                setViewVisibility(visibility, targetedView, false)
            } else {
                visibility = targetModel?.visibility
                setViewVisibility(visibility, targetedView, true)
            }
        } else {
            var enabled: Boolean? = null
            if (targetOption != null) {
                enabled = targetModelStatus(targetModel, enabled, targetOption)
                setViewEnableDisable(enabled, targetedView, false)
            } else {
                enabled = targetModel?.isEnabled
                setViewEnableDisable(enabled, targetedView, true)
            }
        }
    }

    private fun setViewEnableDisable(
        enabled: Boolean?,
        rootLyt: View,
        resetValue: Boolean = false
    ) {
        recursiveLoopChildren(rootLyt as ViewGroup, { view ->
            view?.apply {
                if (resetValue) {
                    resetChildFormViewComponents(view)
                } else {
                    resetSpecificChildViews(view)
                }
                if (enabled == null) {
                    this.isEnabled = true
                    return@apply
                }
                this.isEnabled = enabled
            }
        }) { viewGroup ->
            if (resetValue) {
                resetChildFormViewGroupComponents(viewGroup)
            }
            viewGroup?.forEach { view ->
                view.apply {
                    if (enabled == null) {
                        this.isEnabled = true
                        return@apply
                    }
                    this.isEnabled = enabled
                }
            }
        }
    }

    private fun targetModelVisibility(
        targetModel: FormLayout?,
        targetOption: String,
        visibility: String?
    ): String? {
        var visibleOrNot = visibility
        targetModel?.optionsList?.forEach { map ->
            if (map[DefinedParams.NAME] == targetOption) {
                val value = map[DefinedParams.VISIBILITY]
                if (value is String?) {
                    visibleOrNot = value
                }
            }
        }
        return visibleOrNot
    }

    private fun targetModelStatus(
        targetModel: FormLayout?,
        status: Boolean?,
        targetOption: String
    ): Boolean? {
        var enableOrNot = status
        targetModel?.optionsList?.forEach { map ->
            if (map[DefinedParams.NAME] == targetOption) {
                val value = map[DefinedParams.isEnabled]
                if (value is Boolean?) {
                    enableOrNot = value
                }
            }
        }
        return enableOrNot
    }

    private fun removeTargetId(targetId: String?, visibility: String?, targetedView: View) {
        if (visibility == GONE && targetId != null) {
            val view = getViewByTag(targetId)
            if (view is RadioGroup && targetedView.isEnabled) {
                view.clearCheck()
                resultHashMap.remove(targetId)
            }
        }
    }

    private fun visibleEnableConditionRendering(
        visibility: String?,
        enabled: Boolean?,
        conditionalModel: ConditionalModel,
        actualValue: String?,
        targetedView: View?,
        isCheckBox: Boolean
    ) {
        if (visibility != null) {
            checkConditionBasedRendering(
                conditionalModel,
                ConditionModelConfig.VISIBILITY,
                actualValue,
                targetedView,
                isCheckBox = isCheckBox
            )
        } else if (enabled != null) {
            checkConditionBasedRendering(
                conditionalModel,
                ConditionModelConfig.ENABLED,
                actualValue,
                targetedView,
                isCheckBox = isCheckBox
            )
        }
    }

    private fun resetSpecificChildViews(view: View?) {
        view?.apply {
            val model = serverData?.find { it.id == tag }
            when (model?.viewType) {
                VIEW_TYPE_TIME -> {
                    if (!view.isEnabled) {
                        getViewByTag(R.id.radioGrpDate)?.let {
                            resetRadioGroup(it, model)
                        }
                    }
                }
            }
        }
    }

    private fun setViewVisibility(visibility: String?, root: View, resetValue: Boolean = false) {

        if (resetValue && visibility != null && visibility != VISIBLE) {
            resetChildViews(root)
        }

        when (visibility) {
            VISIBLE -> {
                root.visibility = View.VISIBLE
            }

            INVISIBLE -> {
                root.visibility = View.INVISIBLE
            }

            GONE -> {
                root.visibility = View.GONE
            }

            else -> {
                root.visibility = View.VISIBLE
            }
        }
    }

    private fun resetChildViews(rootLyt: View) {
        recursiveLoopChildren(rootLyt as ViewGroup, { view ->
            resetChildFormViewComponents(view)
        }) { viewGroup ->
            resetChildFormViewGroupComponents(viewGroup)
        }
    }


    private fun recursiveLoopChildren(
        parent: ViewGroup,
        viewItem: (view: View?) -> Unit,
        viewGroupItem: (viewGroup: ViewGroup?) -> Unit
    ) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child is ViewGroup) {
                recursiveLoopChildren(child, {
                    viewItem.invoke(it)
                }) { viewGroup ->
                    viewGroupItem.invoke(viewGroup)
                }
                if (child.tag != null) {
                    viewGroupItem.invoke(child)
                }

            } else {
                if (child != null && child.tag != null) {
                    // DO SOMETHING WITH VIEW
                    viewItem.invoke(child)
                }
            }
        }
    }

    private fun resetChildFormViewComponents(view: View?) {
        view?.apply {
            val model = serverData?.find { it.id == tag }
            when (model?.viewType) {
                VIEW_TYPE_FORM_DATEPICKER,
                VIEW_TYPE_FORM_SPINNER,
                VIEW_TYPE_FORM_EDITTEXT, VIEW_TYPE_NO_OF_DAYS -> resetEditTextDatePicker(this, model)

                VIEW_TYPE_TIME -> resetTimeView(this, model)
                VIEW_TYPE_FORM_AGE -> resetAgeView(this, model)
                VIEW_TYPE_DIALOG_CHECKBOX -> resetCheckBoxDialogView(this, model)
            }
        }
    }

    private fun resetChildFormViewGroupComponents(viewGroup: ViewGroup?) {
        viewGroup?.apply {
            val model = serverData?.find { it.id == tag }
            model?.let {
                when (model.viewType) {
                    VIEW_TYPE_FORM_SPINNER -> resetSpinner(this)
                    VIEW_TYPE_FORM_RADIOGROUP -> resetRadioGroup(this, model)
                    VIEW_TYPE_SINGLE_SELECTION -> {
                        resultHashMap.remove(model.id)
                        if (this is SingleSelectionCustomView) {
                            resetSingleSelectionChildViews()
                        }
                    }
                    //  VIEW_TYPE_FORM_CHECKBOX -> resetCheckbox(this, model)
                }
            }

        }
    }

    private fun resetEditTextDatePicker(view: View, model: FormLayout) {
        if (view is EditText) {
            model.defaultValue?.let {
                view.setText(it)
            } ?: kotlin.run {
                view.text.clear()
            }
        } else if (view is TextView) {
            model.defaultValue?.let {
                view.text = it
            } ?: kotlin.run {
                view.text = ""
            }
        }
    }

    private fun resetTimeView(view: View, model: FormLayout) {
        resetEditTextDatePicker(view, model)
        getViewByTag(R.id.etMinute)?.let {
            resetEditTextDatePicker(it, model)
        }
        getViewByTag(R.id.timeRadioGroup)?.let {
            resetRadioGroup(it, model)
        }
        getViewByTag(R.id.radioGrpDate)?.let {
            resetRadioGroup(it, model)
        }
    }

   fun getViewByTag(tag: Any): View? {
        return parentLayout.findViewWithTag(tag)
    }

    private fun resetRadioGroup(rgGroup: View, model: FormLayout, button: Int? = null) {
        if (rgGroup is RadioGroup) {
            val default = model.defaultValue
            if (default != null || button != null) {
                val count = rgGroup.childCount
                for (i in 0..count) {
                    resetRg(rgGroup, default, button)
                }
            } else {
                rgGroup.clearCheck()
                resultHashMap.remove(model.id)
            }
        }
    }

    private fun resetRg(rgGroup: RadioGroup, default: String?, button: Int?) {
        rgGroup.forEach {
            if (it is RadioButton) {
                when {
                    default != null && it.tag == default -> it.isChecked = true
                    button != null && it.id == button -> it.isChecked = true
                }
            }
        }
    }

    private fun resetAgeView(view: View, model: FormLayout) {
        resetEditTextDatePicker(view, model)
        getViewByTag(R.id.etDateOfBirth)?.let {
            resetEditTextDatePicker(it, model)
        }
        getViewByTag(R.id.etYears)?.let {
            resetEditTextDatePicker(it, model)
        }
        getViewByTag(R.id.etMonths)?.let {
            resetEditTextDatePicker(it, model)
        }
        getViewByTag(R.id.etWeeks)?.let {
            resetEditTextDatePicker(it, model)
        }
    }

    private fun resetCheckBoxDialogView(view: View, model: FormLayout) {
        (view as TextView).text = model.defaultValue ?: getString(R.string.please_select)
        resultHashMap.remove(model.id)
    }

    private fun resetSpinner(spinnerView: View) {
        if (spinnerView is Spinner) {
            spinnerView.setSelection(0, true)
        }
    }

    fun formSubmitAction(view: View) {
        if (validateInputs()) {
            hideKeyboard(view)
            listener.onFormSubmit(resultMap = resultHashMap, serverData = serverData)
        } else {
            focusNeeded?.let { focusNeeded ->
                scrollView?.let { scrollView ->
                    scrollToView(scrollView, focusNeeded)
                }
            }
        }
    }


    /**
     * Used to scroll to the given view.
     *
     * @param scrollViewParent Parent ScrollView
     * @param view View to which we need to scroll.
     */
    private fun scrollToView(scrollViewParent: NestedScrollView, view: View) {
        // Get deepChild Offset
        val childOffset = Point()
        getDeepChildOffset(scrollViewParent, view.parent, view, childOffset)
        // Scroll to child.
        scrollViewParent.smoothScrollTo(0, childOffset.y)
    }

    /**
     * Used to get deep child offset.
     *
     *
     * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
     * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
     *
     * @param mainParent        Main Top parent.
     * @param parent            Parent.
     * @param child             Child.
     * @param accumulatedOffset Accumulated Offset.
     */
    private fun getDeepChildOffset(
        mainParent: ViewGroup,
        parent: ViewParent,
        child: View,
        accumulatedOffset: Point
    ) {
        val parentGroup = parent as ViewGroup
        accumulatedOffset.x += child.left
        accumulatedOffset.y += child.top
        if (parentGroup == mainParent) {
            return
        }
        getDeepChildOffset(mainParent, parentGroup.parent, parentGroup, accumulatedOffset)
    }


    private fun validateInputs(): Boolean {
        var isValid = true
        focusNeeded = null
        serverData?.forEach { data ->
            data.apply {

                if ((id == headPhoneNumber || id == phoneNumber) && isMandatory && resultHashMap.containsKey(id)) {
                    val actualValue = resultHashMap[id] as? String
                    actualValue?.let {
                        if (!startsWith.isNullOrEmpty() && !checkPhoneNumberValidOrNot(it, startsWith)) {
                            isValid = false
                            requestFocusView(
                                data, getString(
                                    R.string.start_with_validation,
                                    startsWith?.joinToString(separator = " ${getString(R.string.or)}") ?: ""
                                )
                            )
                        } else if (!phoneNumberConatinMaxLength(maxLength, it) || !FormFieldValidator.isValidMobileNumber(it)) {
                            isValid = false
                            requestFocusView(data)
                        } else {
                            hideValidationField(data)
                        }
                    } ?: run {
                        isValid = false
                        requestFocusView(data)
                    }
                } else if (isMandatory && ((!resultHashMap.containsKey(id) && isViewVisible(id) && isViewEnabled(id))
                            || (resultHashMap[id] is String && (resultHashMap[id] as String).isEmpty())))
                {
                    isValid = false
                    requestFocusView(data)
                } else {
                    hideValidationField(data)
                }
            }
        }
        return isValid
    }

    private fun isViewVisible(id: String): Boolean {
        val view = getViewByTag(id + rootSuffix)
        return view != null && view.visibility == View.VISIBLE
    }

    private fun isViewEnabled(id: String): Boolean {
        val view = getViewByTag(id)
        return view != null && view.isEnabled
    }

    private fun phoneNumberConatinMaxLength(maxLength: Int?, actualValue: String): Boolean {
        return (maxLength != null && actualValue.length == maxLength)
    }
    private fun checkPhoneNumberValidOrNot(
        actualValue: String,
        startsWithArray: ArrayList<String>?
    ): Boolean {
        var valid = false
        startsWithArray?.forEach { value ->
            if (actualValue.startsWith(value, true)) {
                valid = true
            }
        }
        return valid
    }

    private fun requestFocusView(serverViewModel: FormLayout, message: String? = null) {
        if (focusNeeded == null) {
            focusNeeded = showValidationMessage(serverViewModel, message)
        } else {
            showValidationMessage(serverViewModel, message)
        }
    }

    private fun showValidationMessage(serverViewModel: FormLayout, message: String? = null): View? {
        serverViewModel.apply {
            val view = getViewByTag(serverViewModel.id + errorSuffix)
            if (view is TextView) {
                view.visibility = View.VISIBLE
                if (message != null) {
                    view.text = message
                } else {
                    view.text = getErrorMessageFromJSON(errorMessage, cultureErrorMessage)
                }
                return getViewByTag(serverViewModel.id + titleSuffix)
            }
        }
        return null
    }

    private fun getErrorMessageFromJSON(
        errorMessage: String?,
        cultureErrorMessage: String?
    ): String {

        return if (translate && !(cultureErrorMessage.isNullOrEmpty() || cultureErrorMessage.isBlank()))
            cultureErrorMessage
        else if (!(errorMessage.isNullOrEmpty() || errorMessage.isBlank()))
            errorMessage
        else getString(R.string.default_user_input_error)
    }

    private fun hideValidationField(serverViewModel: FormLayout) {
        serverViewModel.apply {
            val view = getViewByTag(serverViewModel.id + errorSuffix)
            if (view != null && view is TextView && view.visibility == View.VISIBLE) {
                view.visibility = View.GONE
            }
        }
    }

    fun validateCheckboxDialogue(
        id: String,
        serverViewModel: FormLayout,
        resultMap: ArrayList<HashMap<String, Any>>
    ) {
        if (resultMap.isEmpty()) {
            if (resultHashMap.containsKey(id)) {
                resultHashMap.remove(id)
            }
        } else {
            resultHashMap[id] = resultMap
        }
        getViewByTag(id)?.let { view ->
            if (view is AppCompatTextView) {
                view.text = setCheckBoxDialogText(resultHashMap, id)
            }
        }
        if (isContainsOther(resultMap)) {
            setConditionalVisibility(
                serverViewModel,
                DefinedParams.Other
            )
        } else {
            setConditionalVisibility(
                serverViewModel,
                null
            )
        }
    }
    private fun setCheckBoxDialogText(
        resultHashMap: HashMap<String, Any>,
        id: String
    ): String {
        var text = getString(R.string.please_select)
        if (resultHashMap.containsKey(id)) {
            val mapList = resultHashMap[id]
            if (mapList is java.util.ArrayList<*>) {
                if (mapList.size == 1) {
                    text = setDialogText(mapList)
                } else if (mapList.size > 1) {
                    text = if (isContainsOther(mapList)) {
                        "${mapList.size - 1} and ${DefinedParams.Other} ${
                            getString(R.string.symptoms_selected)
                        }"
                    } else {
                        "${mapList.size} ${getString(R.string.symptoms_selected)}"
                    }
                } else {
                    text = getString(R.string.please_select)
                }
            }
        }
        return text
    }
    private fun setDialogText(mapList: java.util.ArrayList<*>): String {
        return if (isContainsOther(mapList)) {
            "${DefinedParams.Other} ${
                getString(R.string.symptoms_selected)
            }"
        } else if (isNoSymptomContain(mapList)) {
            getString(R.string.no_symptom_selected)
        } else {
            "${mapList.size} ${getString(R.string.symptoms_selected)}"
        }
    }
    private fun isNoSymptomContain(mapList: java.util.ArrayList<*>): Boolean {
        var status = false
        mapList.forEach { map ->
            if (map is HashMap<*, *>) {
                val name = map[DefinedParams.NAME]
                if (name is String && name.startsWith(DefinedParams.NoSymptoms, true)) {
                    status = true
                    return@forEach
                }
            }
        }
        return status
    }
    private fun isContainsOther(mapList: ArrayList<*>): Boolean {
        var status = false
        mapList.forEach { map ->
            if (map is HashMap<*, *>) {
                val name = map[DefinedParams.NAME]
                if (name is String && name.equals(DefinedParams.Other, true)) {
                    status = true
                    return@forEach
                }
            }
        }
        return status
    }

    fun setValueForView(value: Any?, view: View) {
        if (view is AppCompatEditText) {
            if (value is String) {
                view.setText(value)
            } else if (value is Int){
                view.setText(value.toString())
            }
            else {
                view.setText("")
            }
        }

        if (view is AppCompatTextView) {
            when (value) {
                is String -> {
                    view.text = value
                }

                is Int -> {
                    view.text = value.toString()
                }

                else -> {
                    view.text = ""
                }
            }
        }

        if (view is Spinner) {
            val adapter = view.adapter
            if (adapter != null && adapter is CustomSpinnerAdapter && value is Long) {
                val selectedIndex = adapter.getIndexOfItem(value)
                if (selectedIndex != -1)
                    view.setSelection(selectedIndex, true)
            }
            if (adapter != null && adapter is CustomSpinnerAdapter && value is String) {
                val selectedIndex = adapter.getIndexOfItemByName(value)
                if (selectedIndex != -1)
                    view.setSelection(selectedIndex, true)
            }
        }
    }

}