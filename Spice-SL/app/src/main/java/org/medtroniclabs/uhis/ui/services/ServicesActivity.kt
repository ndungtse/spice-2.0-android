package org.medtroniclabs.uhis.ui.services

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.hideKeyboard
import org.medtroniclabs.uhis.appextensions.setTextChangeListener
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.offlinesync.model.HouseholdMemberWithTb
import org.medtroniclabs.uhis.databinding.ActivityServicesBinding
import org.medtroniclabs.uhis.formgeneration.config.DefinedParams
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.formgeneration.utility.CustomSpinnerAdapter
import org.medtroniclabs.uhis.model.services.ServiceStaticFilter
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.externalmember.ExternalMemberRegistrationActivity
import org.medtroniclabs.uhis.ui.household.MemberSelectionListener
import org.medtroniclabs.uhis.ui.household.summary.MemberSummaryActivity
import org.medtroniclabs.uhis.ui.services.viewmodel.ServicesViewModel
import org.medtroniclabs.uhis.common.DefinedParams as CommonDefinedParams

/**
 * Activity responsible to display members based on services
 */
@AndroidEntryPoint
class ServicesActivity : BaseActivity(), View.OnClickListener, MemberSelectionListener {
    private lateinit var binding: ActivityServicesBinding

    private val servicesViewModel: ServicesViewModel by viewModels()

    private lateinit var adapter: ServiceMembersAdapter

    /**
     * A boolean flag for ignoring first selection from adapter.
     */
    private var isFirstSelection = true

    /**
     * Flag to indicate if this is external member mode
     */
    private var isExternalMember = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)

        // Check if this is external member mode
        isExternalMember = intent.getBooleanExtra("isExternalMember", false)

        val title = if (isExternalMember) {
            getString(R.string.external_member)
        } else {
            getString(R.string.service_recipient_list)
        }

        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = title,
        )
        initViews()
        setListeners()
        attachObserver()
    }

    override fun onResume() {
        super.onResume()
        servicesViewModel.setUserJourney(AnalyticsDefinedParams.SERVICES)
    }

    private fun initViews() {
        binding.llFilter.btnFilter.text = getString(R.string.filter)

        // Update search hint for external members
        val searchHint = if (isExternalMember) {
            getString(R.string.member_name_or_phone)
        } else {
            getString(R.string.household_name_or_no)
        }
        binding.llExactSearch.etSearchTerm.hint = searchHint

        adapter = ServiceMembersAdapter(this)

        binding.rvMembersList.apply {
            adapter = this@ServicesActivity.adapter
        }

        // Show/hide button and dropdown for external members
        if (isExternalMember) {
            binding.tvMemberTypes.gone()
            binding.viewMemberTypes.gone()
            binding.bottomNavigationView.visible()
            binding.btnAddExternalMember.safeClickListener(this)
            // Set external member filter directly
            servicesViewModel.setFilterLiveData(staticFilter = ServiceStaticFilter.EXTERNAL_MEMBERS)
        } else {
            binding.bottomNavigationView.gone()
            setDropdown()
        }
        showLoading()
    }

    private fun setDropdown() {
        (binding.tvMemberTypes.background as? GradientDrawable)?.apply {
            setStroke(resources.getDimensionPixelSize(R.dimen._1sdp), getColor(R.color.edittext_stroke))
        }
        val dropDownList = buildDropDownList()
        val adapter = CustomSpinnerAdapter(this, SecuredPreference.getIsTranslationEnabled())

        adapter.setData(dropDownList)
        binding.tvMemberTypes.adapter = adapter
        binding.tvMemberTypes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                itemView: View?,
                position: Int,
                itemId: Long,
            ) {
                // If first selection then ignore as it is triggering the data fetch 2 times
                // once on first selection and once when viewmodel loads
                if (!isFirstSelection) {
                    val item = adapter.getData(position)
                    val id = item?.get(DefinedParams.ID) as? ServiceStaticFilter
                    if (id != null) {
                        servicesViewModel.setFilterLiveData(staticFilter = id)
                    }
                }
                isFirstSelection = false
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do Nothing
            }
        }
    }

    private fun buildDropDownList(): ArrayList<Map<String, Any>> {
        val dropdownList = arrayListOf<Map<String, Any>>()
        val staticFilters = listOf(
            ServiceStaticFilter.ALL_MEMBERS,
            ServiceStaticFilter.FAMILY_PLANNING_COUNSELLING_ELIGIBLE,
            ServiceStaticFilter.PREGNANT_WOMEN,
            ServiceStaticFilter.HIGH_RISK_PREGNANT_WOMEN,
            ServiceStaticFilter.POSTNATAL_CARE_MOTHERS,
            ServiceStaticFilter.CHILDREN_UNDER_TWO_YEARS,
            ServiceStaticFilter.EXPECTED_DELIVERIES,
            ServiceStaticFilter.PENDING_DELIVERIES,
        )
        staticFilters.forEach {
            dropdownList.add(
                mapOf(
                    DefinedParams.cultureValue to it.culturalValue,
                    DefinedParams.NAME to it.value,
                    DefinedParams.ID to it,
                ),
            )
        }
        return dropdownList
    }

    private fun setListeners() {
        binding.llExactSearch.btnSearch.safeClickListener(this)
        binding.llFilter.btnFilter.safeClickListener(this)
        binding.llExactSearch.etSearchTerm.setTextChangeListener {
            val input = it?.trim().toString()
            binding.llExactSearch.btnSearch.isEnabled =
                input.isNotEmpty() &&
                ((input[0].isLetter() && input.length >= 3) || input[0].isDigit())

            if (input.isEmpty()) {
                servicesViewModel.setFilterLiveData(search = "")
            }
        }
    }

    private fun attachObserver() {
        servicesViewModel.getFilterLiveData().observe(this) {
            var count = 0
            if (it.filterBySs.isNotEmpty()) {
                count++
            }
            if (it.filterBySubVillages.isNotEmpty()) {
                count++
            }

            if (count > 0) {
                binding.llFilter.btnFilter.text = this.getString(R.string.filter_count, count)
            } else {
                binding.llFilter.btnFilter.text = getString(R.string.filter)
            }
        }
        servicesViewModel.filteredMembersLiveData.observe(this) {
            hideLoading()
            setMembers(it)
        }
    }

    private fun setMembers(membersList: List<HouseholdMemberWithTb>) {
        val size = membersList.size
        binding.tvMembersCount.text = resources.getQuantityString(R.plurals.plural_member, size, size)
        if (membersList.isNotEmpty()) {
            binding.llFilter.btnFilter.visible()
            binding.tvNoMembersFound.gone()
            binding.rvMembersList.visible()
            adapter.setMembersList(membersList)
        } else {
            binding.tvNoMembersFound.visible()
            binding.rvMembersList.gone()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnFilter -> {
                hideKeyboard(view)
                withLocationCheck({
                    FilterBottomSheetDialogFragment
                        .newInstance()
                        .show(supportFragmentManager, FilterBottomSheetDialogFragment.TAG)
                })
            }

            R.id.btnSearch -> {
                withLocationCheck({
                    servicesViewModel.setUserJourney(AnalyticsDefinedParams.SERVICES_SEARCH_TRIGGERED)
                    val searchTerm = binding.llExactSearch.etSearchTerm.text
                        .toString()
                    servicesViewModel.setFilterLiveData(search = searchTerm)
                })
            }

            R.id.btnAddExternalMember -> {
                withLocationCheck({
                    servicesViewModel.setUserJourney("ADD_EXTERNAL_MEMBER_BUTTON_TRIGGERED")
                    val intent = Intent(this, ExternalMemberRegistrationActivity::class.java)
                    startActivity(intent)
                })
            }
        }
    }

    override fun onMemberSelected(
        item: Long,
        isEdit: Boolean,
        dateOfBirth: String?,
        isContactTrace: Boolean,
        houseHoldId: Long?,
    ) {
        val intent = Intent(this, MemberSummaryActivity::class.java)
        intent.putExtra(CommonDefinedParams.HOUSEHOLD_ID, houseHoldId)
        intent.putExtra(CommonDefinedParams.MEMBER_ID, item)
        intent.putExtra(CommonDefinedParams.DOB, dateOfBirth)
        startActivity(intent)
    }
}
