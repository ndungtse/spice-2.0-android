package com.medtroniclabs.spice.ui.assessment.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.CardLayoutBinding
import com.medtroniclabs.spice.databinding.FragmentAssessmentPregnancyOutcomeSummaryBinding
import com.medtroniclabs.spice.databinding.InstructionLayoutBinding
import com.medtroniclabs.spice.formgeneration.FormSupport.translateTitle
import com.medtroniclabs.spice.formgeneration.utility.InformationLayoutFragment
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment responsible for showing pregnancy outcome assessment summary details
 */
@AndroidEntryPoint
class AssessmentPregnancyOutcomeSummaryFragment : BaseFragment(), View.OnClickListener {
    lateinit var binding: FragmentAssessmentPregnancyOutcomeSummaryBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    private var currentMapData: Map<*, *>? = null

    /**
     * Fields that need to appear in summary even though isSummary is false
     * (they are also shown in the form page, so isSummary can't be true).
     * Hidden automatically when they have no value.
     */
    private val additionalSummaryFieldIds = setOf(
        AssessmentDefinedParams.TIME_OF_DEATH,
        AssessmentDefinedParams.GESTATION_MONTH_AT_DEATH,
        AssessmentDefinedParams.CAUSE_OF_DEATH,
        AssessmentDefinedParams.GESTATION_MONTH_AT_ABORTION,
        AssessmentDefinedParams.TYPE_OF_ABORTION,
        AssessmentDefinedParams.DELIVERY_OUTCOME,
        AssessmentDefinedParams.PLACE_OF_DELIVERY,
        AssessmentDefinedParams.DATE_OF_DELIVERY,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssessmentPregnancyOutcomeSummaryBinding.inflate(
            inflater,
            container,
            false,
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        attachObservers()
        viewModel.setUserJourney(AnalyticsDefinedParams.PREGNANCY_OUTCOME_SUMMARY)
    }

    private fun setListeners() {
        binding.btnDone.safeClickListener(this)
    }

    private fun attachObservers() {
        updateStatusBar()
        // Load pregnancy details to get EDD for preterm calculation
        viewModel.getPregnancyDetailInformation()
        viewModel.pregnancyDetailLiveData.observe(viewLifecycleOwner) {
            // Re-render summary when pregnancy details are loaded to show preterm status
            viewModel.assessmentStringLiveData.value?.let { data ->
                renderSummaryData(data)
            }
        }
        viewModel.assessmentStringLiveData.value?.let {
            renderSummaryData(it)
        }
    }

    private fun updateStatusBar() {
        // Status bar removed for pregnancy outcome - hide it
        binding.riskResultLayout.visibility = View.GONE
    }

    /**
     * Renders summary data
     */
    private fun renderSummaryData(data: String) {
        val convertedMap = StringConverter.stringToMap(data)
        if (convertedMap.isEmpty()) {
            showErrorInSummary()
            return
        }
        binding.emptyErrorMessage.visibility = View.GONE
        binding.parentLayout.visibility = View.VISIBLE
        binding.parentLayout.removeAllViews()

        val mapData = convertedMap[MenuConstants.PREGNANCY_OUTCOME] as? Map<*, *>
        if (mapData == null) {
            showErrorInSummary()
            return
        }

        // Store mapData for use in counselling card
        currentMapData = mapData

        // Create summary data list from form layout
        val listSummaryData = createListSummaryData(data)
        createSummaryView(listSummaryData, mapData)

        // Add counselling card
        addCounsellingCard()
    }

    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? =
        viewModel.formLayoutsLiveData.value
            ?.data
            ?.formLayout
            ?.filter {
                it.isSummary == true || it.id in additionalSummaryFieldIds
            }
            ?.mapNotNull { formLayout ->
                val dataMap = data.replaceFirst(
                    MenuConstants.PREGNANCY_OUTCOME,
                    MenuConstants.PREGNANCY_OUTCOME.lowercase()
                )

                val value = AssessmentCommonUtils.getValueOfKeyFromMap(
                    StringConverter.stringToMap(dataMap),
                    formLayout.id,
                    menuType = MenuConstants.PREGNANCY_OUTCOME,
                )

                if (!value.isNullOrBlank()) {
                    AssessmentSummaryModel(
                        title = formLayout.titleSummary ?: formLayout.title,
                        id = formLayout.id,
                        cultureValue = formLayout.titleCulture,
                        value = value,
                    )
                } else {
                    null
                }
            }
            ?.toMutableList()

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?,
        mapData: Map<*, *>,
    ) {
        listSummaryData?.let { summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            composeSummaryView(summaryData)

            // Add newborn details sections
            addNewbornDetailsSummary(mapData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun composeSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
        listSummaryData.forEach { item ->
            val isOldCounselling = item.id == AssessmentDefinedParams.COUNSELLING_ABORTION ||
                item.id == AssessmentDefinedParams.COUNSELLING_STILL_BIRTH ||
                item.id == AssessmentDefinedParams.COUNSELLING_NEONATAL_DEATH
            val isNewCounselling = item.id == AssessmentDefinedParams.COUNSELLING_EMOTIONAL_SUPPORT ||
                item.id == AssessmentDefinedParams.COUNSELLING_FUTURE_PREGNANCY_PLANNING
            val isAdditionalField = item.id in additionalSummaryFieldIds

            when {
                isOldCounselling || isNewCounselling -> {
                    // Skip counselling fields - they will be shown in separate card
                }
                isAdditionalField -> {
                    // Additional fields: hide if no value
                    val value = item.value
                    if (!value.isNullOrBlank()) {
                        val displayValue = if (item.id == AssessmentDefinedParams.DATE_OF_DELIVERY) {
                            val formattedDate = DateUtils.convertDateFormat(
                                value,
                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                DateUtils.DATE_ddMMyyyy,
                            )
                            // Check if preterm birth and add highlighting
                            checkAndDisplayPretermBirth(formattedDate, value)
                        } else {
                            // Convert ID to display name for SingleSelectionView/Spinner fields
                            getSpinnerDisplayValue(item.id.toString(), value, isTranslationEnabled) ?: value
                        }
                        if (item.id != AssessmentDefinedParams.DATE_OF_DELIVERY) {
                            bindSummaryView(item.title, displayValue.toString())
                        }
                    }
                }
                else -> {
                    // Other isSummary fields: show normally
                    // Convert ID to display name for SingleSelectionView/Spinner fields
                    val displayValue = getSpinnerDisplayValue(item.id.toString(), item.value, isTranslationEnabled) ?: item.value
                    bindSummaryView(item.title, displayValue)
                }
            }
        }
    }

    /**
     * Converts spinner/SingleSelectionView field ID to display name using optionsList from formLayout
     */
    private fun getSpinnerDisplayValue(
        fieldId: String,
        valueId: String?,
        isTranslationEnabled: Boolean,
    ): String? {
        if (valueId.isNullOrBlank()) return null

        val formLayout = viewModel.formLayoutsLiveData.value?.data?.formLayout?.find {
            it.id == fieldId
        } ?: return null

        val optionsList = formLayout.optionsList ?: return null
        optionsList.forEach { option ->
            val optionId = option[com.medtroniclabs.spice.common.DefinedParams.ID] as? String
                ?: option[com.medtroniclabs.spice.common.DefinedParams.id] as? String
            if (optionId == valueId) {
                // Return cultureValue if translation is enabled, otherwise return name
                return if (isTranslationEnabled) {
                    option[com.medtroniclabs.spice.common.DefinedParams.cultureValue] as? String
                        ?: option[com.medtroniclabs.spice.common.DefinedParams.NAME] as? String
                } else {
                    option[com.medtroniclabs.spice.common.DefinedParams.NAME] as? String
                }
            }
        }

        return null
    }

    /**
     * Adds newborn details summary for each baby
     */
    private fun addNewbornDetailsSummary(mapData: Map<*, *>) {
        // newbornDetails could be at the top level of the pregnancy outcome map
        // or nested inside a sub-map
        val newbornDetailsList = findNewbornDetails(mapData) ?: return

        if (newbornDetailsList.isEmpty()) return

        newbornDetailsList.forEachIndexed { index, babyData ->
            if (babyData is Map<*, *>) {
                // Add baby heading
                addBabyHeading(index + 1)

                // Is baby alive
                val isBabyAlive = babyData[AssessmentDefinedParams.IS_BABY_ALIVE]
                if (isBabyAlive != null) {
                    val displayValue = when (isBabyAlive) {
                        is Boolean -> if (isBabyAlive) getString(R.string.yes) else getString(R.string.no)
                        is String -> isBabyAlive
                        else -> isBabyAlive.toString()
                    }
                    bindSummaryView(getString(R.string.is_baby_alive), displayValue)
                }

                // Sex
                val sex = babyData[AssessmentDefinedParams.SEX]
                if (sex != null) {
                    bindSummaryView(getString(R.string.sex_label), sex.toString())
                }

                // Birth weight - Removed (no longer displayed)

                // Cause of neonatal death
                val causeOfDeath = babyData[AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH]
                if (causeOfDeath != null) {
                    val displayValue = formatCauseOfDeath(causeOfDeath)
                    if (displayValue.isNotBlank()) {
                        bindSummaryView(getString(R.string.cause_of_neonatal_death), displayValue)
                    }
                }
            }
        }
    }

    private fun findNewbornDetails(mapData: Map<*, *>): List<*>? {
        // Check direct key
        val directList = mapData[AssessmentDefinedParams.NEWBORN_DETAILS]
        if (directList is List<*>) return directList

        // Check in nested maps (family groups)
        for (entry in mapData.entries) {
            if (entry.value is Map<*, *>) {
                val nestedMap = entry.value as Map<*, *>
                val nestedList = nestedMap[AssessmentDefinedParams.NEWBORN_DETAILS]
                if (nestedList is List<*>) return nestedList
            }
        }
        return null
    }

    private fun formatCauseOfDeath(value: Any?): String {
        val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
        if (value is List<*>) {
            val names = value.mapNotNull { item ->
                when (item) {
                    is Map<*, *> -> {
                        // Use cultureValue if translation is enabled, otherwise use name
                        if (isTranslationEnabled) {
                            item[com.medtroniclabs.spice.common.DefinedParams.cultureValue]?.toString()
                                ?: item[com.medtroniclabs.spice.common.DefinedParams.NAME]?.toString()
                                ?: item["symptom"]?.toString()
                        } else {
                            item[com.medtroniclabs.spice.common.DefinedParams.NAME]?.toString()
                                ?: item["symptom"]?.toString()
                        }
                    }
                    is String -> item
                    else -> null
                }
            }
            return names.joinToString(", ")
        }
        return value?.toString() ?: ""
    }

    /**
     * Checks if delivery is preterm (<37 weeks from EDD) and displays with highlighting
     */
    private fun checkAndDisplayPretermBirth(
        formattedDate: String,
        dateOfDelivery: String,
    ) {
        val pregnancyDetail = viewModel.pregnancyDetailLiveData.value
        val edd = pregnancyDetail?.estimatedDeliveryDate

        // Build value with preterm status if applicable
        val displayValue = if (!edd.isNullOrBlank() && isPretermDelivery(dateOfDelivery, edd)) {
            // Format: "[date]  (Preterm Birth)" where "(Preterm Birth)" is in RED
            buildSpannedString {
                append(formattedDate)
                append("  ")
                color(Color.RED) {
                    append("(${getString(R.string.preterm_birth)})")
                }
            }
        } else {
            formattedDate
        }

        // Display date of delivery with preterm status in value
        bindSummaryView(getString(R.string.date_of_delivery), displayValue)
    }

    /**
     * Checks if delivery is preterm (<37 weeks from EDD)
     */
    private fun isPretermDelivery(
        dateOfDelivery: String,
        edd: String?,
    ): Boolean {
        if (edd.isNullOrBlank()) {
            return false // Can't determine preterm without EDD
        }

        try {
            // Calculate gestational age at delivery
            val gestationalWeeks = calculateGestationalAgeAtDelivery(dateOfDelivery, edd)

            // Preterm is < 37 weeks
            return gestationalWeeks != null && gestationalWeeks < 37
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Calculates gestational age in weeks at delivery based on EDD
     * Returns weeks or null if calculation fails
     */
    private fun calculateGestationalAgeAtDelivery(
        dateOfDelivery: String,
        edd: String,
    ): Int? {
        try {
            // Parse dates - both are in yyyy-MM-dd'T'HH:mm:ssZZZZZ format
            // Convert to yyyyMMdd format for calculation
            val deliveryDateStr = DateUtils.convertDateFormat(
                dateOfDelivery,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DateUtils.DATE_FORMAT_yyyyMMdd,
            )
            val eddDateStr = DateUtils.convertDateFormat(
                edd,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DateUtils.DATE_FORMAT_yyyyMMdd,
            )

            if (deliveryDateStr.isNullOrBlank() || eddDateStr.isNullOrBlank()) {
                return null
            }

            // Get time in milliseconds
            val deliveryMillis = DateUtils.getCalendarFromString(
                deliveryDateStr,
                DateUtils.DATE_FORMAT_yyyyMMdd,
            )
            val eddMillis = DateUtils.getCalendarFromString(
                eddDateStr,
                DateUtils.DATE_FORMAT_yyyyMMdd,
            )

            if (deliveryMillis == null || eddMillis == null) {
                return null
            }

            // Calculate difference in days
            val diffInMillis = eddMillis - deliveryMillis
            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

            // Calculate gestational age at delivery
            // EDD is typically at 40 weeks from LMP
            // If delivery is before EDD, gestational age = 40 - (days before EDD / 7)
            // If delivery is after EDD, gestational age = 40 + (days after EDD / 7)
            val weeksFromEDD = (diffInDays / 7.0).toInt()
            val gestationalWeeks = 40 - weeksFromEDD

            return gestationalWeeks
        } catch (e: Exception) {
            return null
        }
    }

    private fun addBabyHeading(babyNumber: Int) {
        val headingView = TextView(requireContext()).apply {
            text = getString(R.string.baby_label, babyNumber)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.navy_blue))
            setPadding(
                resources.getDimensionPixelSize(R.dimen._16sdp),
                resources.getDimensionPixelSize(R.dimen._12sdp),
                resources.getDimensionPixelSize(R.dimen._16sdp),
                resources.getDimensionPixelSize(R.dimen._4sdp),
            )
        }
        binding.parentLayout.addView(headingView)
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    private fun bindSummaryView(
        title: String?,
        value: CharSequence?,
        valueTextColor: Int? = null,
    ) {
        value?.let { result ->
            // If value is a SpannableString/Spanned, don't pass valueTextColor to avoid overriding colors
            val summaryLayout = if (result is android.text.SpannableString || result is android.text.Spanned) {
                AssessmentCommonUtils.addViewSummaryLayout(
                    title,
                    result.toString(),
                    null, // Don't set valueTextColor for SpannableString
                    requireContext(),
                ).apply {
                    // Set the SpannableString directly to preserve formatting
                    val summaryBinding = com.medtroniclabs.spice.databinding.AssessmentSummaryLayoutBinding.bind(this)
                    summaryBinding.tvValue.text = result
                }
            } else {
                AssessmentCommonUtils.addViewSummaryLayout(
                    title,
                    result.toString(),
                    valueTextColor,
                    requireContext(),
                )
            }

            binding.parentLayout.addView(summaryLayout)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnDone.id -> {
                withLocationCheck({
                    viewModel.fetchCurrentLocation(requireContext())
                    viewModel.updatePregnantWomanAssessmentDetails()
                    viewModel.setUserJourney(AnalyticsDefinedParams.DONEBUTTONTRIGGERED)
                })
            }
        }
    }

    companion object {
        const val TAG = "AssessmentPregnancyOutcomeSummaryFragment"
    }

    fun getCurrentAnsweredStatus(): Boolean = viewModel.otherAssessmentDetails.isNotEmpty()

    /**
     * Checks if counselling should be shown based on pregnancy outcome conditions
     * Returns true if abortion, stillbirth, or baby death occurred
     */
    private fun shouldShowCounselling(mapData: Map<*, *>?): Boolean {
        if (mapData == null) return false

        // Check for abortion
        val abortionMap = mapData["abortion"] as? Map<*, *>
        abortionMap?.let { abortion ->
            val gestationMonth = abortion[AssessmentDefinedParams.GESTATION_MONTH_AT_ABORTION]
            val typeOfAbortion = abortion[AssessmentDefinedParams.TYPE_OF_ABORTION]
            if ((gestationMonth != null && gestationMonth.toString().isNotBlank()) ||
                (typeOfAbortion != null && typeOfAbortion.toString().isNotBlank())
            ) {
                return true
            }
        }

        // Check for stillbirth
        val deliveryOutcomes = mapData["deliveryOutcomes"] as? Map<*, *>
        deliveryOutcomes?.let { delivery ->
            val stillbirthNumbers = delivery[AssessmentDefinedParams.STILLBIRTH_NUMBERS]
            val stillbirthCount = when (stillbirthNumbers) {
                is Number -> stillbirthNumbers.toInt()
                is String -> stillbirthNumbers.toIntOrNull() ?: 0
                else -> 0
            }
            if (stillbirthCount > 0) {
                return true
            }
        }

        // Check for baby death
        val newbornDetailsList = findNewbornDetails(mapData)
        newbornDetailsList?.forEach { babyData ->
            if (babyData is Map<*, *>) {
                val isBabyAlive = babyData[AssessmentDefinedParams.IS_BABY_ALIVE]
                val isDead = when (isBabyAlive) {
                    is String -> isBabyAlive.equals(AssessmentDefinedParams.NO, ignoreCase = true)
                    is Boolean -> !isBabyAlive
                    else -> isBabyAlive?.toString()?.equals(AssessmentDefinedParams.NO, ignoreCase = true) == true
                }
                if (isDead) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Adds counselling card below the result card
     */
    private fun addCounsellingCard() {
        // Check if counselling should be shown
        if (!shouldShowCounselling(currentMapData)) {
            return
        }

        val formLayouts = viewModel.formLayoutsLiveData.value?.data?.formLayout ?: return
        val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()

        // Get counselling card layout for title
        val counsellingCardLayout = formLayouts.firstOrNull {
            it.id == AssessmentDefinedParams.COUNSELLING_ADVERSE_EVENT
        }

        // Get only the new counselling items (filter out old ones)
        val counsellingItems = formLayouts.filter {
            it.family == AssessmentDefinedParams.COUNSELLING_ADVERSE_EVENT &&
            it.viewType == "Instruction" &&
            it.isSummary == true &&
            (it.id == AssessmentDefinedParams.COUNSELLING_EMOTIONAL_SUPPORT ||
             it.id == AssessmentDefinedParams.COUNSELLING_FUTURE_PREGNANCY_PLANNING)
        }.sortedBy { it.orderId ?: Int.MAX_VALUE }

        // If no counselling items found, don't show card
        if (counsellingItems.isEmpty()) {
            return
        }

        // Create Counselling CardView using CardLayoutBinding
        val counsellingCardBinding = CardLayoutBinding.inflate(LayoutInflater.from(requireContext()))

        // Set card title
        counsellingCardLayout?.let { cardLayout ->
            counsellingCardBinding.cardTitle.text = translateTitle(
                cardLayout.titleCulture,
                cardLayout.title,
                isTranslationEnabled
            )
        } ?: run {
            // Fallback title if cardLayout not found
            counsellingCardBinding.cardTitle.text = getString(R.string.counseling_education)
        }

        // Add counselling items to the card's content layout
        counsellingItems.forEach { formLayout ->
            addCounsellingInstructions(formLayout, counsellingCardBinding.llFamilyRoot, isTranslationEnabled)
        }

        // Find the parent ConstraintLayout inside scrollView to add the card
        val scrollViewContent = binding.scrollView.getChildAt(0) as? androidx.constraintlayout.widget.ConstraintLayout
        scrollViewContent?.let { content ->
            // Set layout parameters with constraints
            val layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // Set margins to match resultCardView
                marginStart = resources.getDimensionPixelSize(R.dimen._6sdp)
                marginEnd = resources.getDimensionPixelSize(R.dimen._6sdp)
                topMargin = resources.getDimensionPixelSize(R.dimen._6sdp)
                bottomMargin = resources.getDimensionPixelSize(R.dimen._6sdp)

                // Set constraints: below resultCardView, start/end to parent
                startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                topToBottom = binding.resultCardView.id
            }

            counsellingCardBinding.root.layoutParams = layoutParams

            // Add counselling card to the ConstraintLayout
            content.addView(counsellingCardBinding.root)
        }
    }

    /**
     * Adds counselling instruction row with clickable title that opens dialog
     */
    private fun addCounsellingInstructions(
        formLayout: com.medtroniclabs.spice.formgeneration.model.FormLayout,
        parentLayout: LinearLayout,
        isTranslationEnabled: Boolean,
    ) {
        with(formLayout) {
            val instructionBinding = InstructionLayoutBinding.inflate(LayoutInflater.from(requireContext()))
            instructionBinding.tvTitle.text = translateTitle(titleCulture, title, isTranslationEnabled)
            instructionBinding.tvTitle.visibility = View.VISIBLE
            instructionBinding.tvTitle.setTextColor(Color.BLACK)

            instructionBinding.clInstructionRoot.safeClickListener {
                // Use instructionsCulture if translation is enabled and available, otherwise use instructions
                val instructionsList = if (isTranslationEnabled && !instructionsCulture.isNullOrEmpty()) {
                    instructionsCulture
                } else {
                    instructions
                }
                if (!instructionsList.isNullOrEmpty()) {
                    InformationLayoutFragment
                        .newInstance(id, translateTitle(titleCulture, title, isTranslationEnabled), customInformationList = instructionsList)
                        .show(childFragmentManager, InformationLayoutFragment.TAG)
                    viewModel.setUserJourney("$id ${AnalyticsDefinedParams.INFORMATIONDIALOUGE}")
                }
            }
            // Add to counselling card's content layout
            parentLayout.addView(instructionBinding.root)
        }
    }
}
