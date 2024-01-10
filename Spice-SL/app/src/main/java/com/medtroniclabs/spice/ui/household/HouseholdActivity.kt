package com.medtroniclabs.spice.ui.household

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityHouseholdRegistrationBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.household.fragment.HouseHoldRegistrationFragment
import com.medtroniclabs.spice.ui.household.viewmodel.HouseRegistrationViewModel
import com.medtroniclabs.spice.ui.member.MemberRegistrationFragment
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
        loadFragment(if (householdRegistrationViewModel.houseHoldRegistration) 1 else 2)
        attachObserver()
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
                    loadFragment(2)
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

}