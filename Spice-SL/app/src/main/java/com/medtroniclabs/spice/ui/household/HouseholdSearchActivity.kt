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
        binding.llFilter.btnFilter.text = getString(R.string.filter)
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
            binding.llExactSearch.btnSearch.isEnabled = (s?.trim()?.count() ?: 0) > 0
        }
    }

    private fun attachObserver() {
        householdListViewModel.houseHoldListLiveData.observe(this) { resource ->
            when (resource.state) {

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        setHouseholdListAdapter(it)
                    } ?: kotlin.run {
                        setHouseholdListAdapter(ArrayList())
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

    private fun setHouseholdListAdapter(householdList: ArrayList<HouseHoldEntityWithMemberCount>) {
        binding.tvHouseHoldCount.text = "${householdList.size} ${getString(R.string.households)}"
        if (householdList.isNotEmpty()) {
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
        }
    }

    override fun onHouseHoldSelected(id: Long) {
        val intent = Intent(this@HouseholdSearchActivity, HouseholdSummaryActivity::class.java)
        intent.putExtra(DefinedParams.houseHoldID, id)
        intent.putExtra(DefinedParams.isFromHouseHoldRegistration,false)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        householdListViewModel.getHouseHoldList()
    }

}