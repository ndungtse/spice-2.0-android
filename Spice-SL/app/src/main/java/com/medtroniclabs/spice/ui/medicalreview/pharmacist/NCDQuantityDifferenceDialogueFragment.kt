package com.medtroniclabs.spice.ui.medicalreview.pharmacist

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentNcdQuantityDifferenceDialogueBinding
import com.medtroniclabs.spice.databinding.LayoutQuantityDifferenceBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter

class NCDQuantityDifferenceDialogueFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentNcdQuantityDifferenceDialogueBinding

    companion object {
        const val TAG = "NCDQuantityDifferenceDialogueFragment"
        fun newInstance(): NCDQuantityDifferenceDialogueFragment {
            return NCDQuantityDifferenceDialogueFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdQuantityDifferenceDialogueBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    private fun handleDialogSize() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = if (CommonUtils.checkIsTablet(requireContext())) {
            if (isLandscape) 90 else 90
        } else {
            if (isLandscape) 50 else 60
        }
        setWidth(width)
    }

    private fun initView() {
        binding.ivClose.safeClickListener(this)
        binding.bottomView.btnDone.safeClickListener(this)
        binding.bottomView.btnCancel.safeClickListener(this)
        loadMedicationDifferenceDate()
    }

    private fun loadMedicationDifferenceDate() {
        binding.rvDifferenceList.removeAllViews()
        val lifeStyleBinding = LayoutQuantityDifferenceBinding.inflate(layoutInflater)
        lifeStyleBinding.tvMedicationName.text = getString(R.string.separator_hyphen)
        lifeStyleBinding.tvPrescribedDays.text = getString(R.string.separator_hyphen)
        lifeStyleBinding.tvfilledDays.text = getString(R.string.separator_hyphen)
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(getDropDownList())
        lifeStyleBinding.etReasonSpinner.adapter = adapter
        binding.rvDifferenceList.addView(lifeStyleBinding.root)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivClose -> {
                dialog?.dismiss()
            }

            R.id.btnDone -> {
            }

            R.id.btnCancel -> {
                dialog?.dismiss()
            }
        }
    }

    private fun getDropDownList(): ArrayList<Map<String, Any>> {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultID
            )
        )
        return dropDownList
    }
}