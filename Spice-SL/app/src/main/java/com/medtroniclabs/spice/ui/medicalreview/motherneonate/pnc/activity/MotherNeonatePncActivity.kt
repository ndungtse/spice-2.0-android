package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.databinding.ActivityMedicalReviewPncBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancyPastObstetricHistoryFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.listener.PncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.viewmodel.MotherNeonatePNCViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MotherNeonatePncActivity : BaseActivity(), View.OnClickListener, PncVisitCallBack {
    private lateinit var binding: ActivityMedicalReviewPncBinding
    private val viewModel: MotherNeonatePNCViewModel by viewModels()
    private var addedFragments: List<Fragment>? = null
    private val chipItemViewModel: ClinicalNotesViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalReviewPncBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            callback = {
                backNavigation()
            }
        )
        getCurrentLocation()
        initializePatientInfoFragment()
        binding.btnSubmit.text = getString(R.string.next)
        binding.blurView.safeClickListener {}
    }

    private fun getCurrentLocation() {
        SpiceLocationManager(this).getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    data class FragmentConfig<T : Fragment>(
        val containerId: Int,
        val fragmentClass: Class<T>,
        val newInstance: (() -> T?)? = null,
        val args: Bundle? = null
    )

    // Extension function to initialize or replace fragments
    private fun FragmentManager.initializeOrReplaceFragments(fragmentConfigs: List<FragmentConfig<out Fragment>>): MutableList<Fragment> {
        val fragmentTransaction = beginTransaction()
        val addedFragments = mutableListOf<Fragment>()
        fragmentConfigs.forEach { config ->
            val existingFragment = findFragmentById(config.containerId)
            val fragmentClass = config.fragmentClass
            val tag = fragmentClass.simpleName
            val fragmentInstance =
                config.newInstance?.invoke() ?: fragmentClass.getDeclaredConstructor()
                    .newInstance().apply {
                        arguments = config.args
                    }
            if (existingFragment == null) {
                fragmentTransaction.add(config.containerId, fragmentInstance, tag)
                addedFragments.add(fragmentInstance)
            } else if (!config.fragmentClass.isInstance(existingFragment)) {
                fragmentTransaction.replace(config.containerId, fragmentInstance, tag)
                addedFragments.add(fragmentInstance)
            }
        }

        fragmentTransaction.commit()
        return addedFragments
    }


    private fun addReplaceFragment(containerId: Int, fragment: Fragment) {
        val existingFragment = supportFragmentManager.findFragmentById(containerId)
        supportFragmentManager.commit {
            if (existingFragment == null) {
                add(containerId, fragment)
            } else if (!existingFragment::class.java.isInstance(fragment)) {
                replace(containerId, fragment)
            }
        }
    }

    private fun initializePatientInfoFragment() {
        val patientInfoFragment = PatientInfoFragment.newInstance(
            intent.getStringExtra(DefinedParams.PatientId),
            isPnc = true
        ).apply {
            setDataCallbackPNC(this@MotherNeonatePncActivity)
        }
        addReplaceFragment(R.id.patientDetailFragment, patientInfoFragment)
    }

    private fun initializeFragment() {
        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.ANC.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.ANC.name
            )
        }
        val medicalReviewPatientDiagnosisFragment =
            MedicalReviewPatientDiagnosisFragment.newInstance(
                true,
                intent.getStringExtra(DefinedParams.PatientId),
                viewModel.memberId
            )
        addReplaceFragment(R.id.diagnosisFragment, medicalReviewPatientDiagnosisFragment)

        val presentingComplaintsFragment =
            PresentingComplaintsFragment::class.java.getDeclaredConstructor().newInstance()
        presentingComplaintsFragment.arguments = bundle
        addReplaceFragment(R.id.presentingComplaintsContainer, presentingComplaintsFragment)

        val systemicExaminationsFragment =
            SystemicExaminationsFragment::class.java.getDeclaredConstructor().newInstance()
        systemicExaminationsFragment.arguments = bundle
        addReplaceFragment(R.id.systemicExaminationsContainer, systemicExaminationsFragment)

        val clinicalNotesFragment =
            ClinicalNotesFragment::class.java.getDeclaredConstructor().newInstance()
        clinicalNotesFragment.arguments = bundle
        addReplaceFragment(R.id.clinicalNotesContainer, clinicalNotesFragment)

        with(binding) {
            patientDetailFragment.visible()
            diagnosisFragment.visible()
            presentingComplaintsContainer.visible()
            systemicExaminationsContainer.visible()
            clinicalNotesContainer.visible()
        }
    }

    override fun onDataLoaded(data: PatientListRespModel) {
        viewModel.pncVisit = data.pregnancyDetails?.pncVisitAssessment?.takeIf { true } ?: 1
        viewModel.memberId = data.memberId
        if (viewModel.pncVisit == 1L) {
            initView()
        }
    }

    private fun initView() {
        showLoading()
        initializeFragment()
        initializeAliveStatusLayout()
        showBottomNavigation()
        setButtonClickListener()
        clinicalNotesResult()
    }

    private fun initializeAliveStatusLayout() {
        getAliveStatusFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = PregnancyPastObstetricHistoryFragment.TAG
            view.addViewElements(
                it,
                false,
                viewModel.resultFlowHashMap,
                Pair(PregnancyPastObstetricHistoryFragment.TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
            binding.btnLayout.addView(view)
        }
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultFlowHashMap[PregnancyPastObstetricHistoryFragment.TAG] =
                selectedID as String
            val flowValue =
                viewModel.resultFlowHashMap[PregnancyPastObstetricHistoryFragment.TAG] as? String
            viewModel.deliveryKit =
                flowValue?.equals(HouseHoldRegistration.yes, ignoreCase = true) ?: false
            if (viewModel.deliveryKit == true) {
                binding.blurView.gone()
            } else {
                binding.blurView.visible()
                isMotherOrChildNotAlive(binding.tvAliveStatus.text == getString(R.string.is_the_mother_alive))
            }
        }


    private fun isMotherOrChildNotAlive(motherOrChild: Boolean) {
        binding.btnSubmit.isEnabled = false
        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.ANC.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.ANC.name
            )
        }
        supportFragmentManager.commit {
            val presentingComplaintsFragment =
                PresentingComplaintsFragment::class.java.getDeclaredConstructor().newInstance()
            presentingComplaintsFragment.arguments = bundle

            val systemicExaminationsFragment =
                if (motherOrChild) SystemicExaminationsFragment::class.java.getDeclaredConstructor()
                    .newInstance() else ClinicalNotesFragment::class.java.getDeclaredConstructor()
                    .newInstance()
            systemicExaminationsFragment.arguments = bundle

            val clinicalNotesFragment =
                ClinicalNotesFragment::class.java.getDeclaredConstructor().newInstance()
            clinicalNotesFragment.arguments = bundle
            val map = hashMapOf(
                presentingComplaintsFragment to binding.presentingComplaintsContainer.id,
                systemicExaminationsFragment to binding.systemicExaminationsContainer.id,
                clinicalNotesFragment to binding.clinicalNotesContainer.id
            )
            map.forEach { (fragment, containerId) ->
                addReplaceFragment(containerId, fragment)
            }
        }
    }

        private fun getAliveStatusFlowData(): ArrayList<Map<String, Any>> {
            val flowList = ArrayList<Map<String, Any>>()
            flowList.add(
                CommonUtils.getOptionMap(
                    getString(R.string.yes),
                    getString(R.string.yes)
                )
            )
            flowList.add(
                CommonUtils.getOptionMap(
                    getString(R.string.no),
                    getString(R.string.no)
                )
            )
            return flowList
        }

        private fun showBottomNavigation() {
            binding.bottomNavigationView.visible()
        }

        private fun enableSubmitBtn() {
            binding.btnSubmit.isEnabled = chipItemViewModel.enteredClinicalNotes.isNotBlank()
        }

        private fun backNavigation() {
            val fragmentManager = supportFragmentManager
            val systemicExaminationsFragment =
                fragmentManager.findFragmentById(R.id.systemicExaminationsContainer)
            if (viewModel.pncVisit == 1L && systemicExaminationsFragment is SystemicExaminationsFragment) {
                // Show the dialog here
                showErrorDialog()
            } else if (viewModel.pncVisit == 1L && systemicExaminationsFragment is ClinicalNotesFragment) {
                showLoading()
                initView()
                hideLoading()
            }

        }

        override fun onClick(v: View?) {
            when (v?.id) {
                binding.btnSubmit.id -> physicalExaminationFlow()
            }
        }

        private fun physicalExaminationFlow() {
            if (binding.btnSubmit.text == getString(R.string.next)) {
                val bundle = Bundle()
                replaceFragmentInId<ClinicalNotesFragment>(
                    binding.systemicExaminationsContainer.id,
                    bundle = bundle,
                    tag = SystemicExaminationsFragment::class.simpleName
                )
            }
        }

        private fun clinicalNotesResult() {
            supportFragmentManager
                .setFragmentResultListener(
                    MedicalReviewDefinedParams.CLINICAL_NOTES,
                    this
                ) { _, _ ->
                    enableSubmitBtn()
                }
        }

        private fun showErrorDialog() {
            showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.exit_reason),
                isNegativeButtonNeed = true
            ) { isPositive ->
                if (isPositive) {
                    onBackPressPopStack()
                }
            }
        }

        private fun onBackPressPopStack() {
            this@MotherNeonatePncActivity.finish()
        }

        private fun setButtonClickListener() {
            binding.btnSubmit.safeClickListener(this@MotherNeonatePncActivity)
        }


    }

