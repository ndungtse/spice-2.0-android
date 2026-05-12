package org.medtroniclabs.uhis.ui.home

import android.content.Intent
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.medtroniclabs.microcoaching.MicroCoachingSDK
import com.medtroniclabs.microcoaching.ui.chat.CoachingChatBottomSheet
import com.medtroniclabs.microcoaching.ui.components.ChatFab
import com.medtroniclabs.microcoaching.ui.components.LearnFab
import com.medtroniclabs.microcoaching.ui.flow.CoachingFlowActivity
import com.medtroniclabs.microcoaching.ui.theme.MicroCoachingTheme
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.databinding.FragmentHomeScreenBinding
import org.medtroniclabs.uhis.db.entity.MenuEntity
import org.medtroniclabs.uhis.ncd.followup.activity.NCDFollowUpActivity
import org.medtroniclabs.uhis.ncd.screening.ui.ScreeningActivity
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.MenuConstants
import org.medtroniclabs.uhis.ui.common.PatientSearchActivity
import org.medtroniclabs.uhis.ui.communityprofile.CommunityProfileActivity
import org.medtroniclabs.uhis.ui.dashboard.ncd.NCDDashboardViewActivity
import org.medtroniclabs.uhis.ui.followup.FollowUpMyPatientActivity
import org.medtroniclabs.uhis.ui.home.adapter.DashboardMenuItemsAdapter
import org.medtroniclabs.uhis.ui.household.HouseholdSearchActivity
import org.medtroniclabs.uhis.ui.landing.viewmodel.LandingViewModel
import org.medtroniclabs.uhis.ui.peersupervisor.PerformanceMonitoringActivity
import org.medtroniclabs.uhis.ui.services.ServicesActivity
import android.net.ConnectivityManager as AndroidConnectivityManager

@AndroidEntryPoint
class HomeScreenFragment : BaseFragment(), MenuSelectionListener {
    private lateinit var binding: FragmentHomeScreenBinding

    private val viewModel: LandingViewModel by activityViewModels()

    companion object {
        const val TAG = "HomeScreenFragment"

        fun newInstance(): HomeScreenFragment = HomeScreenFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        attachObservers()
        viewModel.getMenus()
        setupCoachingSurfaces()
    }

    /**
     * Wire up the three MicroCoaching SDK overlays on the home screen:
     *   1. Coaching card banner pinned above the menu grid (UC-2, gap-driven)
     *   2. Learn & Grow FAB at bottom-left (UC-1, opens onboarding/modules/quiz)
     *   3. CHW AI chat FAB at bottom-right (opens chat in a bottom sheet)
     *
     * The banner subscribes to `MicroCoachingSDK.morningModules` (v3 morning
     * routine source). When the list is empty the banner Composable renders
     * nothing so the layout collapses cleanly. Tapping the banner opens the
     * v3 Learn flow on the gap-prioritised top module.
     *
     * The chat FAB reuses the model-download dialog from Phase 1.2 — we surface
     * the same prompt before launching the chat sheet if the on-device LLM is
     * not yet downloaded.
     */
    private fun setupCoachingSurfaces() {
        if (!MicroCoachingSDK.isInitialized()) return
        val sdk = MicroCoachingSDK.getInstance()
        val chwId = runCatching { SecuredPreference.getUserId().toString() }.getOrDefault("")

        // Trigger morning-module load — populates `sdk.morningModules` StateFlow
        // via TriggerEvaluator with gap-prioritised ranking.
        sdk.onHomeScreenShown(chwId)

        // ── Morning module banner ──────────────────────────────────────────
        binding.coachingCardBanner.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MicroCoachingTheme {
                    val modules by sdk.morningModules.collectAsState()
                    val top = modules.firstOrNull()
                    if (top != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {
                                    CoachingFlowActivity.launchLearn(requireContext(), chwId)
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = top.titleBn,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                top.descriptionBn?.let { desc ->
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Learn FAB (bottom-left) ────────────────────────────────────────
        binding.learnFab.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MicroCoachingTheme {
                    LearnFab(
                        onClick = {
                            CoachingFlowActivity.launchLearn(requireContext(), chwId)
                        },
                    )
                }
            }
        }

        // ── Chat FAB (bottom-right) ────────────────────────────────────────
        binding.chatFab.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MicroCoachingTheme {
                    ChatFab(
                        onClick = { launchCoachingChatSheet() },
                    )
                }
            }
        }
    }

    /**
     * Open [CoachingChatBottomSheet] if the on-device LLM model is staged.
     * Otherwise prompt the CHW to download it (~800 MB) — same dialog flow as
     * Phase 1.2's drawer entry, surfaced from a different entry point.
     */
    private fun launchCoachingChatSheet() {
        if (!MicroCoachingSDK.isInitialized()) return
        val sdk = MicroCoachingSDK.getInstance()
        if (sdk.modelManager.isModelPresent()) {
            CoachingChatBottomSheet.show(parentFragmentManager)
        } else {
            showCoachingModelDownloadPrompt()
        }
    }

    private fun showCoachingModelDownloadPrompt() {
        val activity = (activity as? BaseActivity) ?: return
        activity.showErrorDialogue(
            title = getString(R.string.coaching_model_download_title),
            message = getString(R.string.coaching_model_download_message),
            isNegativeButtonNeed = true,
            positiveButtonName = getString(R.string.yes),
            cancelBtnName = getString(R.string.no),
        ) { isPositive ->
            if (!isPositive) return@showErrorDialogue
            if (isOnMeteredNetwork()) {
                activity.showErrorDialogue(
                    title = getString(R.string.coaching_metered_network_title),
                    message = getString(R.string.coaching_metered_network_message),
                    isNegativeButtonNeed = true,
                    positiveButtonName = getString(R.string.yes),
                    cancelBtnName = getString(R.string.no),
                ) { metered -> if (metered) triggerCoachingModelDownload() }
            } else {
                triggerCoachingModelDownload()
            }
        }
    }

    private fun triggerCoachingModelDownload() {
        MicroCoachingSDK.getInstance().modelManager.triggerDownload()
        Toast
            .makeText(
                requireContext(),
                getString(R.string.coaching_download_started),
                Toast.LENGTH_LONG,
            ).show()
    }

    private fun isOnMeteredNetwork(): Boolean {
        val cm = requireContext().getSystemService(AndroidConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }

    override fun onResume() {
        super.onResume()
        viewModel.setUserJourney(getString(R.string.home))
        isDeeplink(arguments?.getBoolean(DefinedParams.IsDeepLink, false))
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

    private fun setAdapterViews(menuEntity: List<MenuEntity>) {
        if (CommonUtils.checkIsTablet(requireContext())) {
            val layoutManager = FlexboxLayoutManager(context)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            binding.rvActivitiesList.layoutManager = layoutManager
        } else {
            val layoutManager = GridLayoutManager(context, 2)
            binding.rvActivitiesList.layoutManager = layoutManager
        }
        binding.rvActivitiesList.adapter = DashboardMenuItemsAdapter(menuEntity, this)
    }

    override fun onMenuSelected(
        menuId: String,
        subModule: String?,
    ) {
        when (menuId) {
            MenuConstants.HOUSEHOLD_MENU_ID -> {
                startActivity(Intent(requireContext(), HouseholdSearchActivity::class.java))
            }

            MenuConstants.MY_PATIENTS_MENU_ID -> {
                val bundle = Bundle().apply {
                    putString(DefinedParams.ORIGIN, MenuConstants.MY_PATIENTS_MENU_ID)
                }
                val intent = if (CommonUtils.isCommunity()) {
                    Intent(
                        requireContext(),
                        FollowUpMyPatientActivity::class.java,
                    )
                } else {
                    Intent(requireContext(), PatientSearchActivity::class.java)
                }

                intent.putExtras(bundle)
                if (CommonUtils.isCommunity()) {
                    startActivity(intent)
                } else {
                    withNetworkAvailability(online = {
                        startActivity(intent)
                    })
                }
            }

            MenuConstants.COMMUNITY_PROFILE -> {
                startActivity(Intent(requireContext(), CommunityProfileActivity::class.java))
            }

            MenuConstants.PERFORMANCE_MONITORING_ID -> {
                if (connectivityManager.isNetworkAvailable()) {
                    val intent = Intent(requireContext(), PerformanceMonitoringActivity::class.java)
                    startActivity(intent)
                } else {
                    (activity as BaseActivity?)?.showErrorDialogue(
                        getString(R.string.title_no_network),
                        getString(R.string.message_no_network),
                        isNegativeButtonNeed = false,
                    ) { _ -> }
                }
            }

            // NCD WorkFlow
            MenuConstants.SCREENING -> {
                startActivity(Intent(requireContext(), ScreeningActivity::class.java))
            }

            MenuConstants.REGISTRATION -> {
                withNetworkAvailability(online = {
                    val bundle = Bundle().apply {
                        putString(DefinedParams.ORIGIN, MenuConstants.REGISTRATION.lowercase())
                    }
                    val intent = Intent(requireContext(), PatientSearchActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                })
            }

            MenuConstants.ASSESSMENT -> {
                val bundle = Bundle().apply {
                    putString(DefinedParams.ORIGIN, MenuConstants.ASSESSMENT.lowercase())
                }
                val intent = Intent(requireContext(), PatientSearchActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            MenuConstants.DISPENSE -> {
                val bundle = Bundle().apply {
                    putString(DefinedParams.ORIGIN, MenuConstants.DISPENSE.lowercase())
                }
                val intent = Intent(requireContext(), PatientSearchActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            MenuConstants.DASHBOARD -> {
                val intent = Intent(requireContext(), NCDDashboardViewActivity::class.java)
                startActivity(intent)
            }

            MenuConstants.LIFESTYLE -> {
                val bundle = Bundle().apply {
                    putString(DefinedParams.ORIGIN, MenuConstants.LIFESTYLE.lowercase())
                }
                val intent = Intent(requireContext(), PatientSearchActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            MenuConstants.PSYCHOLOGICAL -> {
                val bundle = Bundle().apply {
                    putString(DefinedParams.ORIGIN, MenuConstants.PSYCHOLOGICAL.lowercase())
                }
                val intent = Intent(requireContext(), PatientSearchActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            MenuConstants.INVESTIGATION -> {
                val bundle = Bundle().apply {
                    putString(DefinedParams.ORIGIN, MenuConstants.INVESTIGATION.lowercase())
                }
                val intent = Intent(requireContext(), PatientSearchActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            MenuConstants.FOLLOW_UP -> {
                if ((CommonUtils.isChp())) {
                    val intent = Intent(requireContext(), FollowUpMyPatientActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(requireContext(), NCDFollowUpActivity::class.java)
                    startActivity(intent)
                }
            }

            MenuConstants.SERVICE_RECIPIENT -> {
                startActivity(Intent(requireContext(), ServicesActivity::class.java))
            }
        }
    }

    // Deeplink  redirecting to Search Patient
    private fun isDeeplink(isDeepLink: Boolean?) {
        if (isDeepLink == true) {
            val bundle = Bundle().apply {
                putString(DefinedParams.ORIGIN, MenuConstants.MY_PATIENTS_MENU_ID)
            }
            val intent = if (CommonUtils.isCommunity()) {
                Intent(
                    requireContext(),
                    FollowUpMyPatientActivity::class.java,
                )
            } else {
                Intent(requireContext(), PatientSearchActivity::class.java)
            }

            intent.putExtras(bundle)
            if (CommonUtils.isCommunity()) {
                startActivity(intent)
            } else {
                startActivity(intent)
            }
        }
    }
}
