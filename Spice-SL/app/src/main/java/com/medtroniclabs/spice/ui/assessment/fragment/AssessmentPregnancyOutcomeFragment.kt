package com.medtroniclabs.spice.ui.assessment.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.CardLayoutBinding
import com.medtroniclabs.spice.databinding.CheckboxDialogSpinnerLayoutBinding
import com.medtroniclabs.spice.databinding.EdittextLayoutBinding
import com.medtroniclabs.spice.databinding.FragmentAssessmentBinding
import com.medtroniclabs.spice.databinding.LayoutSingleSelectionBinding
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment responsible for pregnancy outcome assessment
 */
@AndroidEntryPoint
class AssessmentPregnancyOutcomeFragment :
    BaseFragment(),
    FormEventListener,
    View.OnClickListener {
    private lateinit var binding: FragmentAssessmentBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    private lateinit var formGenerator: FormGenerator

    private val MAX_BABIES = 5
    private val NEWBORN_CONTAINER_TAG = "newbornDynamicContainer"
    private var currentBabyCount = 0

    // Store baby data: babyIndex (1-based) -> fieldId -> value
    private val babyDataMap = HashMap<Int, HashMap<String, Any>>()

    // Neonatal death cause options
    private val neonatalDeathCauseOptions: ArrayList<Map<String, Any>> = arrayListOf(
        mapOf("id" to "asphyxia", "name" to "Asphyxia", "cultureValue" to "শ্বাসরোধ"),
        mapOf("id" to "abnormallyLowTemperature", "name" to "Abnormally low temperature", "cultureValue" to "অস্বাভাবিক কম তাপমাত্রা"),
        mapOf("id" to "lowBirthWeight", "name" to "Low birth weight", "cultureValue" to "কম জন্ম ওজন"),
        mapOf("id" to "convulsions", "name" to "Convulsions", "cultureValue" to "খিঁচুনি"),
        mapOf("id" to "prematureBirth", "name" to "Premature birth", "cultureValue" to "অপরিণত জন্ম"),
        mapOf("id" to "sepsisUmbilicalSepsis", "name" to "Sepsis/ Umbilical sepsis", "cultureValue" to "নাভির সংক্রমণ / সংক্রমণ"),
        mapOf("id" to "pneumonia", "name" to "Pneumonia", "cultureValue" to "নিউমোনিয়া"),
        mapOf("id" to "congenitalAnomaly", "name" to "Congenital Anomaly", "cultureValue" to "জন্মগত ত্রুটি"),
        mapOf("id" to "unknown", "name" to "Unknown", "cultureValue" to "জানা নেই"),
    )

    // Sex options
    private val sexOptions: ArrayList<Map<String, Any>> = arrayListOf(
        mapOf("id" to "male", "name" to "Male", "cultureValue" to "পুরুষ"),
        mapOf("id" to "female", "name" to "Female", "cultureValue" to "নারী"),
        mapOf("id" to "other", "name" to "Other", "cultureValue" to "অন্যান্য"),
    )

    // Baby alive options
    private val babyAliveOptions: ArrayList<Map<String, Any>> = arrayListOf(
        mapOf("id" to "Yes", "name" to "Yes", "cultureValue" to "হ্যাঁ"),
        mapOf("id" to "No", "name" to "No", "cultureValue" to "না"),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        setListener()
        UserDetail.startDateTime = AnalyticsUtils.getCurrentDateTimeInLocalTime()
        viewModel.setUserJourney(AnalyticsDefinedParams.PREGNANCY_OUTCOME)
    }

    private fun attachObservers() {
        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        // Filter out fields marked as isSummary to hide them in form view
                        val filteredFormLayout = data.formLayout.filter { it.isSummary != true }
                        formGenerator.populateViews(filteredFormLayout)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.LOADING -> {
                    showProgress()
                }
            }
        }
    }

    private fun setListener() {
        binding.btnSubmit.safeClickListener(this)
    }

    fun initView() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG,
        )
        childFragmentManager.executePendingTransactions()
        formGenerator = FormGenerator(
            requireContext(),
            binding.llForm,
            this,
            binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled(),
            callback = { resultMap, changedFieldId ->
                if (changedFieldId == "liveBirthNumbers") {
                    val count = (resultMap["liveBirthNumbers"] as? Number)?.toInt() ?: 0
                    updateBabySections(count)
                }
            },
        )
        viewModel.getFormData(MenuConstants.PREGNANCY_OUTCOME)
    }

    /**
     * Creates or updates the dynamic baby sections based on liveBirthNumbers value
     */
    private fun updateBabySections(count: Int) {
        val effectiveCount = count.coerceIn(0, MAX_BABIES)

        if (count > MAX_BABIES) {
            Toast
                .makeText(
                    requireContext(),
                    getString(R.string.max_babies_allowed, MAX_BABIES),
                    Toast.LENGTH_SHORT,
                ).show()
        }

        // Remove existing dynamic container
        val existingContainer = binding.llForm.findViewWithTag<LinearLayout>(NEWBORN_CONTAINER_TAG)
        existingContainer?.let { binding.llForm.removeView(it) }

        // Clear baby data for removed babies
        val keysToRemove = babyDataMap.keys.filter { it > effectiveCount }
        keysToRemove.forEach { babyDataMap.remove(it) }

        currentBabyCount = effectiveCount

        if (effectiveCount <= 0) {
            return
        }

        // Create a container for all baby sections
        val container = LinearLayout(requireContext()).apply {
            tag = NEWBORN_CONTAINER_TAG
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }

        val translate = SecuredPreference.getIsTranslationEnabled()

        for (babyIndex in 1..effectiveCount) {
            // Ensure baby data map exists for this baby
            if (!babyDataMap.containsKey(babyIndex)) {
                babyDataMap[babyIndex] = HashMap()
            }

            val babyCard = createBabyCard(babyIndex, translate)
            container.addView(babyCard)
        }

        // Find the position of the counselling section and insert before it
        // or just append at the end of the form
        val counsellingCardTag = "counsellingAdverseEvent" + AssessmentDefinedParams.rootSuffix
        val counsellingView = binding.llForm.findViewWithTag<View>(counsellingCardTag)
        if (counsellingView != null) {
            val counsellingIndex = binding.llForm.indexOfChild(counsellingView)
            binding.llForm.addView(container, counsellingIndex)
        } else {
            binding.llForm.addView(container)
        }
    }

    /**
     * Creates a single baby card with all fields
     */
    private fun createBabyCard(
        babyIndex: Int,
        translate: Boolean,
    ): View {
        val cardBinding = CardLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        cardBinding.cardTitle.text = getString(R.string.baby_label, babyIndex)
        cardBinding.root.tag = "babyCard_$babyIndex"

        val familyRoot = cardBinding.llFamilyRoot

        // 1. Is the baby alive - SingleSelectionView
        addIsBabyAliveField(familyRoot, babyIndex, translate)

        // 2. Sex - SingleSelectionView
        addSexField(familyRoot, babyIndex, translate)

        // 3. Birth Weight - EditText
        addBirthWeightField(familyRoot, babyIndex)

        // 4. Cause of neonatal death - DialogCheckbox (initially hidden)
        addCauseOfDeathField(familyRoot, babyIndex, translate)

        return cardBinding.root
    }

    private fun addIsBabyAliveField(
        parent: LinearLayout,
        babyIndex: Int,
        translate: Boolean,
    ) {
        val selectionBinding = LayoutSingleSelectionBinding.inflate(
            LayoutInflater.from(requireContext()),
        )
        selectionBinding.root.tag = "isBabyAlive_${babyIndex}_root"
        selectionBinding.tvTitle.text = if (translate) {
            getString(R.string.is_baby_alive)
        } else {
            getString(R.string.is_baby_alive)
        }

        val singleSelectionView = SingleSelectionCustomView(requireContext())
        singleSelectionView.tag = "isBabyAlive_$babyIndex"

        val resultMap = babyDataMap[babyIndex] ?: HashMap()
        val formLayout = FormLayout(
            viewType = "SingleSelectionView",
            id = "isBabyAlive_$babyIndex",
            title = getString(R.string.is_baby_alive),
            optionsList = babyAliveOptions,
            visibility = "visible",
        )

        singleSelectionView.addViewElements(
            babyAliveOptions,
            translate,
            resultMap,
            Pair(AssessmentDefinedParams.IS_BABY_ALIVE, null),
            formLayout,
        ) { selectedId, _, _, name ->
            babyDataMap[babyIndex]?.set(AssessmentDefinedParams.IS_BABY_ALIVE, selectedId ?: "")

            // Toggle cause of neonatal death visibility
            val causeRoot = parent.findViewWithTag<View>("${AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH}_${babyIndex}_root")
            if (name == "No") {
                causeRoot?.visibility = View.VISIBLE
            } else {
                causeRoot?.visibility = View.GONE
                // Clear cause of death data when baby is alive
                babyDataMap[babyIndex]?.remove(AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH)
                // Reset the checkbox text
                val checkboxView = parent.findViewWithTag<View>("${AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH}_$babyIndex")
                if (checkboxView is androidx.appcompat.widget.AppCompatTextView) {
                    checkboxView.text = getString(R.string.cause_of_death_hint)
                }
            }
        }

        selectionBinding.selectionGroup.addView(singleSelectionView)
        parent.addView(selectionBinding.root)
    }

    private fun addSexField(
        parent: LinearLayout,
        babyIndex: Int,
        translate: Boolean,
    ) {
        val selectionBinding = LayoutSingleSelectionBinding.inflate(
            LayoutInflater.from(requireContext()),
        )
        selectionBinding.root.tag = "sex_${babyIndex}_root"
        selectionBinding.tvTitle.text = if (translate) {
            getString(R.string.sex_label)
        } else {
            getString(R.string.sex_label)
        }

        val singleSelectionView = SingleSelectionCustomView(requireContext())
        singleSelectionView.tag = "sex_$babyIndex"

        val resultMap = babyDataMap[babyIndex] ?: HashMap()
        val formLayout = FormLayout(
            viewType = "SingleSelectionView",
            id = "sex_$babyIndex",
            title = getString(R.string.sex_label),
            optionsList = sexOptions,
            visibility = "visible",
        )

        singleSelectionView.addViewElements(
            sexOptions,
            translate,
            resultMap,
            Pair(AssessmentDefinedParams.SEX, null),
            formLayout,
        ) { selectedId, _, _, _ ->
            babyDataMap[babyIndex]?.set(AssessmentDefinedParams.SEX, selectedId ?: "")
        }

        selectionBinding.selectionGroup.addView(singleSelectionView)
        parent.addView(selectionBinding.root)
    }

    private fun addBirthWeightField(
        parent: LinearLayout,
        babyIndex: Int,
    ) {
        val editBinding = EdittextLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        editBinding.root.tag = "birthWeight_${babyIndex}_root"
        editBinding.tvTitle.text = getString(R.string.birth_weight_in_kg)
        editBinding.etUserInput.tag = "birthWeight_$babyIndex"
        editBinding.etUserInput.hint = getString(R.string.enter_weight_in_kg)
        editBinding.etUserInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editBinding.etUserInput.filters = arrayOf(android.text.InputFilter.LengthFilter(6))

        (editBinding.etUserInput.background as? GradientDrawable)?.apply {
            setStroke(
                resources.getDimensionPixelSize(R.dimen._1sdp),
                requireContext().getColor(R.color.edittext_stroke),
            )
        }

        editBinding.etUserInput.addTextChangedListener { editable ->
            val value = editable?.toString()?.trim()?.toDoubleOrNull()
            if (value != null) {
                babyDataMap[babyIndex]?.set(AssessmentDefinedParams.BIRTH_WEIGHT, value)
            } else {
                babyDataMap[babyIndex]?.remove(AssessmentDefinedParams.BIRTH_WEIGHT)
            }
        }

        parent.addView(editBinding.root)
    }

    private fun addCauseOfDeathField(
        parent: LinearLayout,
        babyIndex: Int,
        translate: Boolean,
    ) {
        val checkboxBinding = CheckboxDialogSpinnerLayoutBinding.inflate(
            LayoutInflater.from(requireContext()),
        )
        checkboxBinding.root.tag = "${AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH}_${babyIndex}_root"
        checkboxBinding.tvTitle.text = getString(R.string.cause_of_neonatal_death)
        checkboxBinding.etUserInput.tag = "${AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH}_$babyIndex"
        checkboxBinding.etUserInput.text = getString(R.string.cause_of_death_hint)

        // Initially hidden
        checkboxBinding.root.visibility = View.GONE

        checkboxBinding.etUserInput.safeClickListener {
            openCauseOfDeathDialog(babyIndex, checkboxBinding.etUserInput)
        }

        parent.addView(checkboxBinding.root)
    }

    private fun openCauseOfDeathDialog(
        babyIndex: Int,
        textView: androidx.appcompat.widget.AppCompatTextView,
    ) {
        val dialogKey = "${AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH}_$babyIndex"
        val inputData = arrayListOf<SignsAndSymptomsEntity>()
        neonatalDeathCauseOptions.forEachIndexed { index, it ->
            val name = it[DefinedParams.NAME] as String
            val value = it[DefinedParams.ID] as String
            inputData.add(
                SignsAndSymptomsEntity(
                    _id = index.toLong(),
                    symptom = name,
                    type = dialogKey,
                    value = value,
                    displayValue = it[DefinedParams.cultureValue]?.toString(),
                ),
            )
        }

        val existingData = babyDataMap[babyIndex]?.get(AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH)

        CheckBoxDialog
            .newInstance(dialogKey, existingData, title = getString(R.string.cause_of_death_hint), inputData = inputData) { map ->
                if (map.isEmpty()) {
                    babyDataMap[babyIndex]?.remove(AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH)
                    textView.text = getString(R.string.cause_of_death_hint)
                } else {
                    babyDataMap[babyIndex]?.set(AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH, map)
                    val count = map.size
                    textView.text = "$count ${getString(R.string.selected)}"
                }
            }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun loadLocalCache(
        id: String,
        localDataCache: Any,
        selectedParent: Long?,
    ) {
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?,
    ) {
        // Use localDataCache if available, otherwise use id (for database loading)
        val dialogKey = formLayout.localDataCache ?: id

        if (formLayout.localDataCache != null) {
            // If localDataCache exists, load from database
            CheckBoxDialog
                .newInstance(dialogKey, resultMap, title = formLayout.hint) { map ->
                    formGenerator.validateCheckboxDialogue(id, formLayout, map)
                }.show(childFragmentManager, CheckBoxDialog.TAG)
        } else {
            // Create inputData from optionsList
            val inputData = arrayListOf<SignsAndSymptomsEntity>()
            formLayout.optionsList?.forEachIndexed { index, it ->
                val name = it[DefinedParams.NAME] as String
                val value = it[DefinedParams.ID] as String
                inputData.add(
                    SignsAndSymptomsEntity(
                        _id = index.toLong(),
                        symptom = name,
                        type = dialogKey,
                        value = value,
                        displayValue = it[DefinedParams.cultureValue]?.toString(),
                    ),
                )
            }
            CheckBoxDialog
                .newInstance(dialogKey, resultMap, title = formLayout.hint, inputData = inputData) { map ->
                    formGenerator.validateCheckboxDialogue(id, formLayout, map)
                }.show(childFragmentManager, CheckBoxDialog.TAG)
        }
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?,
    ) {
    }

    override fun onFormSubmit(
        resultMap: HashMap<String, Any>?,
        serverData: List<FormLayout>?,
    ) {
        resultMap?.let { details ->
            // Add baby data as an array under "newbornDetails" key
            if (currentBabyCount > 0) {
                val newbornDetailsList = ArrayList<HashMap<String, Any>>()
                for (babyIndex in 1..currentBabyCount) {
                    babyDataMap[babyIndex]?.let { babyData ->
                        if (babyData.isNotEmpty()) {
                            newbornDetailsList.add(HashMap(babyData))
                        }
                    }
                }
                if (newbornDetailsList.isNotEmpty()) {
                    details[AssessmentDefinedParams.NEWBORN_DETAILS] = newbornDetailsList
                }
            }
            val referralResult = ReferralResultGenerator().calculatePregnancyOutcomeStatus(details)
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    serverData = it,
                    details,
                    MenuConstants.PREGNANCY_OUTCOME.lowercase(),
                )
            }
            result?.second?.let {
                viewModel.saveAssessment(serverData, it, referralResult, viewModel.menuId)
            }
        }
        viewModel.setAnalyticsData(
            UserDetail.startDateTime,
            eventType = AnalyticsDefinedParams.PREGNANCY_OUTCOME,
            eventName = AnalyticsDefinedParams.AssessmentCreation,
        )
    }

    override fun onRenderingComplete() {
    }

    override fun onUpdateInstruction(
        id: String,
        selectedId: Any?,
    ) {
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?,
    ) {
    }

    override fun onAgeCheckForPregnancy() {
    }

    override fun handleMandatoryCondition(formLayout: FormLayout?) {
    }

    override fun onAgeUpdateListener(
        age: Int,
        serverData: List<FormLayout>?,
        resultHashMap: HashMap<String, Any>,
    ) {
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                withLocationCheck({
                    viewModel.setUserJourney(AnalyticsDefinedParams.SUBMITBUTTONTRIGGERED)
                    viewModel.fetchCurrentLocation(requireContext())
                    formGenerator.formSubmitAction(view)
                })
            }
        }
    }

    /**
     * Returns whether user has filled any details already or not.
     *
     * Based on this check, when user pressing back, we show alert for confirmation
     */
    fun getCurrentAnsweredStatus(): Boolean = formGenerator.getResultMap().isNotEmpty() || babyDataMap.any { it.value.isNotEmpty() }

    companion object {
        const val TAG = "AssessmentPregnancyOutcomeFragment"
    }
}
