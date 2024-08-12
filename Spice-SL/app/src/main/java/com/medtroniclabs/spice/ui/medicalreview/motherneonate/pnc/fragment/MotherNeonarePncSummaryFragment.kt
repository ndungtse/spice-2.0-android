package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentMotherNeonarePncSummaryBinding
import com.medtroniclabs.spice.databinding.FragmentMotherNeonateAncSummaryBinding
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.MotherNeonateAncSummary
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel.MotherNeonateSummaryViewModel


class MotherNeonarePncSummaryFragment : Fragment() {
    private lateinit var binding: FragmentMotherNeonarePncSummaryBinding
    var adapter: CustomSpinnerAdapter? = null
    private var datePickerDialog: DatePickerDialog? = null
//    val viewModel: MotherNeonateSummaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMotherNeonarePncSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "MotherNeonateSummary"
        fun newInstance(): MotherNeonarePncSummaryFragment {
            return MotherNeonarePncSummaryFragment()
        }

        fun newInstance(encounterId: String?): MotherNeonarePncSummaryFragment {
            val fragment = MotherNeonarePncSummaryFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.EncounterId, encounterId)
            fragment.arguments = bundle
            return fragment
        }
    }

}