package com.medtroniclabs.spice.ui.phuwalkins.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.FragmentPhuWalkInsListBinding
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.phuwalkins.adapter.PhuLinkListAdapter
import com.medtroniclabs.spice.ui.phuwalkins.listener.PhuLinkCallback
import com.medtroniclabs.spice.ui.phuwalkins.viewmodel.PhuWalkInsViewModel

class PhuWalkInsListFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentPhuWalkInsListBinding
    private var dataCallback: PhuLinkCallback? = null
    private val viewModel: PhuWalkInsViewModel by viewModels()
    fun setDataCallback(callback: PhuLinkCallback) {
        dataCallback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhuWalkInsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "PhuWalkInsListFragment"
        fun newInstance(): PhuWalkInsListFragment {
            return PhuWalkInsListFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set layout manager and adapter
        initObserver()
    }

    override fun onClick(v: View?) {
    }

    private fun initObserver() {
        viewModel.unAssignedMembers.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.tvPatientNoFound.visible()
                binding.rcLinkPatientList.gone()
                binding.tvHPatientCount.gone()
            } else {
                binding.tvPatientNoFound.gone()
                binding.rcLinkPatientList.visible()
                binding.tvHPatientCount.visible()
            }

            val phoneNumberCode = SecuredPreference.getPhoneNumberCode()
            binding.tvHPatientCount.text = it.size.toString().plus(getString(R.string._patients))
            binding.rcLinkPatientList.layoutManager = LinearLayoutManager(requireContext())
            binding.rcLinkPatientList.adapter = PhuLinkListAdapter(phoneNumberCode, it, activity as PhuLinkCallback)
        }
    }

}