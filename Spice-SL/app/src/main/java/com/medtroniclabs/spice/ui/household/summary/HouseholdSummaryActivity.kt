package com.medtroniclabs.spice.ui.household.summary

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.isMemberRegistration
import com.medtroniclabs.spice.databinding.ActivityHouseholdSummaryBinding
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.formgeneration.extension.safePopupMenuClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.dialog.SuccessDialogFragment
import com.medtroniclabs.spice.ui.home.ToolsActivity
import com.medtroniclabs.spice.ui.household.HouseholdActivity
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.ID
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.isFromHouseHoldRegistration
import com.medtroniclabs.spice.ui.household.HouseholdSearchActivity
import com.medtroniclabs.spice.ui.household.MemberSelectionListener
import com.medtroniclabs.spice.ui.household.viewmodel.HouseHoldSummaryViewModel
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener

class HouseholdSummaryActivity : BaseActivity(), MemberSelectionListener, View.OnClickListener, OnDialogDismissListener {

    private lateinit var binding: ActivityHouseholdSummaryBinding

    private val householdSummaryViewModel: HouseHoldSummaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdSummaryBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, isToolbarVisible = true, title = getString(R.string.households)
        )
        initializeView()
        setListener()
        attachObserver()
    }

    private fun attachObserver() {
        householdSummaryViewModel.memberListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { data ->
                        initializeAdapter(data)
                    } ?: kotlin.run {
                        hideLoading()
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

        householdSummaryViewModel.houseHoldDetailLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.name?.let { name ->
                        setTitle(name)
                    }
                }
            }
        }
    }

    private fun initializeAdapter(data: ArrayList<HouseholdMemberEntity>) {
        val householdListAdapter = HouseholdMemberListAdapter(data, this)
        binding.rvHouseholdList.apply {
            layoutManager =
                GridLayoutManager(this@HouseholdSummaryActivity, DefinedParams.span_count_1)
            adapter = householdListAdapter
        }
    }

    private fun initializeView() {
        householdSummaryViewModel.houseHoldId = intent.getLongExtra(ID, -1)
        householdSummaryViewModel.isFromHouseHoldRegistration = intent.getBooleanExtra(
            isFromHouseHoldRegistration, false
        )
        supportFragmentManager.beginTransaction()
            .add(binding.fragmentContainer.id, HouseholdDetailsFragment()).commit()
        householdSummaryViewModel.getHouseHoldDetailsById()
        handleBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        householdSummaryViewModel.getAllHouseHoldMemberList()
    }


    private fun handleBottomNavigation() {
        if (householdSummaryViewModel.isFromHouseHoldRegistration) {
            binding.bottomNavigationView.visibility = View.VISIBLE
            showHideVerticalIcon(false)
        } else {
            binding.bottomNavigationView.visibility = View.GONE
            showHideVerticalIcon(true)
        }

    }

    private fun showHideVerticalIcon(visibility: Boolean) {
        showVerticalMoreIcon(visibility) {
            onMoreIconClicked(it)
        }
    }


    private fun onMoreIconClicked(view: View) {
        val popupMenu = PopupMenu(this@HouseholdSummaryActivity, view)
        popupMenu.menuInflater.inflate(R.menu.menu_house_hold_detail, popupMenu.menu)
        popupMenu.safePopupMenuClickListener(object :
            android.widget.PopupMenu.OnMenuItemClickListener,
            PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.addNewMember -> {
                        addNewMember()
                    }
                    R.id.editHousehold -> {
                        editHouseholdDetails()
                    }
                    R.id.editMember -> {
                        MemberEditDialogFragment.newInstance(this@HouseholdSummaryActivity)
                            .show(supportFragmentManager, MemberEditDialogFragment.TAG)
                    }
                }
                return true
            }
        })
        popupMenu.setForceShowIcon(true)
        popupMenu.show()
    }

    override fun onMemberSelected(item: Long, isEdit: Boolean) {
        if (isEdit){
            val intent = Intent(this, HouseholdActivity::class.java)
            intent.putExtra(DefinedParams.MemberID, item)
            startActivity(intent)
            this.finish()
        } else {
            val intent = Intent(this, ToolsActivity::class.java)
            intent.putExtra(DefinedParams.MemberID, item)
            startActivity(intent)
        }
    }

    private fun setListener() {
        binding.btnAddNewMember.setOnClickListener(this)
        binding.btnFinishRegistration.setOnClickListener(this)
        /*onBackPressedDispatcher.addCallback(this) {
            onHouseHoldSummaryActivity()
        }*/
    }

    private fun onHouseHoldSummaryActivity() {
        if (householdSummaryViewModel.isFromHouseHoldRegistration) {
            startAsNewActivity(
                Intent(
                    this@HouseholdSummaryActivity,
                    HouseholdSearchActivity::class.java
                )
            )
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnFinishRegistration -> {
                SuccessDialogFragment.newInstance().show(supportFragmentManager, SuccessDialogFragment.TAG)
            }

            R.id.btnAddNewMember -> {
                addNewMember()
            }
        }
    }

    private fun addNewMember() {
        if (householdSummaryViewModel.houseHoldId != -1L) {
            val intent =
                Intent(this@HouseholdSummaryActivity, HouseholdActivity::class.java)
            intent.putExtra(isMemberRegistration, true)
            intent.putExtra(ID, householdSummaryViewModel.houseHoldId)
            startActivity(intent)
        }
    }
    private fun editHouseholdDetails() {
        if (householdSummaryViewModel.houseHoldId != -1L) {
            val intent =
                Intent(this@HouseholdSummaryActivity, HouseholdActivity::class.java)
            intent.putExtra(isMemberRegistration, false)
            intent.putExtra(ID, householdSummaryViewModel.houseHoldId)
            startActivity(intent)
            finish()
        }
    }

    override fun onDialogDismissListener() {
        startActivity(Intent(this, HouseholdSearchActivity::class.java))
        finish()
    }

}