package org.medtroniclabs.uhis.ui.communityprofile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.setTextChangeListener
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.data.community.CommunityProfileDetail
import org.medtroniclabs.uhis.databinding.FragmentCommunityProfileSearchBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.communityprofile.adapter.CommunityListAdapter
import org.medtroniclabs.uhis.ui.communityprofile.viewmodel.CommunityProfileViewModel

class CommunityProfileSearchFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentCommunityProfileSearchBinding
    private val communityProfileViewModel: CommunityProfileViewModel by activityViewModels()
    private lateinit var communityListAdapter: CommunityListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCommunityProfileSearchBinding.inflate(
            inflater,
            container,
            false,
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        addObserver()
        setListener()
    }

    override fun onResume() {
        super.onResume()
        communityProfileViewModel.setUserJourney(AnalyticsDefinedParams.COMMUNITYPROFILELISTSCREEN)
    }

    private fun setListener() {
        binding.llExactSearch.btnSearch.safeClickListener(this)
        binding.llExactSearch.etSearchTerm.setTextChangeListener {
            val input = it?.trim().toString()
            binding.llExactSearch.btnSearch.isEnabled =
                input.isNotEmpty() &&
                input.length >= 3

            if (input.isEmpty()) {
                communityProfileViewModel.setSearchFilter("")
            }
        }
    }

    private fun initViews() {
        hideHomeIcon()
        communityProfileViewModel.setSearchFilter("")
        communityProfileViewModel.reinitSaveLiveData()
        binding.llExactSearch.etSearchTerm.setHint(R.string.community_name)
        binding.llExactSearch.etSearchTerm.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_blue_search,
            ), // Left drawable
            null, // Top drawable
            null, // Right drawable
            null, // Bottom drawable
        )
        communityListAdapter = CommunityListAdapter { selectedCommunity ->
            val isExist = selectedCommunity.isCommunityProfileDetailAvailable?.let { it > 0 } ?: false
            loadFormOrDetailFragment(selectedCommunity, isExist)
        }
        binding.rvCommunities.adapter = communityListAdapter
        binding.llExactSearch.etSearchTerm.setText(communityProfileViewModel.getSearchFilter())
    }

    private fun addObserver() {
        communityProfileViewModel.searchFilterLiveData.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvNoCommunityFound.visibility = View.VISIBLE
            } else {
                binding.tvNoCommunityFound.visibility = View.GONE
            }
            binding.tvCommunityCount.text = setLabel(it.size)
            communityListAdapter.updateList(it)
        }
    }

    private fun loadFormOrDetailFragment(
        community: CommunityProfileDetail,
        isExist: Boolean,
    ) {
        val bundle = Bundle().apply {
            putLong(DefinedParams.COMMUNITY_ID, community.villageId)
            putString(DefinedParams.COMMUNITY_NAME, community.villageName)
            putBoolean(DefinedParams.COMMUNITY_REGISTERED, isExist)
        }

        if (isExist) {
            communityProfileViewModel.updateCurrentFragment(3, bundle)
        } else {
            communityProfileViewModel.updateCurrentFragment(2, bundle)
        }
    }

    private fun setLabel(size: Int): String {
        return if (size > 1) {
            getString(R.string.no_of_communities, size)
        } else {
            return getString(R.string.no_of_community, size)
        }
    }

    companion object {
        const val TAG = "CommunityProfileSearchFragment"
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSearch -> {
                val searchTerm = binding.llExactSearch.etSearchTerm.text
                    .toString()
                communityProfileViewModel.setSearchFilter(searchTerm)
            }
        }
    }
}
