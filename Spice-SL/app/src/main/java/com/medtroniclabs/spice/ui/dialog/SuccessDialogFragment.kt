package com.medtroniclabs.spice.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.DefinedParams.PatientId
import com.medtroniclabs.spice.databinding.FragmentSuccessDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.HouseholdNo
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener

class SuccessDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentSuccessDialogBinding
    private var onDismissListener: OnDialogDismissListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onDismissListener = context as OnDialogDismissListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSuccessDialogBinding.inflate(layoutInflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "SuccessDialogFragment"

        fun newInstance(householdNo : Long, patientId : String): SuccessDialogFragment {
            val bundle = Bundle()
            bundle.putLong(HouseholdNo, householdNo)
            bundle.putString(PatientId, patientId)
            val fragment =  SuccessDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
        attachObserver()
    }

    private fun setListener() {
        binding.btnDone.safeClickListener(this)
    }

    private fun attachObserver() {
        arguments?.getLong(HouseholdNo, -1)?.let { householdNo ->
            if (householdNo != -1L) {
                binding.successMessage.text = getString(R.string.household_successfully)
                binding.householdNo.text =
                    requireContext().getString(R.string.household_with_no, householdNo.toString())
            }
        }
        arguments?.getString(PatientId, DefaultID)?.let { patientId ->
            if (patientId != DefaultID) {
                binding.successMessage.text = getString(R.string.member_registered_successfully)
                binding.householdNo.text =
                    requireContext().getString(R.string.patient_with_id, patientId)
            }
        }
    }

    override fun onClick(view: View) {
        when(view.id){
            binding.btnDone.id -> {
                onDismissListener?.onDialogDismissListener()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissListener = null
    }
}