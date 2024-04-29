package com.medtroniclabs.spice.ui.mypatient

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.databinding.ActivityFollowUpMyPatientBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.household.FilterBottomSheetDialogFragment
import com.medtroniclabs.spice.ui.mypatient.adapter.FollowUpPatientListAdapter
import com.medtroniclabs.spice.ui.mypatient.adapter.PatientListAdapter
import com.medtroniclabs.spice.ui.mypatient.fragment.FollowUpDialogFragment
import com.medtroniclabs.spice.ui.mypatient.viewmodel.FollowUpViewModel

class FollowUpMyPatientActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityFollowUpMyPatientBinding
    private val viewModel: FollowUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowUpMyPatientBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, isToolbarVisible = true, title = getString(R.string.households)
        )
        initView()
        setPatientCount()
        setTabLayout()
    }


    private fun setTabLayout() {

        binding.viewPager.adapter = FollowUpPatientListAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.llExactSearch.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "HH Visit"
                1 -> tab.text = "Referred"
                2 -> tab.text = "Medical Review"
            }
        }.attach()

        binding.llExactSearch.tabLayout.getTabAt(0)?.view?.setBackgroundResource(R.drawable.left_mh_view_selector)
        binding.llExactSearch.tabLayout.getTabAt(1)?.view?.setBackgroundResource(R.drawable.mental_health_button_bg)
        binding.llExactSearch.tabLayout.getTabAt(2)?.view?.setBackgroundResource(R.drawable.right_mh_view_selector)
        binding.llExactSearch.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    binding.viewPager.currentItem = it.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // No action needed
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // No action needed
            }
        })
    }


    private fun setPatientCount() {
        viewModel.setPatientDateList.observe(this) {
            binding.tvHPatientCount.text = getString(R.string.patient_count, it.size)
        }
    }


    private fun initView() {
        with(binding) {
            val origin = intent.getStringExtra(MenuConstants.MY_PATIENTS_MENU_ID)
            llFilter.btnFilter.safeClickListener {
                FilterBottomSheetDialogFragment.newInstance(origin)
                    .show(supportFragmentManager, FilterBottomSheetDialogFragment.TAG)
            }
            with(llExactSearch) {
                etSearchTerm.addTextChangedListener(searchListener)
                viewModel.getFollowUpPatientList(DefinedParams.HH_VISIT)
                btnSearch.safeClickListener(this@FollowUpMyPatientActivity)
            }
        }
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

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.btnSearch -> {
                viewModel.getFollowUpPatientList(
                    searchKey = binding.llExactSearch.etSearchTerm.text.toString().trim()
                )
            }
        }
    }

}


