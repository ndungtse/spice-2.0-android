package com.medtroniclabs.spice.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentHomeScreenBinding
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.home.adapter.DashboardMenuItemsAdapter
import com.medtroniclabs.spice.ui.household.HouseholdSearchActivity
import dagger.hilt.android.AndroidEntryPoint
import com.medtroniclabs.spice.ui.medicalreview.MedicalReviewBaseActivity


@AndroidEntryPoint
class HomeScreenFragment : Fragment(), MenuSelectionListener {

    private lateinit var binding: FragmentHomeScreenBinding

    companion object {
        const val TAG = "HomeScreenFragment"
        fun newInstance(): HomeScreenFragment {
            return HomeScreenFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapterViews()
    }

    private fun setAdapterViews() {
        val menuList = ArrayList<MenuEntity>()
        menuList.add(
            MenuEntity(
                id = 1,
                name = MenuConstants.HOUSEHOLD_MENU_ID,
                role = getString(R.string.chw),
                menuId = MenuConstants.HOUSEHOLD_MENU_ID,
                displayOrder = 1
            )
        )
        menuList.add(
            MenuEntity(
                id = 12,
                name = MenuConstants.MY_PATIENTS_MENU_ID,
                role = getString(R.string.chw),
                menuId = MenuConstants.MY_PATIENTS_MENU_ID,
                displayOrder = 2
            )
        )
        if (CommonUtils.checkIsTablet(requireContext())) {
            val layoutManager = FlexboxLayoutManager(context)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            binding.rvActivitiesList.layoutManager = layoutManager
        } else {
            val layoutManager = GridLayoutManager(context, 2)
            binding.rvActivitiesList.layoutManager = layoutManager
        }

        binding.rvActivitiesList.adapter = DashboardMenuItemsAdapter(menuList, this)

    }

    override fun onMenuSelected(name: String) {
        when(name) {
            MenuConstants.HOUSEHOLD_MENU_ID -> {
                startActivity(Intent(requireContext(), HouseholdSearchActivity::class.java))
            }
            MenuConstants.MY_PATIENTS_MENU_ID -> {
                startActivity(Intent(requireContext(), MedicalReviewBaseActivity::class.java))
            }
        }
    }
}