package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.ChildPatientId
import com.medtroniclabs.spice.common.DefinedParams.DOB
import com.medtroniclabs.spice.common.DefinedParams.DateOfDelivery
import com.medtroniclabs.spice.common.DefinedParams.Gender
import com.medtroniclabs.spice.common.DefinedParams.HIV
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.common.DefinedParams.MemberID
import com.medtroniclabs.spice.common.DefinedParams.NeonateOutcome
import com.medtroniclabs.spice.common.DefinedParams.PatientId
import com.medtroniclabs.spice.common.DefinedParams.female
import com.medtroniclabs.spice.common.DefinedParams.male
import com.medtroniclabs.spice.databinding.FragmentPatientMenuBinding
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PREGNANCY_MAX_AGE
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PREGNANCY_MIN_AGE
import com.medtroniclabs.spice.ui.home.MenuSelectionListener
import com.medtroniclabs.spice.ui.home.ToolsViewModel
import com.medtroniclabs.spice.ui.home.adapter.DashboardMenuItemsAdapter
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.AboveFiveYearsBaseActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.SelectFlowDialog
import com.medtroniclabs.spice.ui.medicalreview.underfiveyears.UnderFiveYearsBaseActivity
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.activity.UnderTwoMonthsBaseActivity
import com.medtroniclabs.spice.ncd.medicalreview.NCDMedicalReviewActivity
import com.medtroniclabs.spice.ui.household.ConsentFormActivity
import com.medtroniclabs.spice.ui.medicalreview.epi.ImmunizationActivity
import com.medtroniclabs.spice.ui.medicalreview.familyplan.activity.FamilyPlanMedicalReviewActivity
import com.medtroniclabs.spice.ui.medicalreview.hiv.activity.HivImrAndCmrActivity
import com.medtroniclabs.spice.ui.medicalreview.tb.activity.TBMedicalReviewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientMenuFragment : BaseFragment(), MenuSelectionListener {

    lateinit var binding: FragmentPatientMenuBinding
    private val viewModel: ToolsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getMyPatientsMenuItemsList()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.menuListLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity).showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity).hideLoading()
                    resourceState.data?.let {
                        setAdapterViews(it)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity).hideLoading()
                }
            }
        }
    }

    private fun setAdapterViews(menuItemsList: List<MenuEntity>) {
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
        val dob = arguments?.getString(DOB, "")
        // Get the menu items list

        // Check and set isDisable property based on gender
        menuItemsList.forEach { menuItem ->
            when (menuItem.name) {
                 (MenuConstants.MOTHER_AND_NEONATE_ID)->{
                    menuItem.isDisabled = when {
                        gender.equals(male, true) -> true
                        gender.equals(female, true) && !dob.isNullOrBlank() -> {
                            val ageAndWeek = DateUtils.getV2YearMonthAndWeek(dob)
                            val ageYears = ageAndWeek.years
                            val ageMonths = ageAndWeek.months
                            val ageWeeks = ageAndWeek.weeks
                            val ageDays = ageAndWeek.days

                            (ageYears !in PREGNANCY_MIN_AGE..PREGNANCY_MAX_AGE) || (ageYears == PREGNANCY_MAX_AGE && (ageMonths + ageWeeks + ageDays) != 0)
                        }

                        (gender.equals(female, true) || gender.equals(
                            male,
                            true
                        )) && dob.isNullOrBlank() -> true

                        else -> false
                    }
                }

                    MenuConstants.GENERAL_ID -> menuItem.isDisabled = isGeneralDisabled(dob)
                    MenuConstants.UNDER_AGE_FIVE_TO_TWO_MONTHS_ID -> menuItem.isDisabled =isUnderFiveToTwoMonthsDisabled(dob)
                    MenuConstants.UNDER_AGE_ABOVE_FIVE_YEAR_ID ->menuItem.isDisabled = isUnderAgeAboveFiveYearsDisabled(dob)
                    MenuConstants.EPI_ID -> menuItem.isDisabled = !isUnderSixtyMonths(dob)
                    MenuConstants.TB_MENU_ID -> menuItem.isDisabled = false
                    else -> {}

            }

        }
        binding.rvActivitiesList.adapter =
            DashboardMenuItemsAdapter(menuItemsList.filter { !it.isDisabled }, this)
    }

    companion object {
        const val TAG = "PatientMenuFragment"
        fun newInstance() =
            PatientMenuFragment()

        fun newInstance(
            patientId: String?,
            id: String?,
            memberId: String?,
            gender: String?,
            dob: String?,
            childPatientId: String?,
            dateOfDelivery:String?,
            neonateOutcome: String?,
            householdId:String?,
            villageId:String?
        ): PatientMenuFragment {
            val fragment = PatientMenuFragment()
            val bundle = Bundle()
            bundle.putString(PatientId, patientId)
            bundle.putString(ID, id)
            bundle.putString(MemberID, memberId)
            bundle.putString(Gender, gender)
            bundle.putString(DOB, dob)
            bundle.putString(ChildPatientId, childPatientId)
            bundle.putString(DateOfDelivery, dateOfDelivery)
            bundle.putString(NeonateOutcome,neonateOutcome)
            bundle.putString(DefinedParams.householdId, householdId)
            bundle.putString(DefinedParams.villageId, villageId)
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
                val intent = Intent(requireContext(), TBMedicalReviewActivity::class.java)
                intent.putExtra(PatientId, arguments?.getString(PatientId))
                intent.putExtra(ID, arguments?.getString(ID))
                startActivity(intent)
            }

            MenuConstants.GENERAL_ID -> {
                val intent = Intent(requireContext(), AboveFiveYearsBaseActivity::class.java)
                intent.putExtra(PatientId, arguments?.getString(PatientId))
                intent.putExtra(ID, arguments?.getString(ID))
                startActivity(intent)
            }

            MenuConstants.MOTHER_AND_NEONATE_ID -> {
                withNetworkAvailability(online = {
                    val patientId = arguments?.getString(PatientId, "")
                    val id = arguments?.getString(ID, "")
                    val childPatientId=arguments?.getString(ChildPatientId,"")
                    val dateOfDelivery=arguments?.getString(DateOfDelivery,"")
                    val neonateOutcome=arguments?.getString(NeonateOutcome,"")
                    if (patientId?.isNotBlank() == true) {
                        SelectFlowDialog.newInstance(patientId, id,childPatientId,dateOfDelivery,neonateOutcome)
                            .show(childFragmentManager, SelectFlowDialog.TAG)
                    }
                })
            }

            MenuConstants.UNDER_AGE_FIVE_TO_TWO_MONTHS_ID -> {
                val intent = Intent(requireContext(), UnderTwoMonthsBaseActivity::class.java)
                intent.putExtra(PatientId, arguments?.getString(PatientId))
                intent.putExtra(ID, arguments?.getString(ID))
                startActivity(intent)
            }

            MenuConstants.UNDER_AGE_ABOVE_FIVE_YEAR_ID -> {
                val intent = Intent(requireContext(), UnderFiveYearsBaseActivity::class.java)
                intent.putExtra(PatientId, arguments?.getString(PatientId))
                intent.putExtra(ID, arguments?.getString(ID))
                startActivity(intent)
            }

            MenuConstants.EPI_ID -> {
                val intent = Intent(requireContext(), ImmunizationActivity::class.java)
                intent.putExtra(PatientId, arguments?.getString(PatientId))
                intent.putExtra(DOB, arguments?.getString(DOB))
                intent.putExtra(ID, arguments?.getString(ID))
                intent.putExtra(MemberID, arguments?.getString(MemberID))
                intent.putExtra(DefinedParams.householdId, arguments?.getString(DefinedParams.householdId))
                intent.putExtra(DefinedParams.villageId, arguments?.getString(DefinedParams.villageId))
                startActivity(intent)
            }

            MenuConstants.FP_MENU_MR -> {
                val intent = Intent(requireContext(),FamilyPlanMedicalReviewActivity::class.java)
                intent.putExtra(PatientId, arguments?.getString(PatientId))
                intent.putExtra(DOB, arguments?.getString(DOB))
                intent.putExtra(ID, arguments?.getString(ID))
                intent.putExtra(MemberID, arguments?.getString(MemberID))
                startActivity(intent)
            }

            MenuConstants.HIV -> {
                val intent = Intent(requireContext(), ConsentFormActivity::class.java)
                intent.putExtra(PatientId, arguments?.getString(PatientId))
                intent.putExtra(HIV, true)
                intent.putExtra(ID, arguments?.getString(ID))
                startActivity(intent)
            }
            else -> {
                startAssessmentActivity()
            }
        }
    }


    private fun startAssessmentActivity() {
        if (connectivityManager.isNetworkAvailable()) {
            val intent = Intent(requireContext(), NCDMedicalReviewActivity::class.java)
            val patientId = arguments?.getString(PatientId, "")
            if (patientId?.isNotBlank() == true) {
                intent.putExtra(PatientId, patientId)
            }
            startActivity(intent)
        } else {
            showErrorDialog(getString(R.string.error), getString(R.string.no_internet_error))
        }
    }
    private fun isGeneralDisabled(dob: String?): Boolean {
        if (dob.isNullOrBlank()) return false
        val age = DateUtils.getV2YearMonthAndWeek(dob)
        return when {
            age.years > 5 -> false
            age.years == 5 && (age.months > 0 || age.weeks > 0 || age.days > 0) -> false
            else -> true
        }
    }

    private fun isGeneralDisabledForTB(dob: String?): Boolean {
        if (dob.isNullOrBlank()) return false
        val age = DateUtils.getV2YearMonthAndWeek(dob)
        return when {
            age.years >= 5 -> false  // Allow exactly 5 and more
            else -> true
        }
    }

    private fun isUnderFiveToTwoMonthsDisabled(dob: String?): Boolean {
        if (dob.isNullOrBlank()) return false
        val age = DateUtils.getV2YearMonthAndWeek(dob)
        return when {
            // Age is under 2 months
            age.years == 0 && age.months < 2 -> false
            // Age is exactly 2 months but with any week or day deviation
            // age.years == 0 && age.months == 2 && age.weeks == 0 && age.days== 0 -> false
            else -> true
        }
    }

    private fun isUnderAgeAboveFiveYearsDisabled(dob: String?): Boolean {
        if (dob.isNullOrBlank()) return false
        val age = DateUtils.getV2YearMonthAndWeek(dob)
        return when {
            // Age is less than 2 months
            age.years == 0 && age.months < 2 -> true

            age.years == 0  && (age.months == 2 && age.weeks == 0 && age.days == 0)->false
            // Age is exactly 5 years
            age.years == 5 && (age.months == 0 && age.weeks == 0 && age.days == 0) -> false
            // Age is more than 2 months but less than 5 years
            age.years < 5 -> false
            else -> true
        }
    }

    private fun isUnderSixtyMonths(dob: String?): Boolean {
        val maxMonthAgeForEPI = 60
        if (dob.isNullOrBlank()) return false
        val age = DateUtils.getV2YearMonthAndWeek(dob)
        val ageInMonths = (age.years * 12) + age.months

        return ageInMonths < maxMonthAgeForEPI
    }
}