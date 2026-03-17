package org.medtroniclabs.uhis.ui.medicalreview.tb.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.postError
import org.medtroniclabs.uhis.appextensions.setWidth
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.SpiceLocationManager
import org.medtroniclabs.uhis.data.model.BpAndWeightRequestModel
import org.medtroniclabs.uhis.data.model.MedicalReviewEncounter
import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.databinding.FragmentAddBpDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.markMandatory
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.DialogDismissListener
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import org.medtroniclabs.uhis.ui.medicalreview.tb.viewmodel.BmiViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AddBMIDialog : DialogFragment(), View.OnClickListener {
    var listener: DialogDismissListener? = null
    private lateinit var binding: FragmentAddBpDialogBinding
    private val viewModel: BmiViewModel by activityViewModels()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddBpDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "AddBMIDialog"

        fun newInstance(
            patientId: String?,
            villageId: String?,
            householdId: String?,
            memberId: String?,
            isTb: Boolean = false,
        ): AddBMIDialog {
            val fragment = AddBMIDialog()
            fragment.arguments = Bundle().apply {
                putString(DefinedParams.PatientId, patientId)
                putString(DefinedParams.villageId, villageId)
                putString(DefinedParams.householdId, householdId)
                putString(DefinedParams.MEMBER_ID, memberId)
                putBoolean(DefinedParams.TB, isTb)
            }
            return fragment
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        getCurrentLocation()
        viewModel.setUserJourney(AnalyticsDefinedParams.AddHeightWeightDialogue)
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(requireContext())
        locationManager.getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun attachObservers() {
        viewModel.saveBMI.observe(viewLifecycleOwner) { resourcesState ->
            when (resourcesState.state) {
                ResourceState.LOADING -> {
                    binding.loader.visible()
                }

                ResourceState.SUCCESS -> {
                    binding.loader.gone()
                    dismiss()
                    listener?.onDialogDismissed(isBp = false, false)
                    viewModel.saveBMI.postError(optionalData = true)
                }

                ResourceState.ERROR -> {
                    binding.loader.gone()
                }
            }
        }
    }

    private fun isTb(): Boolean = arguments?.getBoolean(DefinedParams.TB, false) ?: false

    private fun initView() {
        with(binding) {
            tvTitle.text = getString(R.string.add_height_weight)
            tvSystolic.text = getString(R.string.height)
            tvSystolic.markMandatory()
            etSystolic.hint = getString(R.string.enter_the_height)
            tvDiastolic.text = getString(R.string.weight)
            tvDiastolic.markMandatory()
            etDiastolic.hint = getString(R.string.enter_the_weight)
            etDiastolic.apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                filters = arrayOf(InputFilter.LengthFilter(5))
            }
            etSystolic.apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                filters = arrayOf(InputFilter.LengthFilter(5))
            }
            tvPulse.gone()
            tvPulseError.gone()
            etPulse.gone()
            btnOkay.safeClickListener(this@AddBMIDialog)
            btnCancel.safeClickListener(this@AddBMIDialog)
            ivClose.safeClickListener(this@AddBMIDialog)
            etSystolic.addTextChangedListener(textWatcher)
            etDiastolic.addTextChangedListener(textWatcher)
        }
    }

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
            val heightFilled = binding.etSystolic.text?.isNotBlank() == true
            val weightFilled = binding.etDiastolic.text?.isNotBlank() == true

            // Check if only one of the EditText fields is filled
            val onlyOneFilled = heightFilled && weightFilled

            // Enable the button if only one EditText field is filled
            binding.btnOkay.isEnabled = onlyOneFilled
        }
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogSize()
    }

    private fun inputValidate(): Boolean {
        val isHeightValid = isHeightValid()
        val isWeightValid = isWeightValid()
        return isHeightValid && isWeightValid
    }

    private fun isHeightValid(): Boolean {
        val range = if (isTb()) {
            45.0..300.0
        } else {
            50.0..300.0
        }
        val errorMessage = if (isTb()) {
            R.string.height_error_45_300
        } else {
            R.string.height_error
        }
        return MotherNeonateUtil.isValidInput(
            binding.etSystolic.text.toString(),
            binding.etSystolic,
            binding.tvSystolicError,
            range,
            errorMessage,
            false,
            requireContext(),
        )
    }

    private fun isWeightValid(): Boolean {
        val range = if (isTb()) {
            0.1..400.0
        } else {
            10.0..400.0
        }
        val errorMessage = if (isTb()) {
            R.string.weight_error_0_400
        } else {
            R.string.weight_error
        }
        return MotherNeonateUtil.isValidInput(
            binding.etDiastolic.text.toString(),
            binding.etDiastolic,
            binding.tvDiastolicError,
            range,
            errorMessage,
            false,
            requireContext(),
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> handleOkayClick()
            binding.btnCancel.id, binding.ivClose.id -> {
                viewModel.setUserJourney(AnalyticsDefinedParams.CANCELBUTTONTRIGGERED)
                dismiss()
            }
        }
    }

    private fun handleOkayClick() {
        if (connectivityManager.isNetworkAvailable()) {
            if (inputValidate()) {
                viewModel.setUserJourney(AnalyticsDefinedParams.OKAYBUTTONTRIGGERED)
                viewModel.saveBMI(createHeightAndWeightRequestModel())
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

    private fun createHeightAndWeightRequestModel(): BpAndWeightRequestModel =
        BpAndWeightRequestModel(
            height = binding.etSystolic.text
                ?.trim()
                .toString()
                .toDoubleOrNull(),
            weight = binding.etDiastolic.text
                ?.trim()
                .toString()
                .toDoubleOrNull(),
            encounter = createMedicalReviewEncounter(),
        )

    private fun createMedicalReviewEncounter(): MedicalReviewEncounter =
        MedicalReviewEncounter(
            provenance = ProvanceDto(),
            latitude = viewModel.lastLocation?.latitude,
            longitude = viewModel.lastLocation?.longitude,
            patientId = arguments?.getString(DefinedParams.PatientId, ""),
            startTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
            endTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
            villageId = arguments?.getString(DefinedParams.villageId),
            householdId = arguments?.getString(DefinedParams.householdId),
            memberId = arguments?.getString(DefinedParams.MEMBER_ID, ""),
        )
}
