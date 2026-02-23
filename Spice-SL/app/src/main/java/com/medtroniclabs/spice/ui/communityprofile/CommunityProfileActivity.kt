package com.medtroniclabs.spice.ui.communityprofile

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityCommunityProfileBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.communityprofile.fragments.CommunityProfileSearchFragment
import com.medtroniclabs.spice.ui.communityprofile.fragments.CommunityProfileSummaryFragment
import com.medtroniclabs.spice.ui.communityprofile.fragments.EditCommunityProfileFragment
import com.medtroniclabs.spice.ui.communityprofile.viewmodel.CommunityProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityCommunityProfileBinding
    private val communityViewModel: CommunityProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityCommunityProfileBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.community_profile),
            callback = {
                backNavigation()
            },
            homeIcon = getDrawable(R.drawable.ic_edit_white),
            homeAndBackVisibility = Pair(true, true),
            callbackMore = {
                navigateToEditScreen()
            },
        )
        initView()
        addObservers()
    }

    private fun initView() {
        communityViewModel.getFormData(DefinedParams.COMMUNITY_PROFILE)
        communityViewModel.updateCurrentFragment(1)
    }

    private fun addObservers() {
        communityViewModel.currentFragment.observe(this) {
            loadFragment(it.first, it.second)
        }
    }

    private fun loadFragment(
        status: Int,
        bundle: Bundle? = null,
    ) {
        when (status) {
            1 -> {
                replaceFragmentInId<CommunityProfileSearchFragment>(
                    binding.communityProfileContainer.id,
                    tag = CommunityProfileSearchFragment::class.simpleName,
                )
            }

            2 -> {
                replaceFragmentInId<EditCommunityProfileFragment>(
                    binding.communityProfileContainer.id,
                    tag = EditCommunityProfileFragment::class.simpleName,
                    bundle = bundle,
                )
            }

            3 -> {
                replaceFragmentInId<CommunityProfileSummaryFragment>(
                    binding.communityProfileContainer.id,
                    tag = CommunityProfileSummaryFragment::class.simpleName,
                    bundle = bundle,
                )
            }
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation()
            }
        }

    fun backNavigation() {
        val status = getBackFragmentStatus()
        if (status.first) {
            showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.exit_reason),
                isNegativeButtonNeed = true,
            ) { isPositive ->
                if (isPositive) {
                    backNavigationFlow(status.second)
                }
            }
        } else {
            backNavigationFlow(status.second)
        }
    }

    private fun getBackFragmentStatus(): Pair<Boolean, Boolean> {
        val fragment = supportFragmentManager.findFragmentById(R.id.communityProfileContainer)
        if (fragment is EditCommunityProfileFragment) {
            return Pair(fragment.getCurrentAnswerStatus(), false)
        } else if (fragment is CommunityProfileSearchFragment) {
            return Pair(false, true)
        }
        return Pair(false, false)
    }

    private fun backNavigationFlow(isHome: Boolean) {
        if (!isHome) {
            communityViewModel.updateCurrentFragment(1)
        } else {
            finish()
        }
    }

    private fun navigateToEditScreen() {
        val fragment = supportFragmentManager.findFragmentById(R.id.communityProfileContainer)
        if (fragment is CommunityProfileSummaryFragment) {
            fragment.navigateToEditScreen()
        }
    }
}
