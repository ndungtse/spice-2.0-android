package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.hideKeyboard
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.loadAsGif
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.appextensions.resetImageView
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.takeIfNotNull
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMdd
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.calculateGestationalAge
import com.medtroniclabs.spice.common.DateUtils.formatGestationalAge
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.PregnancyDetailsModel
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.DialogNcdPregnancyBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDPregnancyViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.EstimatedDeliveryDate
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.initTextWatcherForDouble
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.initTextWatcherForInt
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class NCDPregnancyDialog(private val callback: ((isPositiveResult: Boolean, message: String) -> Unit)) : DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogNcdPregnancyBinding
    private val viewModel: NCDPregnancyViewModel by viewModels()

    private lateinit var neonatalOutcomesView: TagListCustomView
    private lateinit var maternalOutcomesView: TagListCustomView
    private var datePickerDialog: DatePickerDialog? = null

    override fun onStart() {
        super.onStart()
        handleOrientation()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleOrientation()
    }

    private fun handleOrientation() {
        val isTablet = CommonUtils.checkIsTablet(requireContext())
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val percent = when {
            isTablet && isLandscape -> 70
            isTablet && !isLandscape -> 90
            else -> 100
        }
        setDialogPercent(percent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogNcdPregnancyBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        clickListener()
        attachObserver()
    }

    private fun initView() {
        viewModel.relatedPersonFhirId = arguments?.getString(PATIENT_ID)
        with(binding) {
            tvPregnant.markMandatory()

            initTextWatcherForInt(etGravida) {
                viewModel.ncdPregnancyCreateModel.gravida = it
            }
            initTextWatcherForInt(etParity) {
                viewModel.ncdPregnancyCreateModel.parity = it
            }
            initTextWatcherForInt(etTemperature) {
                viewModel.ncdPregnancyCreateModel.temperature = it
            }
            tvLastMenstrualPeriodDate.addTextChangedListener {
                binding.tvLastMenstrualPeriodError.gone()
                if (viewModel.ncdPregnancyDetailsResponse.value?.data?.estimatedDeliveryDate.isNullOrBlank())
                    calculateGestationalAgeAndEstimationDeliveryDate(it.toString())

                viewModel.ncdPregnancyCreateModel.lastMenstrualPeriod =
                    apiFormattedDate(it.toString())
            }
            tvLastMenstrualPeriodDate.safeClickListener(this@NCDPregnancyDialog)
            tvEstimatedDeliveryDate.addTextChangedListener {
                viewModel.ncdPregnancyCreateModel.estimatedDeliveryDate =
                    apiFormattedDate(it.toString())
            }
            tvGestationalText.addTextChangedListener {
                viewModel.ncdPregnancyCreateModel.gestationalAge = removeWeeksStr(it.toString())
            }
            initTextWatcherForInt(etNoFetuses) {
                viewModel.ncdPregnancyCreateModel.noOfFetus = it
            }
            initTextWatcherForDouble(etWeight) {
                viewModel.ncdPregnancyCreateModel.weight = it
            }

            if (viewModel.isPregnancyAncEnabledSite) {
                pregnancyGroup.gone()
                pregnancyAncGroup.visible()

                tvLastMenstrualPeriodLabel.markMandatory()

                neonatalOutcomesView = TagListCustomView(
                    binding.root.context, binding.cgNeonatalOutcomes
                ) { _, _, _ -> }
                neonatalOutcomesView.addChipItemList(neoNatalOutcomes())

                maternalOutcomesView = TagListCustomView(
                    binding.root.context, binding.cgMaternalOutcomes
                ) { _, _, _ -> }
                maternalOutcomesView.addChipItemList(maternalOutcomes())

                tvDeliveryDateLabel.markMandatory()
                tvDeliveryDate.addTextChangedListener {
                    viewModel.ncdPregnancyCreateModel.actualDeliveryDate =
                        apiFormattedDate(it.toString())
                }
                tvDeliveryDate.safeClickListener(this@NCDPregnancyDialog)

                viewModel.ncdPregnancyCreateModel.isPregnancyAnc = true
            } else {
                pregnancyGroup.visible()
                pregnancyAncGroup.gone()

                tvPregnancyDiagnosisLbl.markMandatory()
                tvPatientTreatmentLbl.markMandatory()

                tvDiagnosesTime.addTextChangedListener {
                    viewModel.ncdPregnancyCreateModel.diagnosisTime =
                        apiFormattedDate(it.toString())
                }
                tvDiagnosesTime.safeClickListener(this@NCDPregnancyDialog)

                viewModel.ncdPregnancyCreateModel.isPregnancyAnc = false
            }

            ncdDiabetesHypertension.tvDiabetes.markMandatory()
            ncdDiabetesHypertension.tvHypertension.markMandatory()

            btnCancel.safeClickListener(this@NCDPregnancyDialog)
            btnConfirm.safeClickListener(this@NCDPregnancyDialog)
            ivClose.safeClickListener(this@NCDPregnancyDialog)
        }
        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = DIABETES
            view.addViewElements(
                it,
                false,
                viewModel.resultDiabetesHashMap,
                Pair(DIABETES, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForDiabetes
            )
            binding.ncdDiabetesHypertension.llDiabetes.addView(view)
        }
        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = HYPERTENSION
            view.addViewElements(
                it,
                false,
                viewModel.resultHypertensionHashMap,
                Pair(HYPERTENSION, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForHypertension
            )
            binding.ncdDiabetesHypertension.llHypertension.addView(view)
        }
        getSingleSelectionOptionsForPregnant().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = PREGNANT_STATUS
            view.addViewElements(
                it,
                false,
                viewModel.resultPregnantHashMap,
                Pair(PREGNANT_STATUS, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForPregnant
            )
            binding.llPregnant.addView(view)
        }
        viewModel.relatedPersonFhirId?.let { id ->
            viewModel.ncdPregnancyDetails(id)
        }
        binding.loadingProgress.safeClickListener {

        }
        binding.loadingProgress.bringToFront()
    }

    private fun removeWeeksStr(weeks: String): Long? {
        return weeks.split(" ")[0].toLongOrNull()
    }

    private fun apiFormattedDate(toString: String): String {
        return DateUtils.convertDateFormat(
            toString, DATE_ddMMyyyy, DATE_FORMAT_yyyyMMdd
        )
    }

    private var singleSelectionCallbackForDiabetes: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultDiabetesHashMap[DIABETES] = selectedID as String
        }

    private var singleSelectionCallbackForHypertension: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultHypertensionHashMap[HYPERTENSION] = selectedID as String
        }

    private var singleSelectionCallbackForPregnant: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            (selectedID as? String?)?.let { pregnantStatus ->
                viewModel.resultPregnantHashMap[PREGNANT_STATUS] = pregnantStatus

                clearFields()

                val isPregnant = pregnantStatus.equals(PREGNANT, true)
                binding.clPregnant.visibility = if (isPregnant) View.VISIBLE else View.GONE
                viewModel.ncdPregnancyCreateModel.isPregnant = isPregnant

                binding.tvPregnantError.gone()
            }
        }

    private fun neoNatalOutcomes(): ArrayList<ChipViewItemModel> {
        val items = listOf(
            Pair(STILL_BIRTH, getString(R.string.still_birth)),
            Pair(LIVE_BIRTH, getString(R.string.live_birth)),
            Pair(NEO_NATAL_DEATH, getString(R.string.neo_natal_death))
        )
        val chipItems = ArrayList<ChipViewItemModel>()
        items.forEachIndexed { index, element ->
            chipItems.add(
                ChipViewItemModel(
                    id = (index + 1).toLong(),
                    value = element.first,
                    name = element.second
                )
            )
        }
        return chipItems
    }

    private fun maternalOutcomes(): ArrayList<ChipViewItemModel> {
        val items = listOf(
            Pair(ALIVE_AND_WELL, getString(R.string.alive_well)),
            Pair(MATERNAL_DEATH, getString(R.string.maternal_death))
        )
        val chipItems = ArrayList<ChipViewItemModel>()
        items.forEachIndexed { index, element ->
            chipItems.add(
                ChipViewItemModel(
                    id = (index + 1).toLong(),
                    value = element.first,
                    name = element.second
                )
            )
        }
        return chipItems
    }

    private fun getSingleSelectionOptions(): ArrayList<Map<String, Any>> {
        val yearOfDiagnosis = ArrayList<Map<String, Any>>()
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                N_A, getString(R.string.n_a)
            )
        )
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                NEW_PATIENT, getString(R.string.new_patient)
            )
        )
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                KNOWN_PATIENT, getString(R.string.known_patient)
            )
        )
        return yearOfDiagnosis
    }

    private fun getSingleSelectionOptionsForPregnant(): ArrayList<Map<String, Any>> {
        val pregnantList = ArrayList<Map<String, Any>>()
        pregnantList.add(
            CommonUtils.getOptionMap(
                PREGNANT, getString(R.string.pregnant)
            )
        )
        pregnantList.add(
            CommonUtils.getOptionMap(
                NOT_PREGNANT, getString(R.string.not_pregnant)
            )
        )
        return pregnantList
    }

    private fun clickListener() {
        binding.mcbNone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.mcbEclampsia.isChecked = false
                binding.mcbPreEclampsia.isChecked = false
                binding.mcbGestationalDiabetes.isChecked = false
            }
            handleCheckBox()
        }
        binding.mcbEclampsia.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                binding.mcbNone.isChecked = false
            handleCheckBox()
        }
        binding.mcbPreEclampsia.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                binding.mcbNone.isChecked = false
            handleCheckBox()
        }
        binding.mcbGestationalDiabetes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                binding.mcbNone.isChecked = false
            handleCheckBox()
        }

        binding.rgPatientTreatment.setOnCheckedChangeListener { _, selectedItemId ->
            binding.tvDiagnosesTime.text = getString(R.string.empty)
            binding.tvPatientTreatmentError.gone()
            when (selectedItemId) {
                R.id.rbYes -> {
                    viewModel.ncdPregnancyCreateModel.isOnTreatment = true
                    binding.diagnosesTimeGroup.visible()
                }

                R.id.rbNo -> {
                    viewModel.ncdPregnancyCreateModel.isOnTreatment = false
                    binding.diagnosesTimeGroup.gone()
                }

                else -> {
                    viewModel.ncdPregnancyCreateModel.isOnTreatment = null
                    binding.diagnosesTimeGroup.gone()
                }
            }
        }
        binding.cgNeonatalOutcomes.setOnCheckedStateChangeListener { _, checkedIds ->
            val selected = neonatalOutcomesView.getSelectedTags()
            if (selected.isNotEmpty())
                viewModel.ncdPregnancyCreateModel.neonatalOutcomes =
                    selected[0].name.ifBlank { null }
            if (checkedIds.size > 0) {
                binding.actualDeliveryDateGroup.visible()
            } else {
                binding.actualDeliveryDateGroup.gone()
                resetDeliveryDate()
            }
        }
        binding.cgMaternalOutcomes.setOnCheckedStateChangeListener { _, _ ->
            val selected = maternalOutcomesView.getSelectedTags()
            if (selected.isNotEmpty())
                viewModel.ncdPregnancyCreateModel.maternalOutcomes =
                    selected[0].name.ifBlank { null }
        }
    }

    private fun resetDeliveryDate() {
        binding.tvDeliveryDate.text = DateUtils.getCurrentDateAndTime(DATE_ddMMyyyy)
    }

    private fun handleCheckBox() {
        val showPatientTreatment =
            binding.mcbEclampsia.isChecked || binding.mcbPreEclampsia.isChecked || binding.mcbGestationalDiabetes.isChecked
        if (showPatientTreatment) {
            binding.patientTreatmentGroup.visible()
        } else {
            binding.patientTreatmentGroup.gone()
            binding.rgPatientTreatment.clearCheck()
        }
        setPregnancyDiagnoses()
    }

    private fun setPregnancyDiagnoses() {
        val selectedItems = ArrayList<Map<String, Any>>()
        if (binding.mcbNone.isChecked)
            selectedItems.add(
                CommonUtils.getOptions(
                    NONE,
                    getString(R.string.none)
                )
            )
        if (binding.mcbEclampsia.isChecked)
            selectedItems.add(
                CommonUtils.getOptions(
                    ECLAMPSIA,
                    getString(R.string.eclampsia)
                )
            )
        if (binding.mcbPreEclampsia.isChecked)
            selectedItems.add(
                CommonUtils.getOptions(
                    PRE_ECLAMPSIA,
                    getString(R.string.pre_eclampsia)
                )
            )
        if (binding.mcbGestationalDiabetes.isChecked)
            selectedItems.add(
                CommonUtils.getOptions(
                    GESTATIONAL_DIABETES,
                    getString(R.string.gestational_diabetes)
                )
            )
        viewModel.ncdPregnancyCreateModel.diagnosis = selectedItems
        binding.tvPregnancyDiagnosisError.gone()
    }

    private fun attachObserver() {
        viewModel.ncdPregnancyCreateResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    callback.invoke(
                        true, resourceState.data?.message ?: ""
                    )
                    dismiss()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    callback.invoke(
                        false,
                        resourceState.message ?: getString(R.string.something_went_wrong_try_later)
                    )
                    dismiss()
                }
            }
        }
        viewModel.ncdPregnancyDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        populateFields(it)
                    }
                    hideLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun clearFields() {
        with(binding) {
            requireContext().hideKeyboard(root)

            //Common Fields
            etGravida.text?.clear()
            tvGravidaError.gone()

            etParity.text?.clear()
            tvParityError.gone()

            etTemperature.text?.clear()
            tvTemperatureError.gone()

            tvLastMenstrualPeriodDate.text = getString(R.string.empty)

            tvEstimatedDeliveryDate.text = getString(R.string.empty)

            etNoFetuses.text?.clear()
            tvNoFetusesError.gone()

            etWeight.text?.clear()
            tvWeightError.gone()

            tvGestationalText.text = getString(R.string.hyphen_symbol)

            //Pregnancy Fields
            mcbNone.isChecked = false
            mcbEclampsia.isChecked = false
            mcbPreEclampsia.isChecked = false
            mcbGestationalDiabetes.isChecked = false

            //Pregnancy ANC Fields
            cgNeonatalOutcomes.clearCheck()
            resetDeliveryDate()
            cgMaternalOutcomes.clearCheck()
        }
    }

    companion object {
        const val PATIENT_ID = "PATIENT_ID"

        const val STILL_BIRTH = "Still Birth"
        const val LIVE_BIRTH = "Live Birth"
        const val NEO_NATAL_DEATH = "Neo Natal Death"

        const val ALIVE_AND_WELL = "Alive & Well"
        const val MATERNAL_DEATH = "Maternal Death"

        const val N_A = "N/A"
        const val NEW_PATIENT = "New Patient"
        const val KNOWN_PATIENT = "Known Patient"
        const val DIABETES = "Diabetes"
        const val HYPERTENSION = "Hypertension"

        const val PREGNANT_STATUS = "PregnantStatus"
        const val PREGNANT = "Pregnant"
        const val NOT_PREGNANT = "Not Pregnant"

        const val NONE = "none"
        const val ECLAMPSIA = "eclampsia"
        const val PRE_ECLAMPSIA = "preEclampsia"
        const val GESTATIONAL_DIABETES = "gestationalDiabetes"

        const val TAG = "NCDPregnancyCreateDialog"
        fun newInstance(patientId: String, callback: ((isPositiveResult: Boolean, message: String) -> Unit)): NCDPregnancyDialog {
            val fragment = NCDPregnancyDialog(callback)
            val bundle = Bundle()
            bundle.putString(PATIENT_ID, patientId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnConfirm.id -> {
                (activity as? BaseActivity?)?.withNetworkAvailability(online = {
                    if (validateInputs()) {
                        viewModel.ncdPregnancyCreateModel.apply {
                            relatedPersonFhirId = viewModel.relatedPersonFhirId
                        }.also {
                            viewModel.ncdPregnancyCreate(it)
                        }
                    }
                })
            }

            binding.tvLastMenstrualPeriodDate.id -> showDatePickerDialog(binding.tvLastMenstrualPeriodDate)

            binding.tvDiagnosesTime.id -> showDatePickerDialog(binding.tvDiagnosesTime)

            binding.tvDeliveryDate.id -> showDatePickerDialog(binding.tvDeliveryDate)

            binding.ivClose.id, binding.btnCancel.id -> dismiss()
        }
    }

    private fun validateInputs(): Boolean {
        with(viewModel.ncdPregnancyCreateModel) {
            if (isPregnant == null) {
                binding.tvPregnantError.visible()
                return false
            } else if (isPregnant == true) {
                if (viewModel.isPregnancyAncEnabledSite) {
                    if (lastMenstrualPeriod.isNullOrBlank()) {
                        binding.tvLastMenstrualPeriodError.visible()
                        return false
                    }
                } else {
                    if (diagnosis.isNullOrEmpty()) {
                        binding.tvPregnancyDiagnosisError.visible()
                        return false
                    } else {
                        diagnosis?.let { list ->
                            if (list.isNotEmpty()) {
                                val isNone =
                                    list.size == 1 && list[0][DefinedParams.value] == N_A
                                if (!isNone && isOnTreatment == null) {
                                    binding.tvPatientTreatmentError.visible()
                                    return false
                                }
                            }
                        }
                    }
                }
            }
        }

        return true
    }

    private fun showDatePickerDialog(textView: AppCompatTextView) {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!textView.text.isNullOrBlank()) yearMonthDate =
            DateUtils.convertedMMMToddMM(textView.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(context = requireContext(),
                minDate = null,
                date = yearMonthDate,
                isMenstrualPeriod = true,
                disableFutureDate = true,
                cancelCallBack = { datePickerDialog = null }) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                textView.text = DateUtils.convertDateTimeToDate(
                    stringDate, DATE_FORMAT_ddMMyyyy, DATE_ddMMyyyy
                )
                datePickerDialog = null
            }
        }
    }

    private fun calculateGestationalAgeAndEstimationDeliveryDate(lmpText: String) {
        if (lmpText.isNotEmpty()) {
            val lmpDate = LocalDate.parse(lmpText, DateTimeFormatter.ofPattern(DATE_ddMMyyyy))
            val estimatedDeliveryDate = lmpDate.plusDays(EstimatedDeliveryDate)
            val formattedEstimatedDeliveryDate =
                estimatedDeliveryDate.format(DateTimeFormatter.ofPattern(DATE_ddMMyyyy))
            binding.tvEstimatedDeliveryDate.text = formattedEstimatedDeliveryDate

            val gestationalAgeInWeeks = calculateGestationalAge(lmpDate)
            binding.tvGestationalText.text =
                formatGestationalAge(gestationalAgeInWeeks, requireContext())
        }
    }

    private fun populateFields(model: PregnancyDetailsModel) {
        with(model) {
            binding.apply {
                val pregnantTag = "${PREGNANT}_$PREGNANT_STATUS"
                val notPregnantTag = "${NOT_PREGNANT}_$PREGNANT_STATUS"

                if (isPregnant == true)
                    llPregnant.findViewWithTag<TextView>(pregnantTag)?.performClick()
                else
                    llPregnant.findViewWithTag<TextView>(notPregnantTag)?.performClick()
                
                etGravida.setText(model.gravida.takeIfNotNull())
                etParity.setText(model.parity.takeIfNotNull())
                etTemperature.setText(model.temperature.takeIfNotNull())
                tvLastMenstrualPeriodDate.text = model.lastMenstrualPeriod?.let {
                    DateUtils.convertDateFormat(it, DATE_FORMAT_yyyyMMdd, DATE_ddMMyyyy)
                }
                tvEstimatedDeliveryDate.text = model.estimatedDeliveryDate?.let {
                    DateUtils.convertDateFormat(it, DATE_FORMAT_yyyyMMdd, DATE_ddMMyyyy)
                }
                tvGestationalText.text = model.gestationalAge?.let {
                    formatGestationalAge(it, requireContext())
                } ?: ""
                etNoFetuses.setText(model.noOfFetus.takeIfNotNull())
                etWeight.setText(model.weight.takeIfNotNull())

                if (viewModel.isPregnancyAncEnabledSite) {
                    neonatalOutcomes?.let {
                        if (it.isNotBlank())
                            neonatalOutcomesView.populateChipByName(
                                cgNeonatalOutcomes,
                                neoNatalOutcomes(),
                                it
                            ) {
                                tvDeliveryDate.text =
                                    model.actualDeliveryDate?.let { deliveryDate ->
                                        DateUtils.convertDateFormat(
                                            deliveryDate,
                                            DATE_FORMAT_yyyyMMdd,
                                            DATE_ddMMyyyy
                                        )
                                    }
                            }
                    }
                    maternalOutcomes?.let {
                        if (it.isNotBlank())
                            maternalOutcomesView.populateChipByName(
                                cgMaternalOutcomes,
                                maternalOutcomes(),
                                it
                            ) {}
                    }
                } else {
                    if (!diagnosis.isNullOrEmpty()) {
                        diagnosis?.forEach { map ->
                            when (map[DefinedParams.value]) {
                                NONE -> mcbNone.isChecked = true
                                ECLAMPSIA -> mcbEclampsia.isChecked = true
                                PRE_ECLAMPSIA -> mcbPreEclampsia.isChecked = true
                                GESTATIONAL_DIABETES -> mcbGestationalDiabetes.isChecked = true
                            }
                        }
                        if (isOnTreatment == true) {
                            rbYes.isChecked = true
                            tvDiagnosesTime.text = model.diagnosisTime?.let {
                                DateUtils.convertDateFormat(
                                    it,
                                    DATE_FORMAT_yyyyMMdd,
                                    DATE_ddMMyyyy
                                )
                            }
                        } else if (isOnTreatment == false) {
                            rbNo.isChecked = true
                        }
                    }
                }
            }
            viewModel.ncdPregnancyDetailsResponse.postSuccess(null)
        }
    }

    fun showLoading() {
        binding.apply {
            btnConfirm.invisible()
            btnCancel.invisible()
            loadingProgress.visible()
            loaderImage.apply {
                loadAsGif(R.drawable.loader_spice)
            }
        }
    }

    fun hideLoading() {
        binding.apply {
            btnConfirm.visible()
            btnCancel.visible()
            loadingProgress.gone()
            loaderImage.apply {
                resetImageView()
            }
        }
    }
}