package com.medtroniclabs.spice.ui.followup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.databinding.FragmentFollowUpMyPatientListBinding
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.followup.adapter.PatientListAdapter
import com.medtroniclabs.spice.ui.followup.viewmodel.FollowUpViewModel

class FollowUpReferredFragment() : BaseFragment() {

    private lateinit var binding: FragmentFollowUpMyPatientListBinding
    private val viewModel: FollowUpViewModel by activityViewModels()
    private lateinit var adapter: PatientListAdapter

    companion object {
        const val TAG = "FragmentFollowUpPatientReferred"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowUpMyPatientListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        attachListener()
    }

    private fun attachListener() {
        viewModel.followUpPatientListLiveData.observe(viewLifecycleOwner) {
            binding.apply {
                tvPatientNoFound.visibility =
                    if (it?.isNotEmpty() == true) {
                        rvPatientList.visibility = View.VISIBLE
                        adapter.updateList(it)
                        View.GONE

                    } else {
                        rvPatientList.visibility = View.GONE
                        View.VISIBLE
                    }
            }
        }
    }

    private fun initAdapter() {
        adapter = PatientListAdapter {
            FollowUpDialogFragment.newInstance(it).show(
                parentFragmentManager, FollowUpDialogFragment.TAG
            )
        }
        binding.rvPatientList.adapter = adapter
    }
}