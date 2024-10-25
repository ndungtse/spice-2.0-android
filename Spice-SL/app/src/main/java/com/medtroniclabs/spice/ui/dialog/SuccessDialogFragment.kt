package com.medtroniclabs.spice.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.DefinedParams.PatientId
import com.medtroniclabs.spice.databinding.FragmentSuccessDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.HouseholdNo
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.IsHousehold
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.IsHouseholdMember
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

        fun newInstance(isHousehold : Boolean = false, isMember : Boolean = false): SuccessDialogFragment {
            val bundle = Bundle()
            bundle.putBoolean(IsHousehold, isHousehold)
            bundle.putBoolean(IsHouseholdMember, isMember)
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
        binding.householdNo.invisible()

        if (arguments?.getBoolean(IsHousehold) == true) {
            binding.successMessage.text = getString(R.string.household_successfully)
        }

        if (arguments?.getBoolean(IsHouseholdMember) == true) {
            binding.successMessage.text = getString(R.string.member_registered_successfully)
        }
    }

    override fun onClick(view: View) {
        when(view.id){
            binding.btnDone.id -> {
                onDismissListener?.onDialogDismissListener(
                    arguments?.getBoolean(IsHousehold) == true
                )
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissListener = null
    }
}