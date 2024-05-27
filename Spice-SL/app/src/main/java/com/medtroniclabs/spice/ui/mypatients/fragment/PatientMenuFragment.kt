package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams.Gender
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.common.DefinedParams.PatientId
import com.medtroniclabs.spice.common.DefinedParams.male
import com.medtroniclabs.spice.databinding.FragmentPatientMenuBinding
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.home.MenuSelectionListener
import com.medtroniclabs.spice.ui.home.ToolsViewModel
import com.medtroniclabs.spice.ui.home.adapter.DashboardMenuItemsAdapter
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.AboveFiveYearsBaseActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.SelectFlowDialog
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.UnderTwoMonthsBaseActivity
import com.medtroniclabs.spice.ui.mypatients.MedicalReviewBaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientMenuFragment : BaseFragment(), MenuSelectionListener {

    lateinit var binding: FragmentPatientMenuBinding
    private val viewModel: ToolsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapterViews()
    }

    private fun setAdapterViews() {
        if (CommonUtils.checkIsTablet(requireContext())) {
            val layoutManager = FlexboxLayoutManager(context)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            binding.rvActivitiesList.layoutManager = layoutManager
        } else {
            val layoutManager = GridLayoutManager(context, 2)
            binding.rvActivitiesList.layoutManager = layoutManager
        }
        val gender = arguments?.getString(Gender, "")
        // Get the menu items list
        val menuItemsList = viewModel.getMyPatientsMenuItemsList()

        // Check and set isDisable property based on gender
        menuItemsList.forEach {
            if (it.name == MenuConstants.MOTHER_AND_NEONATE_ID && gender.equals(male,true)) {
                it.isDisabled = true
            }
        }
        binding.rvActivitiesList.adapter =
            DashboardMenuItemsAdapter(menuItemsList, this)
    }

    companion object {
        const val TAG = "PatientMenuFragment"
        fun newInstance() =
            PatientMenuFragment()

        fun newInstance(patientId: String?, id: String?,gender: String?): PatientMenuFragment {
            val fragment = PatientMenuFragment()
            val bundle = Bundle()
            bundle.putString(PatientId, patientId)
            bundle.putString(ID, id)
            bundle.putString(Gender, gender)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onMenuSelected(menuId: String, subModule: String?) {
        startAssessmentToolsActivity(menuId)
    }

    private fun startAssessmentToolsActivity(menuId: String) {
        when (menuId) {
            MenuConstants.TB_MENU_ID -> {
            }

            MenuConstants.GENERAL_ID -> {
                val intent = Intent(requireContext(), AboveFiveYearsBaseActivity::class.java)
                intent.putExtra(PatientId, arguments?.getString(PatientId))
                intent.putExtra(ID, arguments?.getString(ID))
                startActivity(intent)
            }

            MenuConstants.MOTHER_AND_NEONATE_ID -> {
                val patientId = arguments?.getString(PatientId, "")
                if (patientId?.isNotBlank() == true) {
                    SelectFlowDialog.newInstance(patientId).show(childFragmentManager, SelectFlowDialog.TAG)
                }
            }

            MenuConstants.UNDER_AGE_FIVE_TO_TWO_MONTHS_ID -> {
                val intent = Intent(requireContext(), UnderTwoMonthsBaseActivity::class.java)
                intent.putExtra(PatientId, arguments?.getString(PatientId))
                startActivity(intent)
            }

            MenuConstants.UNDER_AGE_ABOVE_FIVE_YEAR_ID -> {
            }

            MenuConstants.EPI_ID -> {
            }

            else -> {
                startAssessmentActivity()
            }
        }
    }


    private fun startAssessmentActivity() {
        if (connectivityManager.isNetworkAvailable()) {
            val intent = Intent(requireContext(), MedicalReviewBaseActivity::class.java)
            val patientId = arguments?.getString(PatientId, "")
            if (patientId?.isNotBlank() == true) {
                intent.putExtra(PatientId, patientId)
            }
            startActivity(intent)
        } else {
            showErrorDialog(getString(R.string.error), getString(R.string.no_internet_error))
        }
    }
}