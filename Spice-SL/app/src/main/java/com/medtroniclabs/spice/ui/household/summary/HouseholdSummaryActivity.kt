package com.medtroniclabs.spice.ui.household.summary

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityHouseholdSummaryBinding
import com.medtroniclabs.spice.db.entity.HouseholdSummaryModel
import com.medtroniclabs.spice.ui.BaseActivity

class HouseholdSummaryActivity : BaseActivity() {

    private lateinit var binding: ActivityHouseholdSummaryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdSummaryBinding.inflate(layoutInflater)
        setMainContentView(binding.root,isToolbarVisible = true, title = getString(R.string.households))
        initializeView()
        initializeAdapter()
    }

    private fun initializeAdapter() {
        val householdeMemberList = ArrayList<HouseholdSummaryModel>()
        var age = 20
        for (i in 1..20) {
            householdeMemberList.add(
                HouseholdSummaryModel(
                    id = i.toLong(),
                    name = "Hari's Household ${i}",
                    age = age + i,
                    gender = DefinedParams.MALE
                )
            )
        }
        val householdListAdapter = HouseholdSummaryListAdapter( householdeMemberList)
        binding.rvHouseholdList.apply {
            layoutManager =
                GridLayoutManager(this@HouseholdSummaryActivity, DefinedParams.span_count_1)
            adapter = householdListAdapter
        }
    }

    private fun initializeView() {
        supportFragmentManager.beginTransaction()
            .add(binding.householdDetailsFragment.id, HouseholdDetailsFragment())
            .commit()
    }
}