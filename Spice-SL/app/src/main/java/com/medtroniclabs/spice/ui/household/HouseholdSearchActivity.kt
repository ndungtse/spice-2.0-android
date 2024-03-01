package com.medtroniclabs.spice.ui.household

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityHouseholdSearchBinding
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.ID
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.NoOfPeople
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.VillageId
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.isFromHouseHoldRegistration
import com.medtroniclabs.spice.ui.household.adapter.HouseholdListAdapter
import com.medtroniclabs.spice.ui.household.viewmodel.HouseholdListViewModel
import com.medtroniclabs.spice.ui.household.summary.HouseholdSummaryActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HouseholdSearchActivity : BaseActivity(), View.OnClickListener, HouseholdSelectionListener {

    private lateinit var binding: ActivityHouseholdSearchBinding
    private val householdListViewModel: HouseholdListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdSearchBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.households)
        )
        initViews()
        setListeners()
        attachObserver()
    }


    private fun initViews() {
        binding.llFilter.btnFilter.text = getString(R.string.filters)
        binding.llExactSearch.etSearchTerm.hint = getString(R.string.household_name_or_no)
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        if (tabletSize) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            householdListViewModel.spanCount = DefinedParams.span_count_3
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            householdListViewModel.spanCount = DefinedParams.span_count_1
        }
    }

    private fun setListeners() {
        binding.llExactSearch.btnSearch.safeClickListener(this)
        binding.llExactSearch.etSearchTerm.addTextChangedListener(searchListener)
        binding.btnAddHousehold.safeClickListener(this)
        binding.llFilter.btnFilter.safeClickListener(this)
    }

    private val searchListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            /**
             * this method is not used
             */
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            /**
             * this method is not used
             */
        }

        override fun afterTextChanged(s: Editable?) {
            val input = s?.trim().toString()
            binding.llExactSearch.btnSearch.isEnabled =
                input.isNotEmpty() && ((input[0].isLetter() && input.length >= 3) || input[0].isDigit())
        }
    }

    private fun attachObserver() {
        householdListViewModel.houseHoldListLiveData.observe(this) { resource ->
            when (resource.state) {

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        setHouseholdListAdapter(it, false)
                    } ?: kotlin.run {
                        setHouseholdListAdapter(ArrayList(), false)
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.LOADING -> {
                    showLoading()
                }
            }
        }
    }

    private fun updateFilterCount(isFromFilter: Boolean) {
        var count = 0
        householdListViewModel.villageFilterList?.let { if (it.isNotEmpty()) count++ }
        householdListViewModel.statusFilterList?.let { if (it.isNotEmpty()) count++ }
        if (isFromFilter && count!=0) {
            binding.llFilter.btnFilter.text = this.getString(R.string.filter_count, count)
        }
        else {
            binding.llFilter.btnFilter.text = getString(R.string.filters)
        }
    }

    private fun setHouseholdListAdapter(
        householdList: ArrayList<HouseHoldEntityWithMemberCount>,
        isFromFilter: Boolean
    ) {
        binding.tvHouseHoldCount.text = "${householdList.size} ${getString(R.string.households)}"
        updateFilterCount(isFromFilter)
        if (householdList.isNotEmpty()) {
            binding.llFilter.btnFilter.visibility = View.VISIBLE
            binding.tvNoHouseHoldFound.visibility = View.GONE
            binding.rvHouseholdList.visibility = View.VISIBLE
            val householdListAdapter = HouseholdListAdapter(this, householdList)
            binding.rvHouseholdList.apply {
                layoutManager =
                    GridLayoutManager(
                        this@HouseholdSearchActivity,
                        householdListViewModel.spanCount
                    )
                adapter = householdListAdapter
            }
        } else {
            if (!isFromFilter)
                binding.llFilter.btnFilter.visibility = View.GONE
            binding.tvNoHouseHoldFound.visibility = View.VISIBLE
            binding.rvHouseholdList.visibility = View.GONE
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnAddHousehold -> {
                startActivity(Intent(this, HouseholdActivity::class.java))
            }

            R.id.btnSearch -> {
                val searchTerm = binding.llExactSearch.etSearchTerm.text
                if (!searchTerm.isNullOrBlank()) {
                    householdListViewModel.searchByHouseholdNameOrNo(searchTerm.toString())
                }
            }

            R.id.btnFilter ->{
                FilterBottomSheetDialogFragment.newInstance(this).show(supportFragmentManager,FilterBottomSheetDialogFragment.TAG)
            }
        }
    }

    override fun onHouseHoldSelected(id: Long) {
        val intent = Intent(this@HouseholdSearchActivity, HouseholdSummaryActivity::class.java)
        intent.putExtra(ID, id)
        intent.putExtra(isFromHouseHoldRegistration, false)
        startActivity(intent)
    }

    override fun filterHouseholdList() {
        householdListViewModel.houseHoldListLiveData.value?.data?.let { householdList ->
            val villageIds = householdListViewModel.villageFilterList?.map { it.id }
            val patientStatus = householdListViewModel.statusFilterList?.map { it.name }?.firstOrNull()
            val householdFilteredList = householdList.filter { household ->
                (villageIds.isNullOrEmpty() || household.villageId in villageIds) &&
                        (patientStatus == null ||
                                (patientStatus == HouseholdDefinedParams.Pending && household.noOfPeople != household.registerMemberCount) ||
                                (patientStatus == HouseholdDefinedParams.Finished && household.noOfPeople == household.registerMemberCount))
            }
            setHouseholdListAdapter(ArrayList(householdFilteredList), true)
        }
    }

    override fun onResume() {
        super.onResume()
        householdListViewModel.getHouseHoldList()
    }

}