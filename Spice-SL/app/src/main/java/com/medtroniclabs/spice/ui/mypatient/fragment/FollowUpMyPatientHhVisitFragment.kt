package com.medtroniclabs.spice.ui.mypatient.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.databinding.FragmentFollowUpMyPatientListBinding
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatient.FollowUpCommunicator
import com.medtroniclabs.spice.ui.mypatient.adapter.PatientListAdapter
import com.medtroniclabs.spice.ui.mypatient.viewmodel.FollowUpViewModel

class FollowUpMyPatientHhVisitFragment : BaseFragment(), FollowUpCommunicator {

    private lateinit var binding: FragmentFollowUpMyPatientListBinding
    private val viewModel: FollowUpViewModel by activityViewModels()

    companion object {
        const val TAG = "FragmentFollowUpMyPatientList"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowUpMyPatientListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListener()
    }

    private fun attachListener() {
        viewModel.followUpPatientList.observe(viewLifecycleOwner, ::handleFollowUpPatientList)
    }

    private fun handleFollowUpPatientList(resource: Resource<List<FollowUpPatientModel>>) {
        when (resource.state) {
            ResourceState.LOADING -> {
//
            }

            ResourceState.SUCCESS -> {
//                hideLoading()
                binding.apply {
                    tvPatientNoFound.visibility =
                        if (resource.data?.isNotEmpty() == true) {
                            rvPatientList.visibility = View.VISIBLE
                            viewModel.setPatientDateList.postValue(resource.data)
                            setFollowUpListToAdapter(resource.data)
                            View.GONE

                        } else {
                            rvPatientList.visibility = View.GONE
                            View.VISIBLE
                        }
                }
            }

            ResourceState.ERROR -> {
//

            }
        }
    }

    private fun setFollowUpListToAdapter(followUpPatientModels: List<FollowUpPatientModel>) {
        with(binding) {
            rvPatientList.apply {
                adapter =
                    PatientListAdapter(
                        ArrayList(followUpPatientModels),
                        viewModel.isAssessmentType,
                        this@FollowUpMyPatientHhVisitFragment
                    )
                layoutManager = GridLayoutManager(
                    context,
                    viewModel.spanCount
                )
            }
        }
    }

    override fun showAssessmentCallDialog() {
        FollowUpDialogFragment.newInstance(viewModel.isAssessmentType).show(
            parentFragmentManager, FollowUpDialogFragment.TAG
        )
    }

}