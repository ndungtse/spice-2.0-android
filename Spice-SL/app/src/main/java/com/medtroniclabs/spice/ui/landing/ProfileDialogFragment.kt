package com.medtroniclabs.spice.ui.landing

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.common.ViewUtils.setDialogHeightToWrapAndSetWidthPercent
import com.medtroniclabs.spice.databinding.FragmentProfileDialogBinding
import com.medtroniclabs.spice.db.entity.UserProfile
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.landing.viewmodel.LandingViewModel

class ProfileDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentProfileDialogBinding
    private var onDismissListener: OnDialogDismissListener? = null
    private val viewModel: LandingViewModel by activityViewModels()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        onDismissListener = context as OnDialogDismissListener
    }

    companion object {
        const val TAG = "ProfileDialogFragment"
        fun newInstance(): ProfileDialogFragment {
            return ProfileDialogFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.userProfileLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity)?.hideLoading()
                    resourceState.data?.let {
                        setUserProfileData(it)
                        setAllVillageName()
                    }
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }
    }

    private fun setAllVillageName() {
        binding.tvVillageText.text = viewModel.villageListResponse?.value?.let { list ->
            list.joinToString { it.name }
        } ?: getString(R.string.separator_hyphen)
        binding.tvAssignedHealthFacilityText.text =
            viewModel.defaultHealthFacilityLiveData?.value?.let {
                it.name
            } ?: getString(R.string.separator_hyphen)
    }

    private fun initView() {
        with(binding) {
            ivClose.safeClickListener(this@ProfileDialogFragment)
            btnCancel.safeClickListener(this@ProfileDialogFragment)
        }
    }

    fun setUserProfileData(user: UserProfile) {
        with(binding) {
            tvName.text = user.firstName?.let {
                requireContext().getString(
                    R.string.firstname_lastname,
                    user.firstName,
                    user.lastName
                )
            } ?: getString(R.string.separator_hyphen)
            tvGenderText.text = user.gender ?: getString(R.string.separator_hyphen)
            tvEmailText.text = user.username
            tvPhoneNumberText.text = user.phoneNumber ?: getString(R.string.separator_hyphen)
            tvSuiteAccessText.text = user.suiteAccess?.let {
                user.suiteAccess[0] ?: getString(R.string.separator_hyphen)
            } ?: getString(R.string.separator_hyphen)
            tvRoleText.text = user.roles.joinToString { it.name }
        }
    }

    override fun onStart() {
        super.onStart()
        handleDialogHeight()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogHeight()
    }

    private fun handleDialogHeight() {
        val res = context?.resources?.configuration?.orientation
        if (res != Configuration.ORIENTATION_PORTRAIT) {
            setDialogHeightToWrapAndSetWidthPercent(90)
        } else {
            setDialogHeightToWrapAndSetWidthPercent(70)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivClose -> {
                onDismissListener?.onDialogDismissListener()
                dismiss()
            }

            R.id.btnCancel -> {
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