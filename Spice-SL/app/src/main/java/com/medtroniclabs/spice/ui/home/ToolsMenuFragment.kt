package com.medtroniclabs.spice.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentToolsMenuBinding
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.home.adapter.DashboardMenuItemsAdapter

class ToolsMenuFragment : Fragment(), MenuSelectionListener {

    private lateinit var binding: FragmentToolsMenuBinding
    private val viewModel: ToolsViewModel by viewModels()

    companion object {
        const val TAG = "ToolsMenuFragment"
        fun newInstance(): ToolsMenuFragment {
            return ToolsMenuFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentToolsMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapterViews()
    }

    private fun setAdapterViews() {
        if (CommonUtils.checkIsTablet(requireContext())) {
            val layoutManager = FlexboxLayoutManager(context)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            binding.rvActivitiesList.layoutManager = layoutManager
        } else {
            val layoutManager = GridLayoutManager(context, 2)
            binding.rvActivitiesList.layoutManager = layoutManager
        }
        binding.rvActivitiesList.adapter = DashboardMenuItemsAdapter(viewModel.getMenuItemsList(requireContext()), this)
    }

    override fun onMenuSelected(name: String) {
        when(name) {
            MenuConstants.ICCM_MENU_ID -> {
                //startActivity
            } MenuConstants.SCREENER_MENU_ID -> {
                //startActivity
            } MenuConstants.CBS_MENU_ID -> {
                //startActivity
            } MenuConstants.TB_MENU_ID -> {
                //startActivity
            } MenuConstants.NCD_MENU_ID -> {
                //startActivity
            }
        }
    }

}