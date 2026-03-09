package com.medtroniclabs.spice.ui.services

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.hideKeyboard
import com.medtroniclabs.spice.appextensions.setTextChangeListener
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberWithTb
import com.medtroniclabs.spice.databinding.ActivityServicesBinding
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.model.services.ServiceStaticFilter
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.AssessmentToolsActivity
import com.medtroniclabs.spice.ui.household.MemberSelectionListener
import com.medtroniclabs.spice.ui.services.viewmodel.ServicesViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.medtroniclabs.spice.common.DefinedParams as CommonDefinedParams

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.service_recipient_list),
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
        binding.llExactSearch.etSearchTerm.hint = getString(R.string.household_name_or_no)
        adapter = ServiceMembersAdapter(this)

        binding.rvMembersList.apply {
            adapter = this@ServicesActivity.adapter
        }
        setDropdown()
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
        }
    }

    override fun onMemberSelected(
        item: Long,
        isEdit: Boolean,
        dateOfBirth: String?,
        isContactTrace: Boolean,
        houseHoldId: Long?,
    ) {
        val intent = Intent(this, AssessmentToolsActivity::class.java)
        intent.putExtra(CommonDefinedParams.HouseholdId, houseHoldId)
        intent.putExtra(CommonDefinedParams.MemberID, item)
        intent.putExtra(CommonDefinedParams.DOB, dateOfBirth)
        startActivity(intent)
    }
}
