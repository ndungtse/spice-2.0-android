package com.medtroniclabs.spice.ui.medicalreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentPresentingComplaintsBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CHIP_ITEMS
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PC_ITEM
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel.MotherNeonateANCViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PresentingComplaintsFragment : BaseFragment() {

    private lateinit var binding: FragmentPresentingComplaintsBinding
    private lateinit var complaintsTagView: TagListCustomView
    private val viewModel : PresentingComplaintsViewModel by activityViewModels()

    companion object{
        const val TAG = "PresentingComplaintsFragment"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.presentingComplaintsType =
                it.getString(MedicalReviewTypeEnums.PresentingComplaints.name) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPresentingComplaintsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attachObserver()
        setListener()
    }

    private fun setListener() {
        binding.etPresentingComplaintsComments.addTextChangedListener {
            it?.let {
                viewModel.enteredComplaintNotes = it.trim().toString()
                setFragmentResult(PC_ITEM, bundleOf(CHIP_ITEMS to true))
            }
        }
    }

    private fun attachObserver() {
        viewModel.presentingComplaintsList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        val chipItemList =
                            listItems.filter { it.category == MedicalReviewTypeEnums.PresentingComplaints.name }
                                .map {
                                    ChipViewItemModel(
                                        id = it.id,
                                        name = it.name,
                                        value = it.value
                                    )
                                }
                        complaintsTagView.addChipItemList(chipItemList, viewModel.selectedPresentingComplaints)
                    }
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    fun validate(): Boolean {
        if (binding.etPresentingComplaintsComments.text?.isNotEmpty() == true) {
            val complaint = binding.etPresentingComplaintsComments.text?.trim().toString()
            if (complaint.isBlank()) {
                binding.tvErrorMessage.text = getString(R.string.default_user_input_error)
                binding.tvErrorMessage.visible()
                return false
            } else {
                binding.tvErrorMessage.gone()
            }
            return true
        }
        return true
    }

    private fun initializeViews() {
        complaintsTagView = TagListCustomView(
            binding.root.context,
            binding.tagViewPresentingComplaints
        ) { _, _, _ ->
            viewModel.selectedPresentingComplaints = ArrayList(complaintsTagView.getSelectedTags())
            setFragmentResult(PC_ITEM, bundleOf(CHIP_ITEMS to true))
        }
        viewModel.getPresentingComplaintsList(viewModel.presentingComplaintsType)
        if (viewModel.enteredComplaintNotes.isNotBlank()) {
            binding.etPresentingComplaintsComments.setText(viewModel.enteredComplaintNotes)
        }
    }

}