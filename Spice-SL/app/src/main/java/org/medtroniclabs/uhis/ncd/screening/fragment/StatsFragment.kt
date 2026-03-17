package org.medtroniclabs.uhis.ncd.screening.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.databinding.FragmentStatsBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ncd.registration.fragment.TermsAndConditionsFragment
import org.medtroniclabs.uhis.ncd.screening.viewmodel.GeneralDetailsViewModel
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentStatsBinding
    private val viewModel: GeneralDetailsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "StatsFragment"

        fun newInstance() = StatsFragment()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.screenedPatientCount.observe(viewLifecycleOwner) {
            binding.tvPeopleScreened.text = it.toString()
        }
        viewModel.referredPatientCount.observe(viewLifecycleOwner) {
            binding.tvPeopleReferred.text = it.toString()
        }
    }

    private fun initView() {
        binding.apply {
            tvSiteName.text = viewModel.siteDetail.siteName.takeIf { it.isNotBlank() } ?: getString(
                R.string.hyphen_symbol,
            )
            tvScreeningCategory.text =
                viewModel.siteDetail.categoryDisplayName?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.hyphen_symbol)
            tvScreeningType.text =
                viewModel.siteDetail.categoryDisplayType?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.hyphen_symbol)
            btnScreenNextPatient.safeClickListener(this@StatsFragment)
        }
        viewModel.toGetCount(true)
        (activity as? BaseActivity)?.showBackButton()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnScreenNextPatient -> {
                replaceFragmentIfExists<TermsAndConditionsFragment>(
                    R.id.screeningParentLayout,
                    bundle = null,
                    tag = TermsAndConditionsFragment.TAG,
                )
            }
        }
    }
}
