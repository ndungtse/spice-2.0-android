package org.medtroniclabs.uhis.ui.phuwalkins.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.databinding.FragmentPhuWalkInsListBinding
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.phuwalkins.adapter.PhuLinkListAdapter
import org.medtroniclabs.uhis.ui.phuwalkins.listener.PhuLinkCallback
import org.medtroniclabs.uhis.ui.phuwalkins.viewmodel.PhuWalkInsViewModel

class PhuWalkInsListFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentPhuWalkInsListBinding
    private var dataCallback: PhuLinkCallback? = null
    private val viewModel: PhuWalkInsViewModel by viewModels()

    fun setDataCallback(callback: PhuLinkCallback) {
        dataCallback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPhuWalkInsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "PhuWalkInsListFragment"

        fun newInstance(): PhuWalkInsListFragment = PhuWalkInsListFragment()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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
