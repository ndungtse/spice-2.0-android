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
import com.medtroniclabs.spice.databinding.ActivityHouseholdSearchBinding
import com.medtroniclabs.spice.db.entity.HouseholdListModel
import com.medtroniclabs.spice.formgenerator.definedproperties.DefinedParams
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.household.adapter.HouseholdListAdapter
import com.medtroniclabs.spice.ui.household.search.viewmodel.HouseholdListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HouseholdSearchActivity : BaseActivity(), View.OnClickListener, HouseholdSelectionListener {

    private lateinit var binding: ActivityHouseholdSearchBinding
    private lateinit var householdListAdapter: HouseholdListAdapter
    private val householdListViewModel: HouseholdListViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setListeners()
        setHouseholdListAdapter()
    }

    private fun initViews() {
        binding.llFilter.btnFilter.text = getString(R.string.filter)
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
        binding.llExactSearch.etPatientSearch.addTextChangedListener(searchListener)
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

    private fun setHouseholdListAdapter() {
        val householdList = ArrayList<HouseholdListModel>()
        for (i in 1..20) {
            householdList.add(
                HouseholdListModel(
                    id = i.toLong(),
                    householdName = "${getString(R.string.households)} ${i}",
                    noOfPeople = i,
                    noOfPeopleRegistered = i,
                    householdNo = i.toLong()
                )
            )
        }
        binding.tvPatientCount.visibility =
            if (householdList.isNotEmpty()) View.VISIBLE else View.GONE
        binding.tvPatientCount.text = "${householdList.size} ${getString(R.string.households)}"
        householdListAdapter = HouseholdListAdapter(this, householdList)
        binding.rvHouseholdList.apply {
            layoutManager =
                GridLayoutManager(this@HouseholdSearchActivity, householdListViewModel.spanCount)
            adapter = householdListAdapter
        }
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.btnAddHousehold -> {
                startActivity(Intent(this, HouseholdRegistrationActivity::class.java))
            }
        }
    }

    override fun onSelectedPatient(item: HouseholdListModel) {
    }
}