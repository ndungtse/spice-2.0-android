package com.medtroniclabs.spice.ui.household

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams.MemberID
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.isMemberRegistration
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.databinding.ActivityHouseholdRegistrationBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.household.fragment.HouseHoldRegistrationFragment
import com.medtroniclabs.spice.ui.household.summary.HouseholdSummaryActivity
import com.medtroniclabs.spice.ui.household.viewmodel.HouseRegistrationViewModel
import com.medtroniclabs.spice.ui.member.MemberRegistrationFragment
import com.medtroniclabs.spice.ui.member.MemberRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HouseholdActivity : BaseActivity() {

    private lateinit var binding: ActivityHouseholdRegistrationBinding

    private val householdRegistrationViewModel: HouseRegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdRegistrationBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.household_registration)
        )
        initializeView()
        attachObserver()
    }

    private fun initializeView() {
        householdRegistrationViewModel.isMemberRegistration =
            intent.getBooleanExtra(isMemberRegistration, false)
        householdRegistrationViewModel.householdId =
            intent.getLongExtra(HouseholdDefinedParams.ID, -1L)
        householdRegistrationViewModel.memberID = intent.getLongExtra(MemberID, -1L)
        loadFragment(if (householdRegistrationViewModel.isMemberRegistration || (householdRegistrationViewModel.memberID != -1L)) 2 else 1)
    }


    fun loadFragment(status: Int) {
        when (status) {
            1 -> {
                setTitle(getString(R.string.household_registration))
                replaceFragmentInId<HouseHoldRegistrationFragment>(
                    binding.fragmentContainer.id,
                    tag = HouseHoldRegistrationFragment::class.simpleName
                )
            }

            2 -> {
                setTitle(getString(R.string.member_registration))
                replaceFragmentInId<MemberRegistrationFragment>(
                    binding.fragmentContainer.id,
                    tag = MemberRegistrationFragment::class.simpleName
                )
            }

            3 -> {
                val intent = Intent(this, HouseholdSummaryActivity::class.java)
                intent.putExtra(
                    HouseholdDefinedParams.ID,
                    householdRegistrationViewModel.householdId
                )
                intent.putExtra(HouseholdDefinedParams.isFromHouseHoldRegistration, false)
                startActivity(intent)
                finish()
            }
        }
    }


    private inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(
                id ?: binding.fragmentContainer.id,
                args = bundle,
                tag = tag
            )
        }
    }


    private fun attachObserver() {
        householdRegistrationViewModel.houseHoldRegistrationLiveData.observe(this@HouseholdActivity) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    if (householdRegistrationViewModel.householdId != -1L)
                        loadFragment(3)
                    else
                        loadFragment(2)
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        householdRegistrationViewModel.houseHoldUpdateLiveData.observe(this@HouseholdActivity) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadFragment(3)
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

}