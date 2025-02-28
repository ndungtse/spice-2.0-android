package com.medtroniclabs.spice.ui.medicalreview.investigation

import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.labtest.LabTestAdapter
import com.google.android.flexbox.FlexboxLayout
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.getDecimalFormatted
import com.medtroniclabs.spice.common.CommonUtils.getMaxDateLimit
import com.medtroniclabs.spice.common.CommonUtils.isTiberbuUser
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.convertDateFormat
import com.medtroniclabs.spice.common.DefinedParams.TestedOn
import com.medtroniclabs.spice.common.DefinedParams.both
import com.medtroniclabs.spice.databinding.CustomSpinnerLayoutInvestigationBinding
import com.medtroniclabs.spice.databinding.DatepickerLayoutBinding
import com.medtroniclabs.spice.databinding.EdittextLayoutInvestigationBinding
import com.medtroniclabs.spice.databinding.LayoutInvestigationRowBinding
import com.medtroniclabs.spice.databinding.ResultSummaryInvestigationBinding
import com.medtroniclabs.spice.formgeneration.FormSupport
import com.medtroniclabs.spice.formgeneration.FormSupport.translateTitle
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.DefaultIDLabel
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_DATEPICKER
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_EDITTEXT
import com.medtroniclabs.spice.formgeneration.config.ViewType.VIEW_TYPE_FORM_SPINNER
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.DigitsInputFilter
import com.medtroniclabs.spice.model.LabTestResultModel
import com.medtroniclabs.spice.model.medicalreview.InvestigationModel
import java.util.Calendar
import kotlin.math.roundToInt

class InvestigationGenerator(
    var context: Context,
    private val parentLayout: LinearLayout,
    var scrollView: NestedScrollView? = null,
    val translate: Boolean = false,
    val listener: InvestigationListener
) : ContextWrapper(context) {

    private var serverData: List<InvestigationModel>? = null
    private val rootSuffix = "rootView"
    private val titleSuffix = "titleTextView"
    private val errorSuffix = "errorMessageView"
    private val unitSuffix = "_unit"
    private var Gender: String? = null

    fun populateViews(
        serverData: ArrayList<InvestigationModel>,
        isNCDFlow: Boolean,
    ) {
        this.serverData = serverData
        this.serverData?.forEach { investigation ->
            val investigationBinding =
                LayoutInvestigationRowBinding.inflate(LayoutInflater.from(this))
            investigationBinding.tvTestName.text = investigation.testName
            investigationBinding.tvRecommendedOn.text = DateUtils.convertDateTimeToDate(
                investigation.recommendedOn,
                DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DATE_ddMMyyyy
            )
            if (!investigation.dataError) {
                investigationBinding.resultContainerHolder.visible()
                investigationBinding.ivDropDown.setImageDrawable(getDrawable(R.drawable.ic_arrow_up))
            }

            if ((investigation.id != null && investigation.labTestResultList != null && investigation.labTestResultList!!.size > 0) || isNCDFlow) {
                investigationBinding.ivRemoveMedication.invisible()
            }  else {
                investigationBinding.ivRemoveMedication.visible()
            }
            toggleFacility(investigationBinding,investigation)
            // After backend done we need to remove it
            investigationBinding.tvRecommendedBy.text =
                investigation.recommendedByName.takeIf { !it.isNullOrBlank() }
                    ?: getString(R.string.hyphen_symbol)
            investigationBinding.root.setOnClickListener {
                investigation.dropdownState = !investigation.dropdownState
                toggleFacility(investigationBinding,investigation)
                
            }
            investigationBinding.clMarkAsReviewed.setVisible(CommonUtils.isNonCommunity() && (!investigation.labTestResultList.isNullOrEmpty()) && investigation.isReview != true)
            investigationBinding.btnMarkAsReviewed.safeClickListener {
                listener.markAsReviewed(
                    investigation.id,
                    investigationBinding.etTestComments.text.toString()
                )
            }
            if (investigation.dataError) {
                investigationBinding.tvErrorMessage.visibility = View.GONE
            } else {
                investigationBinding.tvErrorMessage.text =
                    getString(R.string.default_user_input_error)
                investigationBinding.tvErrorMessage.visibility = View.VISIBLE
            }
            investigationBinding.ivRemoveMedication.setOnClickListener {
                listener.removeInvestigation(investigation)
            }
            if(isTiberbuUser()){
                if (investigation.components!=null){
                    investigationBinding.resultTable.visible()
                    resultTableData(investigationBinding,investigation.components)
                    investigationBinding.comments.text = ":  ${if (investigation.comments.isNullOrEmpty()) getString(R.string.hyphen_symbol) else investigation.comments}"
                    investigationBinding.descriptiveResult.text = ":  ${if (investigation.descriptiveResult.isNullOrEmpty()) getString(R.string.hyphen_symbol) else investigation.descriptiveResult}"

                }else{
                    investigationBinding.ivDropDown.invisible()
                }
            }else {
                if (investigation.labTestResultList != null) {
                    investigationBinding.resultContainerHolder.setPadding(
                        resources.getDimension(R.dimen._16sdp).roundToInt()
                    )
                    investigationBinding.resultViewContainer.visible()
                    investigationBinding.resultContainer.gone()
                    renderInvestigationResultViewContainer(
                        investigation,
                        investigationBinding.resultViewContainer
                    )
                } else {
                    investigationBinding.resultContainerHolder.setPadding(
                        resources.getDimension(R.dimen._0dp).roundToInt()
                    )
                    investigationBinding.resultViewContainer.gone()
                    investigationBinding.resultContainer.visible()
                    renderInvestigationResultContainer(
                        investigation,
                        investigationBinding.resultContainer
                    )
                }
            }
            parentLayout.addView(investigationBinding.root)
        }

    }

    // Table design for Lab test result
    private fun resultTableData(
        investigationBinding: LayoutInvestigationRowBinding,
        components: ArrayList<Map<String, Any?>>
    ) {
        investigationBinding.recyclerView.layoutManager = LinearLayoutManager(this)

        val labTestList = parseComponents(components)

        if (labTestList.isNotEmpty()) {
            val adapter = LabTestAdapter(labTestList)
            investigationBinding.recyclerView.adapter = adapter
            adapter.notifyDataSetChanged() // Ensure RecyclerView updates
        }
    }

    // Updated Parsing Function
    private fun parseComponents(components: ArrayList<Map<String, Any?>>): List<LabTestResultModel> {
        val list = mutableListOf<LabTestResultModel>()

        // Check if at least one valid row contains "result"
        val hasValidEntries = components.any {
            it[com.medtroniclabs.spice.common.DefinedParams.TestName] != null && it[com.medtroniclabs.spice.common.DefinedParams.Result] != null && it[com.medtroniclabs.spice.common.DefinedParams.Uom] != null
        }

        // Add Header if valid entries exist
        if (hasValidEntries) {
            list.add(
                LabTestResultModel(
                    labTestName = getString(R.string.test_name_),
                    resultValue = getString(R.string.result),
                    labTestUom =  getString(R.string.unit),
                    normalRange = getString(R.string.description),
                    isHeader = true
                )
            )
        }

        // Process each component
        for (component in components) {
            val testName = component[com.medtroniclabs.spice.common.DefinedParams.TestName] as? String ?: ""
            val resultValue = component[com.medtroniclabs.spice.common.DefinedParams.Result] as? String ?: ""
            val unit = component[com.medtroniclabs.spice.common.DefinedParams.Uom] as? String ?: ""
            val normalRange = component[com.medtroniclabs.spice.common.DefinedParams.Description] as? String ?: "" // Assuming "description" holds the normal range

            list.add(
                LabTestResultModel(
                    labTestName = testName,
                    resultValue = resultValue,
                    labTestUom = unit,
                    normalRange = normalRange,
                    isHeader = false
                )
            )
        }

        return list
    }





    private fun toggleFacility(
        investigationBinding: LayoutInvestigationRowBinding,
        investigation: InvestigationModel
    ) {
        if (!investigation.dropdownState) {
            investigationBinding.resultContainerHolder.gone()
            investigationBinding.ivDropDown.setImageDrawable(getDrawable(R.drawable.ic_arrow_down))
        } else {
            investigationBinding.resultContainerHolder.visible()
            investigationBinding.ivDropDown.setImageDrawable(getDrawable(R.drawable.ic_arrow_up))
        }
    }

    private fun renderInvestigationResultViewContainer(
        investigation: InvestigationModel,
        resultContainer: LinearLayout
    ) {
        investigation.labTestResultList?.forEach { investigationResult ->
            if (resultContainer.findViewWithTag<TextView>(TestedOn) == null) {
                val testedOnSummaryBinding =
                    ResultSummaryInvestigationBinding.inflate(LayoutInflater.from(context))
                testedOnSummaryBinding.tvValue.tag = TestedOn
                testedOnSummaryBinding.tvKey.text = getString(R.string.tested_on)
                investigationResult.testedOn?.let {
                    testedOnSummaryBinding.tvValue.text =
                        convertDateFormat(it, DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DATE_ddMMyyyy)
                } ?: kotlin.run {
                    testedOnSummaryBinding.tvValue.text = getString(R.string.hyphen_symbol)
                }
                resultContainer.addView(testedOnSummaryBinding.root)
            }
            val summaryLayoutBinding =
                ResultSummaryInvestigationBinding.inflate(LayoutInflater.from(context))
            summaryLayoutBinding.root.minimumWidth =
                resources.getDimension(R.dimen._328sdp).roundToInt()
            summaryLayoutBinding.tvKey.text = investigationResult.name

            var rangeBoolean = false
            var rangeDisplay: String? = null

            investigation.resultList?.formLayout?.filter { it.id == investigationResult.name }
                ?.let { list ->
                    if (list.isNotEmpty()) {
                        val formData = list[0]
                        formData.ranges?.filter {
                            it.unitType.equals(
                                investigationResult.unit,
                                true
                            ) && (it.gender.equals(Gender, true) || it.gender.equals(both, true))
                        }?.let { ranges ->
                            if (ranges.isNotEmpty()) {
                                val numberValue: Double? =
                                    investigationResult.value.toString().toDoubleOrNull()
                                val range = ranges[0]
                                rangeDisplay = range.displayRange
                                if (numberValue != null && (numberValue < range.minRange || numberValue > range.maxRange)) {
                                    rangeBoolean = true
                                }
                            }
                        }
                    }
                }

            investigationResult.value.let {
                if (rangeBoolean) {
                    summaryLayoutBinding.tvValueRange.setTextColor(getColor(R.color.medium_high_risk_color))
                    summaryLayoutBinding.tvValue.setTextColor(getColor(R.color.medium_high_risk_color))
                } else {
                    summaryLayoutBinding.tvValueRange.setTextColor(getColor(R.color.navy_blue))
                    summaryLayoutBinding.tvValue.setTextColor(getColor(R.color.navy_blue))
                }
                if (rangeDisplay != null) {
                    summaryLayoutBinding.tvValueRange.text = rangeDisplay
                }
                summaryLayoutBinding.tvValue.text =
                    appendUnitIfExist(it, investigationResult.unit)
            }

            resultContainer.addView(summaryLayoutBinding.root)
        }
    }

    private fun appendUnitIfExist(
        value: Any?,
        unit: String?
    ): CharSequence {

        val displayValue: String

        if (getDecimalFormatted(value).isNotEmpty()) {
            displayValue = getDecimalFormatted(value)
        } else if (value is String) {
            displayValue = value
        } else {
            displayValue = value.toString()
        }


        return if (unit != null)
            "$displayValue $unit"
        else
            displayValue

    }

    private fun renderInvestigationResultContainer(
        investigation: InvestigationModel,
        resultContainer: FlexboxLayout
    ) {
        investigation.resultList?.formLayout?.forEach { formLayout ->
            when (formLayout.viewType) {
                VIEW_TYPE_FORM_DATEPICKER -> createDatePicker(
                    formLayout,
                    resultContainer,
                    investigation
                )

                VIEW_TYPE_FORM_EDITTEXT -> createEditText(
                    formLayout,
                    resultContainer,
                    investigation
                )

                VIEW_TYPE_FORM_SPINNER -> createCustomSpinner(
                    formLayout,
                    resultContainer,
                    investigation
                )
            }
        }
    }

    private fun createCustomSpinner(
        serverViewModel: FormLayout,
        resultContainer: FlexboxLayout,
        investigation: InvestigationModel
    ) {
        val binding = CustomSpinnerLayoutInvestigationBinding.inflate(LayoutInflater.from(context))
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
            if (isMandatory && !CommonUtils.mandatoryNotRequired()) {
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
                        val selectedItem = adapter.getData(position = pos)
                        handleSelectedItem(selectedItem, investigation, id)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        /**
                         * usage of this method is not required
                         */
                    }
                }
            val existingValue = getCategorizedMap(investigation)[id]
            setExistingValueToAdapter(existingValue, dropDownList, binding.etUserInput)
            resultContainer.addView(binding.root)
        }
    }


    private fun createEditText(
        formLayout: FormLayout,
        resultContainer: FlexboxLayout,
        investigation: InvestigationModel
    ) {
        val binding = EdittextLayoutInvestigationBinding.inflate(LayoutInflater.from(context))
        formLayout.apply {
            binding.root.tag = id + rootSuffix
            binding.tvTitle.tag = id + titleSuffix
            binding.etUserInput.tag = id
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvTitleUnit.text = resources.getString(R.string.unit)
            val unitDropDownList = ArrayList<Map<String, Any>>()
            val adapter = CustomSpinnerAdapter(context, translate)
            unitList?.let { list ->
                binding.tvTitleUnit.visible()
                binding.etUserInputSpinner.visible()
                unitDropDownList.add(
                    hashMapOf<String, Any>(
                        DefinedParams.NAME to DefaultIDLabel,
                        DefinedParams.ID to "-1"
                    )
                )
                addDropDownList(list, unitDropDownList)
                adapter.setData(unitDropDownList)
                binding.etUserInputSpinner.adapter = adapter
            }?:kotlin.run {
                binding.etUserInputSpinner.gone()
                binding.tvTitleUnit.gone()
            }
            binding.etUserInputSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>?,
                        view: View?,
                        pos: Int,
                        itemId: Long
                    ) {
                        val selectedItem = adapter.getData(position = pos)
                        handleSelectedItem(selectedItem, investigation, id + unitSuffix)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        /**
                         * usage of this method is not required
                         */
                    }
                }
            binding.tvTitle.text =
                FormSupport.updateTitle(title, translate, titleCulture, unitMeasurement)
            maxLines?.let { binding.etUserInput.setLines(it) }
                ?: binding.etUserInput.setSingleLine()
            getCategorizedMap(investigation)[id]?.let {
                if (it is String)
                    binding.etUserInput.setText(it)
                else if (it is Double)
                    binding.etUserInput.setText(getDecimalFormatted(it))
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

            resultContainer.addView(binding.root)
            binding.etUserInput.addTextChangedListener { editable: Editable? ->
                when {
                    editable.isNullOrBlank() -> {
                        getCategorizedMap(investigation).remove(id)
                        listener.checkValidation()
                    }

                    else -> {
                        if ((inputType != null && (inputType == InputType.TYPE_CLASS_NUMBER ||
                                    inputType == InputType.TYPE_NUMBER_FLAG_DECIMAL))
                        ) {
                            val resultValue = editable.trim().toString().toDoubleOrNull()
                            resultValue?.let {
                                getCategorizedMap(investigation)[id] = resultValue
                                listener.checkValidation()
                            }
                        } else {
                            getCategorizedMap(investigation)[id] =
                                editable.trim().toString()
                            listener.checkValidation()
                        }
                    }
                }
            }
            val existingValue = getCategorizedMap(investigation)[id + unitSuffix]
            setExistingValueToAdapter(existingValue, unitDropDownList, binding.etUserInputSpinner)

            if (isMandatory && !CommonUtils.mandatoryNotRequired()) {
                binding.tvTitle.markMandatory()
            }
        }
    }

    private fun handleSelectedItem(
        selectedItem: Map<String, Any>?,
        investigation: InvestigationModel,
        id: String
    ) {
        selectedItem?.let {
            val selectedId = it[DefinedParams.ID]
            if ((selectedId is String && selectedId == "-1")) {
                getCategorizedMap(investigation).remove(id)
            } else {
                getCategorizedMap(investigation)[id] =
                    selectedId as Any
            }
            listener.checkValidation()
        } ?: kotlin.run {
            getCategorizedMap(investigation).remove(id)
            listener.checkValidation()
        }
    }

    private fun setExistingValueToAdapter(
        existingValue: Any?,
        dropDownList: ArrayList<Map<String, Any>>,
        etUserInputSpinner: AppCompatSpinner
    ) {
        existingValue?.let {
            val selectedMapIndex =
                dropDownList.indexOfFirst { it.containsKey(DefinedParams.ID) && it[DefinedParams.ID] != null && it[DefinedParams.ID] == existingValue }
            if (selectedMapIndex > 0) {
                val selectedMap = dropDownList[selectedMapIndex]
                selectedMap.let { map ->
                    if (map.isNotEmpty()) {
                        etUserInputSpinner.setSelection(selectedMapIndex, true)
                    }
                }
            }
        }
    }


    private fun createDatePicker(
        serverViewModel: FormLayout,
        resultContainer: FlexboxLayout,
        investigation: InvestigationModel
    ) {
        val binding = DatepickerLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.root.minimumWidth = resources.getDimension(R.dimen._328sdp).roundToInt()
            binding.etUserInput.tag = id
            hint?.let {
                binding.etUserInput.hint = it
            }
            binding.tvErrorMessage.tag = id + errorSuffix
            if (translate) {
                binding.tvTitle.text = titleCulture ?: title
            } else {
                binding.tvTitle.text = title
            }
            binding.tvTitle.tag = id + titleSuffix


            getCategorizedMap(investigation)[id]?.let {
                if (it is String) {
                    binding.etUserInput.text = convertDateFormat(
                        it,
                        DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DATE_ddMMyyyy
                    )
                }
            }

            binding.etUserInputHolder.safeClickListener {
                val dateInput = if (binding.etUserInput.text.toString().isNotEmpty())
                    DateUtils.getYearMonthAndDate(binding.etUserInput.text.toString()) else null
                showDatePicker(
                    context = context,
                    disableFutureDate = disableFutureDate ?: false,
                    minDate = getMaxDateLimit(menstrualPeriod, minDays),
                    maxDate = maxDate ?: getMaxDateLimit(maxDays),
                    date = dateInput
                ) { _, year, month, dayOfMonth ->
                    val stringDate = "$dayOfMonth-$month-$year"
                    val parsedDate = DateUtils.getDatePatternDDMMYYYY().parse(stringDate)
                    parsedDate?.let {
                        binding.etUserInput.text = DateUtils.getDateDDMMYYYY().format(it)
                        getCategorizedMap(investigation)[id] = DateUtils.getDateString(
                            parsedDate.time,
                            inputFormat = DateUtils.DATE_FORMAT_yyyyMMdd,
                            outputFormat = DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                        )
                        listener.checkValidation()
                    }
                }
            }

            if (isMandatory && !CommonUtils.mandatoryNotRequired()) {
                binding.tvTitle.markMandatory()
            }

            resultContainer.addView(binding.root)
        }
    }


    private fun showDatePicker(
        context: Context,
        disableFutureDate: Boolean = false,
        minDate: Long? = null,
        maxDate: Long? = null,
        date: Triple<Int?, Int?, Int?>? = null,
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

    private fun getCategorizedMap(
        investigation: InvestigationModel
    ): HashMap<String, Any> {

        investigation.resultHashMap?.let {
            return it
        } ?: kotlin.run {
            investigation.resultHashMap = HashMap()
            return investigation.resultHashMap!!
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

    fun onValidateInput(isLabTech: Boolean): Boolean {
        var isValid = true
        val investigationBinding =
            LayoutInvestigationRowBinding.inflate(LayoutInflater.from(this))
        this.serverData?.let { investigationList ->
            if (isLabTech) {
                val anyResultsEntered : Boolean = investigationList.any { !it.resultHashMap.isNullOrEmpty() }
                if (CommonUtils.isCommunity() || !anyResultsEntered) {
                    investigationList.forEach { investigation ->
                        if ((investigation.resultHashMap.isNullOrEmpty())) {
                            investigation.dropdownState = !investigation.dropdownState
                            toggleFacility(investigationBinding,investigation)
                            investigation.dataError = false
                            isValid = false
                        }
                    }
                }
            }
            investigationList.filter { it.id == null || (it.resultHashMap != null && it.resultHashMap!!.size > 0) }
                .forEach { data ->
                    if (data.resultHashMap != null && data.resultHashMap!!.size == 0) {
                        isValid = true
                        data.dataError = true
                    } else {
                        data.resultList?.formLayout?.forEach { formData ->
                            if ((formData.isMandatory && data.resultHashMap != null && !data.resultHashMap!!.containsKey(
                                    formData.id
                                ))
                                ||
                                (formData.isMandatory && data.resultHashMap != null && data.resultHashMap!!.containsKey(
                                    formData.id
                                )
                                        && data.resultHashMap!![formData.id] is String && (data.resultHashMap!![formData.id] as String).isEmpty())
                            ) {
                                isValid = false
                                data.dataError = false
                            } else if (formData.viewType == VIEW_TYPE_FORM_EDITTEXT) {
                                if (data.resultHashMap != null && data.resultHashMap!!.containsKey(
                                        formData.id
                                    )
                                ) {
                                    val actualValue = data.resultHashMap!![formData.id]
                                    if (actualValue is String && actualValue.isEmpty() && !formData.isMandatory) {
                                        // hideValidationField(data)
                                    } else {
                                        isValid = validateMinMaxLength(
                                            actualValue,
                                            isValid,
                                            formData
                                        ) && validateUnit(formData, data)
                                        data.dataError = isValid
                                    }
                                } else {
                                    // hideValidationField(data)
                                }
                            } else {
                                // hideValidationField(data)
                            }
                        } ?: kotlin.run {
                            isValid = false
                            data.dataError = false
                        }
                    }
                }
        } ?: kotlin.run {
            isValid = false
        }
        return isValid
    }

    private fun validateUnit(formData: FormLayout, data: InvestigationModel): Boolean {
        return if (formData.unitList == null || (formData.unitList != null && formData.unitList!!.size == 0)) {
            true
        } else {
            data.resultHashMap!!.containsKey(
                formData.id + com.medtroniclabs.spice.common.DefinedParams.Unit
            )
        }
    }


    private fun validateMinMaxLength(
        actualValue: Any?,
        valid: Boolean,
        serverViewModel: FormLayout
    ): Boolean {
        var isValid = valid
        serverViewModel.apply {
            if (minLength != null && viewType == VIEW_TYPE_FORM_EDITTEXT
                && actualValue != null && actualValue is String
                && actualValue.length < minLength!!
            ) {
                isValid = false
                /*requestFocusView(
                    serverViewModel,
                    getString(
                        R.string.min_char_length_validation,
                        minLength!!.toString()
                    )
                )*/
            } else if (maxValue != null || minValue != null) {
                if (maxValue != null && minValue != null) {
                    if (actualValue is String) {
                        actualValue.toDoubleOrNull()?.let { value ->
                            if (value < minValue!! || value > maxValue!!) {
                                isValid = false
                                /*requestFocusView(
                                    serverViewModel,
                                    getString(
                                        R.string.general_min_max_validation,
                                        CommonUtils.getDecimalFormatted(
                                            minValue!!
                                        ),
                                        CommonUtils.getDecimalFormatted(
                                            maxValue!!
                                        )
                                    )
                                )*/
                            } else {
                                // hideValidationField(serverViewModel)
                            }
                        }
                    } else if (actualValue is Number) {
                        actualValue.toDouble().let { value ->
                            if (value < minValue!! || value > maxValue!!) {
                                isValid = false
                                /*requestFocusView(
                                    serverViewModel,
                                    getString(
                                        R.string.general_min_max_validation,
                                        CommonUtils.getDecimalFormatted(
                                            minValue!!
                                        ),
                                        CommonUtils.getDecimalFormatted(
                                            maxValue!!
                                        )
                                    )
                                )*/
                            } else {
                                // hideValidationField(serverViewModel)
                            }
                        }
                    } else {
                        // hideValidationField(serverViewModel)
                    }
                } else if (minValue != null) {
                    if (actualValue is String) {
                        actualValue.toDoubleOrNull()?.let { value ->
                            if (value < minValue!!) {
                                isValid = false
                                /*requestFocusView(
                                    serverViewModel,
                                    getString(
                                        R.string.general_min_validation,
                                        CommonUtils.getDecimalFormatted(
                                            minValue!!
                                        )
                                    )
                                )*/
                            } else {
                                // hideValidationField(serverViewModel)
                            }
                        }
                    } else if (actualValue is Number) {
                        actualValue.toDouble().let { value ->
                            if (value < minValue!!) {
                                isValid = false
                                /*requestFocusView(
                                    serverViewModel,
                                    getString(
                                        R.string.general_min_validation,
                                        CommonUtils.getDecimalFormatted(
                                            minValue!!
                                        )
                                    )
                                )*/
                            } else {
                                // hideValidationField(serverViewModel)
                            }
                        }
                    } else {
                        // hideValidationField(serverViewModel)
                    }
                } else if (maxValue != null) {
                    if (actualValue is String) {
                        actualValue.toDoubleOrNull()?.let { value ->
                            if (value > maxValue!!.toDouble()) {
                                isValid = false
                                /*requestFocusView(
                                    serverViewModel,
                                    getString(
                                        R.string.general_max_validation,
                                        CommonUtils.getDecimalFormatted(
                                            maxValue!!
                                        )
                                    )
                                )*/
                            } else {
                                // hideValidationField(serverViewModel)
                            }
                        }
                    } else if (actualValue is Number) {
                        actualValue.toDouble().let { value ->
                            if (value > maxValue!!.toDouble()) {
                                isValid = false
                                /*requestFocusView(
                                    serverViewModel,
                                    getString(
                                        R.string.general_max_validation,
                                        CommonUtils.getDecimalFormatted(
                                            maxValue!!
                                        )
                                    )
                                )*/
                            } else {
                                //  hideValidationField(serverViewModel)
                            }
                        }
                    } else {
                        //  hideValidationField(serverViewModel)
                    }
                }
            } else if (contentLength != null) {
                if (actualValue is Number) {
                    val actualValueString =
                        getDecimalFormatted(actualValue)
                    if (contentLength == actualValueString.length) {
                        // hideValidationField(serverViewModel)
                    } else {
                        isValid = false
                        //  requestFocusView(serverViewModel)
                    }
                } else {
                    val actualValueString = actualValue.toString()
                    if (contentLength == actualValueString.length) {
                        //  hideValidationField(serverViewModel)
                    } else {
                        isValid = false
                        // requestFocusView(serverViewModel)
                    }
                }
            } else {
                //  hideValidationField(serverViewModel)
            }
        }
        return isValid
    }

    fun getResultFromInvestigation(): List<InvestigationModel>? {
        return serverData
    }

    fun setPatientGender(gender: String) {
        Gender = gender
    }


}