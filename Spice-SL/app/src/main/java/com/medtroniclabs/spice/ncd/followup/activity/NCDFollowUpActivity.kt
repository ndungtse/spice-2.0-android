package com.medtroniclabs.spice.ncd.followup.activity

import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.ActivityNcdFollowUpBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.Assessment
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.Assessment_Type
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.Defaulters
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.Defaulters_Type
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.LTFU
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.LTFU_Type
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.Referred
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.Referred_Type
import com.medtroniclabs.spice.ncd.followup.adapter.NCDFollowUpAdapter
import com.medtroniclabs.spice.ncd.followup.fragment.NCDFollowUpFilterDialog
import com.medtroniclabs.spice.ncd.followup.viewmodel.NCDFollowUpViewModel
import com.medtroniclabs.spice.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDFollowUpActivity : BaseActivity() {
    private lateinit var binding: ActivityNcdFollowUpBinding
    private val viewModel: NCDFollowUpViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = if (CommonUtils.checkIsTablet(this)) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        binding = ActivityNcdFollowUpBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            title = getString(R.string.search_patient),
            isToolbarVisible = true,
            homeAndBackVisibility = Pair(true, true)
        )
        initView()
        setTabLayout()
        attachObservers()
    }

    fun attachObservers() {
        viewModel.totalPatientCount.observe(this) { count ->
            if (count > 0) {
                binding.tvHPatientCount.apply {
                    text = if (count == 1) getString(R.string.patient_found) else getString(
                        R.string.patients_found,
                        count
                    )
                    visibility = View.VISIBLE
                }
                binding.tvPatientNoFound.gone()
                binding.llFilter.btnFilter.visible()
            } else {
                binding.llFilter.btnFilter.gone()
                binding.tvHPatientCount.text = getString(R.string.no_patients_found)
                binding.tvPatientNoFound.visible()
            }
        }
        viewModel.filterCount.observe(this) { count ->
            if (count > 0) {
                binding.llFilter.btnFilter.text = this.getString(R.string.filter_count, count)
            } else {
                binding.llFilter.btnFilter.text = getString(R.string.filter)
            }
        }
    }

    private fun setTabLayout() {
        binding.llExactSearch.etSearchTerm.hint = getString(R.string.search_by_national_id)
        binding.viewPager.adapter = NCDFollowUpAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.llExactSearch.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = Referred
                1 -> tab.text = Assessment
                2 -> tab.text = Defaulters
                3 -> tab.text = LTFU
            }
        }.attach()

        binding.llExactSearch.tabLayout.getTabAt(0)?.view?.setBackgroundResource(R.drawable.left_mh_view_selector)
        binding.llExactSearch.tabLayout.getTabAt(1)?.view?.setBackgroundResource(R.drawable.mental_health_button_bg)
        binding.llExactSearch.tabLayout.getTabAt(2)?.view?.setBackgroundResource(R.drawable.mental_health_button_bg)
        binding.llExactSearch.tabLayout.getTabAt(3)?.view?.setBackgroundResource(R.drawable.right_mh_view_selector)
        val tabView3 = (binding.llExactSearch.tabLayout.getChildAt(0) as? ViewGroup)?.getChildAt(2)
        tabView3?.setPadding(0, 0, 0, 0)

        binding.llExactSearch.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    binding.llExactSearch.etSearchTerm.setText("")
                    viewModel.searchText = ""
                    viewModel.customDate = null
                    viewModel.dateRange = null
                    viewModel.remainingAttempts = listOf()
                    viewModel.filterCount.postValue(0)
                    viewModel.type = when (it.position) {
                        0 -> Referred_Type
                        1 -> Assessment_Type
                        2 -> Defaulters_Type
                        3 -> LTFU_Type
                        else -> ""
                    }
                    binding.viewPager.currentItem = it.position

                    setTabTypeface(
                        it,
                        ResourcesCompat.getFont(applicationContext, R.font.inter_bold)
                    )
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                setTabTypeface(
                    tab,
                    ResourcesCompat.getFont(applicationContext, R.font.inter_regular)
                )
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
            binding.llFilter.btnFilter.text = getString(R.string.filter)
            binding.tvPatientNoFound.visible()
            binding.llFilter.btnFilter.visible()
            binding.llFilter.btnSort.gone()
            llFilter.btnFilter.safeClickListener {
                NCDFollowUpFilterDialog.newInstance()
                    .show(supportFragmentManager, NCDFollowUpFilterDialog.TAG)
            }

            llExactSearch.etSearchTerm.addTextChangedListener {
                val search = it?.trim().toString()
                llExactSearch.btnSearch.isEnabled = !search.isNullOrEmpty()
                if (search.isNullOrEmpty()) {
                    viewModel.searchLiveData(text = "")
                }
            }
            llExactSearch.btnSearch.safeClickListener {
                viewModel.searchLiveData(text = llExactSearch.etSearchTerm.text?.trim().toString())
            }
        }
    }

    private fun setTabTypeface(tab: TabLayout.Tab?, typeface: Typeface?) {
        val count = tab?.view?.childCount ?: 0
        for (i in 0 until count) {
            val tabViewChild = tab?.view?.getChildAt(i)
            if (tabViewChild != null && tabViewChild is TextView) {
                tabViewChild.typeface = typeface
            }
        }
    }
}