package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.databinding.FragmentHivTestBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.InformationLayoutFragment
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.A1_TEST_RESULT
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.A2_TEST_RESULT
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.A3_TEST_RESULT
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums

class HivTestFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentHivTestBinding
    private val hivViewModel: HivViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHivTestBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        addObservers()
    }

    private fun initViews() {
        hivViewModel.getHistoryListMetaItems()
        binding.tvPreTestCounselling.safeClickListener(this)
        binding.tvTestForHiv.safeClickListener(this)
        binding.tvPostTestCounselling.safeClickListener(this)
        binding.tvA1TestResult.text = getString(R.string.a_test_result, 1)
        binding.tvA2TestResult.text = getString(R.string.a_test_result, 2)
        binding.tvA3TestResult.text = getString(R.string.a_test_result, 3)
        binding.tvA1TestResult.markMandatory()
        binding.tvA2TestResult.markMandatory()
        binding.tvA3TestResult.markMandatory()
        binding.tvEntryPoint.markMandatory()

        addCustomView(
            getData(),
            A1_TEST_RESULT,
            hivViewModel.resultHashMap,
            A1TestCallBack,
            binding.llA1TestResultRoot
        )
        addCustomView(
            getData(),
            A2_TEST_RESULT,
            hivViewModel.resultHashMap,
            A2TestCallBack,
            binding.llA2TestResultRoot
        )
        addCustomView(
            getData(),
            A3_TEST_RESULT,
            hivViewModel.resultHashMap,
            A3TestCallBack,
            binding.llA3TestResultRoot
        )
    }

    private fun addObservers() {
        hivViewModel.hivMetaListItems.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { list ->
                        initializeEntryPointOptions(
                            list.filter { it.category == MedicalReviewTypeEnums.entry_point.name }
                                .sortedBy { it.displayOrder }
                        )
                    }
                }
            }
        }
    }

    private fun initializeEntryPointOptions(entryList: List<MedicalReviewMetaItems>) {
        val entryPoint = ArrayList<Map<String, Any>>()
        entryPoint.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.id to DefinedParams.DefaultID,
                DefinedParams.Value to DefinedParams.DefaultIDLabel
            )
        )

        entryList.forEach {
            entryPoint.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to it.name,
                    DefinedParams.id to it.id.toString(),
                    DefinedParams.Value to (it.value ?: it.name)
                )
            )
        }

        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(entryPoint)
        binding.tvEntryPointSpinner.adapter = adapter
        binding.tvEntryPointSpinner.setSelection(0, false)
        binding.tvEntryPointSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedName = it[DefinedParams.NAME] as String?
                        if (selectedName != DefinedParams.DefaultIDLabel) {
                            hivViewModel.selectedEntryPoint = it[DefinedParams.Value] as String
                            resultMapChanged()
                        } else {
                            hivViewModel.selectedEntryPoint = null
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    private fun addCustomView(
        data: ArrayList<Map<String, Any>>,
        tag: String,
        hashMap: HashMap<String, Any>,
        callback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)?,
        container: ViewGroup?
    ) {
        SingleSelectionCustomView(binding.root.context).apply {
            this.tag = tag
            addViewElements(
                data,
                false,
                hashMap,
                Pair(tag, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                callback
            )
            container?.addView(this)
        }
    }

    private fun getData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.reactive), getString(R.string.reactive)))
        flowList.add(
            getOptionMap(
                getString(R.string.non_reactive),
                getString(R.string.non_reactive)
            )
        )
        flowList.add(
            getOptionMap(
                getString(R.string.inconclusive),
                getString(R.string.inconclusive)
            )
        )
        return flowList
    }

    private var A1TestCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            hivViewModel.resultHashMap[A1_TEST_RESULT] = selectedID as String
            resultMapChanged()
        }
    private var A2TestCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            hivViewModel.resultHashMap[A2_TEST_RESULT] = selectedID as String
            resultMapChanged()
        }
    private var A3TestCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            hivViewModel.resultHashMap[A3_TEST_RESULT] = selectedID as String
            resultMapChanged()
        }

    private fun resultMapChanged() {
        setFragmentResult(
            MedicalReviewDefinedParams.HIV_TEST_ITEM, bundleOf(
                MedicalReviewDefinedParams.HIV_TEST_VALUES to true
            )
        )
    }

    companion object {
        const val TAG = "HivTestFragment"
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvPreTestCounselling -> {
                InformationLayoutFragment.newInstance(
                    AssessmentDefinedParams.PreTestCounselling,
                    getString(R.string.pre_test_counselling)
                ).show(childFragmentManager, InformationLayoutFragment.TAG)
            }

            R.id.tvTestForHiv -> {
                InformationLayoutFragment.newInstance(
                    AssessmentDefinedParams.TestForHiv,
                    getString(R.string.test_for_hiv)
                ).show(childFragmentManager, InformationLayoutFragment.TAG)
            }

            R.id.tvPostTestCounselling -> {
                InformationLayoutFragment.newInstance(
                    AssessmentDefinedParams.PostTestCounselling,
                    getString(R.string.post_test_counselling)
                ).show(childFragmentManager, InformationLayoutFragment.TAG)
            }
        }
    }

    fun validation(): Boolean {
        var isValid = true
        val a1TestResult = hivViewModel.resultHashMap[A1_TEST_RESULT] as? String
        val a2TestResult = hivViewModel.resultHashMap[A2_TEST_RESULT] as? String
        val a3TestResult = hivViewModel.resultHashMap[A3_TEST_RESULT] as? String
        val entryPoint = hivViewModel.selectedEntryPoint

        if (!a1TestResult.isNullOrEmpty()) {
            binding.tvA1TestResultError.gone()
        } else {
            binding.tvA1TestResultError.visible()
            isValid = false
        }

        if (!a2TestResult.isNullOrEmpty()) {
            binding.tvA2TestResultError.gone()
        } else {
            binding.tvA2TestResultError.visible()
            isValid = false
        }

        if (!a3TestResult.isNullOrEmpty()) {
            binding.tvA3TestResultError.gone()
        } else {
            binding.tvA3TestResultError.visible()
            isValid = false
        }

        if (!entryPoint.isNullOrEmpty()) {
            binding.tvEntryPointError.gone()
        } else {
            binding.tvEntryPointError.visible()
            isValid = false
        }
        return isValid
    }

}