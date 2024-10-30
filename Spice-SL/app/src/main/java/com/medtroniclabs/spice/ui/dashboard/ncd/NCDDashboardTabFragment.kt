package com.medtroniclabs.spice.ui.dashboard.ncd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentNcdDashboardTabBinding
import com.medtroniclabs.spice.db.entity.MenuEntity

class NCDDashboardTabFragment : Fragment() {
    private lateinit var binding: FragmentNcdDashboardTabBinding
    private lateinit var adapterDashboard: NCDDashboardMenuItemsTabAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdDashboardTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun initView() {
        adapterDashboard = NCDDashboardMenuItemsTabAdapter()
        if (CommonUtils.checkIsTablet(requireContext())) {
            val layoutManager = GridLayoutManager(context, 3)
            binding.rvActivitiesList.layoutManager = layoutManager
        } else {
            val layoutManager = GridLayoutManager(context, 2)
            binding.rvActivitiesList.layoutManager = layoutManager
        }
        binding.rvActivitiesList.adapter = adapterDashboard
    }

    private fun attachObserver() {
        val data = ArrayList<MenuEntity>()
        adapterDashboard.updateData(ArrayList(data))
    }

    companion object {
        const val TAG = "NCDDashboardTabFragment"
        fun newInstance(): NCDDashboardTabFragment {
            return NCDDashboardTabFragment()
        }
    }
}