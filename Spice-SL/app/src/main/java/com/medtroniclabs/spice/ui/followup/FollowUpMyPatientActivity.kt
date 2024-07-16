package com.medtroniclabs.spice.ui.followup

import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityFollowUpMyPatientBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.followup.adapter.FollowUpPatientListAdapter
import com.medtroniclabs.spice.ui.followup.viewmodel.FollowUpViewModel

class FollowUpMyPatientActivity : BaseActivity() {
    private lateinit var binding: ActivityFollowUpMyPatientBinding
    private val viewModel: FollowUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowUpMyPatientBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, isToolbarVisible = true, title = getString(R.string.my_patients)
        )
        initView()
        initObserver()
        setTabLayout()
    }


    private fun setTabLayout() {

        binding.viewPager.adapter = FollowUpPatientListAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.llExactSearch.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.hh_visit)
                1 -> tab.text = getString(R.string.referred)
                2 -> tab.text = getString(R.string.tab_medical_review)
            }
        }.attach()

        binding.llExactSearch.tabLayout.getTabAt(0)?.view?.setBackgroundResource(R.drawable.left_mh_view_selector)
        binding.llExactSearch.tabLayout.getTabAt(1)?.view?.setBackgroundResource(R.drawable.mental_health_button_bg)
        binding.llExactSearch.tabLayout.getTabAt(2)?.view?.setBackgroundResource(R.drawable.right_mh_view_selector)
        val tabView3 = (binding.llExactSearch.tabLayout.getChildAt(0) as? ViewGroup)?.getChildAt(2)
        tabView3?.setPadding(0, 0, 0, 0)

        binding.llExactSearch.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.createNewFollowUpFilter(it.position)
                    binding.llExactSearch.etSearchTerm.setText("")
                    binding.viewPager.currentItem = it.position

                    setTabTypeface(it, ResourcesCompat.getFont(applicationContext, R.font.inter_bold))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                setTabTypeface(tab, ResourcesCompat.getFont(applicationContext, R.font.inter_regular))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // No action needed
            }
        })

        binding.llExactSearch.tabLayout.getTabAt(0)?.let {
            setTabTypeface(it, ResourcesCompat.getFont(applicationContext, R.font.inter_bold))
        }
    }

    private fun initView() {
        with(binding) {
            val origin = intent.getStringExtra(MenuConstants.MY_PATIENTS_MENU_ID)
            llFilter.btnFilter.safeClickListener {
                FollowUpFilterBottomSheetDialogFragment.newInstance()
                    .show(supportFragmentManager, FollowUpFilterBottomSheetDialogFragment.TAG)
            }

            llExactSearch.etSearchTerm.addTextChangedListener {
                val search = it?.trim().toString()
                llExactSearch.btnSearch.isEnabled = !search.isNullOrEmpty()
                if (search.isNullOrEmpty()) {
                    viewModel.updateFollowUpFilter(search = "")
                }
            }

            llExactSearch.btnSearch.setOnClickListener {
                viewModel.updateFollowUpFilter(
                    search = llExactSearch.etSearchTerm.text?.trim().toString()
                )
            }
        }
    }

    private fun initObserver() {
        viewModel.followUpPatientListLiveData.observe(this) {
            binding.tvHPatientCount.text = getString(R.string.patient_count, it.size)
        }

        viewModel.getFilterDataLiveData().observe(this) {
            var count = 0
            if (!it.selectedVillages.isNullOrEmpty())
                count++
            if (!it.selectedDateRange.isNullOrEmpty())
                count++

            if (count > 0) {
                binding.llFilter.btnFilter.text = this.getString(R.string.filter_count, count)
            } else {
                binding.llFilter.btnFilter.text = getString(R.string.filter)
            }
        }
    }

    private fun setTabTypeface(tab: TabLayout.Tab?,  typeface: Typeface?) {
        val count = tab?.view?.childCount ?: 0
        for (i in 0 until count) {
            val tabViewChild = tab?.view?.getChildAt(i)
            if (tabViewChild != null && tabViewChild is TextView) {
                (tabViewChild as TextView).typeface = typeface
            }
        }
    }
}


