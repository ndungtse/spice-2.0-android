package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.model.BpAndWeightRequestModel
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentAddWeightDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.AddHeightViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddHeightDialog : DialogFragment(), View.OnClickListener {
    private val viewModel: AddHeightViewModel by activityViewModels()
    var listener: DialogDismissListener? = null
    private lateinit var binding: FragmentAddWeightDialogBinding

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddWeightDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(requireContext())
        locationManager.getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    companion object {
        const val TAG = "AddHeightDialog"

        fun newInstance(
            patientId: String? = null,
            memberId: String? = null,
            villageId: String?,
            householdId: String?,
        ): AddHeightDialog {
            val fragment = AddHeightDialog()
            fragment.arguments = Bundle().apply {
                putString(DefinedParams.PatientId, patientId)
                putString(DefinedParams.MemberID, memberId)
                putString(DefinedParams.villageId, villageId)
                putString(DefinedParams.householdId, householdId)
            }
            return fragment
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        getCurrentLocation()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.saveHeight.observe(viewLifecycleOwner) { resourcesState ->
            when (resourcesState.state) {
                ResourceState.LOADING -> {
                    binding.loader.visible()
                }

                ResourceState.SUCCESS -> {
                    binding.loader.gone()
                    dismiss()
                    listener?.onDialogDismissed(isBp = false, true)
                    viewModel.saveHeight.postError(optionalData = true)
                }

                ResourceState.ERROR -> {
                    binding.loader.gone()
                }
            }
        }
    }

    private fun initView() {
        with(binding) {
            tvTitle.text = getString(R.string.add_height)
            tvWeightLabel.text = getString(R.string.height)
            tvWeightLabel.markMandatory()
            etWeight.hint = getString(R.string.enter_the_height)
            btnCancel.safeClickListener(this@AddHeightDialog)
            etWeight.addTextChangedListener(textWatcher)
            btnOkay.safeClickListener(this@AddHeightDialog)
            ivClose.safeClickListener(this@AddHeightDialog)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> handleOkayClick()
            binding.btnCancel.id, binding.ivClose.id -> dismiss()
        }
    }

    private fun handleOkayClick() {
        if (connectivityManager.isNetworkAvailable()) {
            if (isHeightValid()) {
                // form the request
                viewModel.saveHeight(createBpAndWeightRequestModel())
            }
        } else {
            (activity as BaseActivity?)?.showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                isNegativeButtonNeed = false,
            ) {
            }
        }
    }

    private fun createBpAndWeightRequestModel(): BpAndWeightRequestModel =
        BpAndWeightRequestModel(
            height = binding.etWeight.text
                ?.trim()
                .toString()
                .toDoubleOrNull(),
            encounter = MedicalReviewEncounter(
                provenance = ProvanceDto(),
                latitude = viewModel.lastLocation?.latitude,
                longitude = viewModel.lastLocation?.longitude,
                patientId = arguments?.getString(DefinedParams.PatientId, ""),
                memberId = arguments?.getString(DefinedParams.MemberID, ""),
                startTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
                endTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
                villageId = arguments?.getString(DefinedParams.villageId),
                householdId = arguments?.getString(DefinedParams.householdId),
            ),
        )

    private fun isHeightValid(): Boolean =
        MotherNeonateUtil.isValidInput(
            binding.etWeight.text.toString(),
            binding.etWeight,
            binding.tvWeightErrorLabel,
            10.0..500.0,
            R.string.height_error,
            false,
            requireContext(),
        )

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int,
        ) {
            // Not needed for your use case
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int,
        ) {
            // Not needed for your use case
        }

        override fun afterTextChanged(s: Editable?) {
            // Call the method to check if any EditText field is filled
            val hasString = (s?.trim()?.count() ?: 0) > 0
            binding.btnOkay.isEnabled = hasString
        }
    }

    private fun handleDialogSize() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = if (CommonUtils.checkIsTablet(requireContext())) {
            if (isLandscape) 65 else 90
        } else {
            if (isLandscape) 65 else 90
        }
        setWidth(width)
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogSize()
    }
}
