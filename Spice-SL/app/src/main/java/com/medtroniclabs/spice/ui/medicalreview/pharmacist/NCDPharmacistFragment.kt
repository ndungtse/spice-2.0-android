package com.medtroniclabs.spice.ui.medicalreview.pharmacist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.databinding.FragmentNcdPharmacistBinding
import com.medtroniclabs.spice.ui.BaseFragment

class NCDPharmacistFragment : BaseFragment() {

    private lateinit var binding: FragmentNcdPharmacistBinding
    private lateinit var prescriptionRefillAdapter: PrescriptionRefillAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdPharmacistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.rvPrescriptionRefillList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrescriptionRefillList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        //Here list contains the list of FillPrescriptionListResponse
        prescriptionRefillAdapter = PrescriptionRefillAdapter()
        binding.rvPrescriptionRefillList.adapter = prescriptionRefillAdapter
        prescriptionRefillAdapter.setData(list)
    }

    companion object {
        const val TAG = "NCDPharmacistFragment"
        fun newInstance(): NCDPharmacistFragment {
            return NCDPharmacistFragment()
        }
    }
}