package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
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
import org.medtroniclabs.uhis.databinding.FragmentAddWeightDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.markMandatory
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.DialogDismissListener
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.isValidInput
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.viewmodel.AddWeightViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AddWeightDialog : DialogFragment(), View.OnClickListener {
    var listener: DialogDismissListener? = null
    private lateinit var binding: FragmentAddWeightDialogBinding
    private val viewModel: AddWeightViewModel by activityViewModels()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    companion object {
        const val TAG = "AddWeightDialog"

        fun newInstance(): AddWeightDialog = AddWeightDialog()

        fun newInstance(
            patientId: String?,
            villageId: String?,
            householdId: String?,
            memberId: String?,
        ): AddWeightDialog {
            val fragment = AddWeightDialog()
            fragment.arguments = Bundle().apply {
                putString(DefinedParams.PatientId, patientId)
                putString(DefinedParams.villageId, villageId)
                putString(DefinedParams.householdId, householdId)
                putString(DefinedParams.MEMBER_ID, memberId)
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
        attachObservers()
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(requireContext())
        locationManager.getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun attachObservers() {
        viewModel.saveWeight.observe(viewLifecycleOwner) { resourcesState ->
            when (resourcesState.state) {
                ResourceState.LOADING -> {
                    binding.loader.visible()
                }

                ResourceState.SUCCESS -> {
                    binding.loader.gone()
                    dismiss()
                    listener?.onDialogDismissed(isBp = false, false)
                    viewModel.saveWeight.postError(optionalData = true)
                }

                ResourceState.ERROR -> {
                    binding.loader.gone()
                }
            }
        }
    }

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

    private fun initView() {
        with(binding) {
            btnCancel.safeClickListener(this@AddWeightDialog)
            tvWeightLabel.markMandatory()
            etWeight.addTextChangedListener(textWatcher)
            btnOkay.safeClickListener(this@AddWeightDialog)
            ivClose.safeClickListener(this@AddWeightDialog)
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

    private fun isWeightValid(): Boolean =
        isValidInput(
            binding.etWeight.text.toString(),
            binding.etWeight,
            binding.tvWeightErrorLabel,
            10.0..400.0,
            R.string.weight_error,
            false,
            requireContext(),
        )

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> handleOkayClick()
            binding.btnCancel.id, binding.ivClose.id -> dismiss()
        }
    }

    private fun handleOkayClick() {
        if (connectivityManager.isNetworkAvailable()) {
            if (isWeightValid()) {
                viewModel.saveWeight(createBpAndWeightRequestModel())
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
            weight = binding.etWeight.text
                ?.trim()
                .toString()
                .toDoubleOrNull(),
            encounter = MedicalReviewEncounter(
                provenance = ProvanceDto(),
                latitude = viewModel.lastLocation?.latitude,
                longitude = viewModel.lastLocation?.longitude,
                patientId = arguments?.getString(DefinedParams.PatientId, ""),
                startTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
                endTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
                villageId = arguments?.getString(DefinedParams.villageId),
                householdId = arguments?.getString(DefinedParams.householdId),
                memberId = arguments?.getString(DefinedParams.MEMBER_ID, ""),
            ),
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
}
