package com.medtroniclabs.spice.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.databinding.FragmentSuccessDialogBinding
import com.medtroniclabs.spice.ui.household.viewmodel.HouseHoldSummaryViewModel
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener

class SuccessDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentSuccessDialogBinding
    private var onDismissListener: OnDialogDismissListener? = null
    private val householdSummaryViewModel: HouseHoldSummaryViewModel by activityViewModels ()

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

        fun newInstance(): SuccessDialogFragment {
            return SuccessDialogFragment()
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
        householdSummaryViewModel.houseHoldDetailLiveData.value?.data?.let {
            binding.householdNo.text = requireContext().getString(R.string.household_with_no, it.householdNo.toString())
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