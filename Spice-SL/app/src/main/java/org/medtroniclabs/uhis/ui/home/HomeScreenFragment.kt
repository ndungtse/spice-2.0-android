package org.medtroniclabs.uhis.ui.home

import android.content.Intent
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.medtroniclabs.microcoaching.Language
import com.medtroniclabs.microcoaching.MicroCoachingSDK
import com.medtroniclabs.microcoaching.ui.chat.CoachingChatBottomSheet
import com.medtroniclabs.microcoaching.ui.components.ChatFab
import com.medtroniclabs.microcoaching.ui.components.LearnCard
import com.medtroniclabs.microcoaching.ui.components.MorningCard
import com.medtroniclabs.microcoaching.ui.flow.CoachingFlowActivity
import com.medtroniclabs.microcoaching.ui.learn.modules.QuickLearnViewModel
import com.medtroniclabs.microcoaching.ui.learn.modules.bottomsheet.RefresherBottomSheet
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

    private val chwId: String
        get() = runCatching { SecuredPreference.getUserId().toString() }.getOrDefault("")

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
     * Wire up the MicroCoaching SDK surfaces on the home screen:
     *   1. LearnCard banner pinned above the menu grid — shows the gap-prioritised
     *      morning module with Start / Skip actions.
     *   2. CHW AI chat FAB at bottom-right (opens chat in a bottom sheet).
     *
     * The banner collapses to zero height when `morningModules` is empty.
     * Tapping Start opens the v3 Learn flow; tapping Skip dismisses the card
     * for the current session.
     */
    private fun setupCoachingSurfaces() {
        if (!MicroCoachingSDK.isInitialized()) return
        val sdk = MicroCoachingSDK.getInstance()

        sdk.onHomeScreenShown(chwId)

        // ── MorningCard banner (above grid) ───────────────────────────────
        // Uses MorningCard (W6) for all modules — shows card count + quiz count.
        // Start always launches the full lesson → quiz flow (cards-first per decision doc).
        // Legacy LearnCard kept as fallback for modules without card content.
        binding.coachingCardBanner.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MicroCoachingTheme {
                    val top = sdk.getSelectedMorningModule()
                    // sdkDismissed is set when the CHW completes or skips from the sheet;
                    // resets on each onHomeScreenShown call (new session / re-open).
                    val sdkDismissed by sdk.morningRefresherDismissed.collectAsState()
                    // Local dismissed handles the session-only skip (tap Skip on the card).
                    var localDismissed by remember { mutableStateOf(false) }

                    val dismissed = sdkDismissed || localDismissed

                    // QuickLearnViewModel provides the wrong-question count for the label.
                    val morningVm: QuickLearnViewModel = viewModel(
                        factory = QuickLearnViewModel.factory(
                            androidx.compose.ui.platform.LocalContext.current.applicationContext,
                            chwId,
                        ),
                    )
                    val wrongCount by morningVm.wrongQuestionCount.collectAsState()

                    // Populate wrongQuestionCount for the label whenever the top module changes.
                    LaunchedEffect(top?.moduleId) {
                        morningVm.computeWrongQuestionCount()
                    }

                    if (top != null && !dismissed) {
                        val title = if (sdk.config.language == Language.ENGLISH) {
                            top.titleEn ?: top.titleBn
                        } else {
                            top.titleBn
                        }
                        // Effective question count: wrong answers if any; total otherwise.
                        val effectiveQuestionCount = if (wrongCount > 0) wrongCount else top.questionCount
                        val hasCards = top.cardCount > 0

                        val onSkip: () -> Unit = {
                            localDismissed = true
                            sdk.dismissMorningRefresher()
                        }
                        val onStart: () -> Unit = {
                            RefresherBottomSheet.show(
                                parentFragmentManager,
                                chwId,
                                fromHomeScreen = true,
                                entryMode = RefresherBottomSheet.EntryMode.QUESTION_FIRST,
                            )
                        }

                        if (hasCards) {
                            MorningCard(
                                moduleTitle = title,
                                cardCount = top.cardCount,
                                questionCount = effectiveQuestionCount,
                                estimatedMinutes = top.estimatedMinutes,
                                onStart = onStart,
                                onSkip = onSkip,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        } else {
                            LearnCard(
                                moduleTitle = title,
                                questionCount = effectiveQuestionCount,
                                estimatedMinutes = top.estimatedMinutes,
                                onStart = onStart,
                                onSkip = onSkip,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
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
     * Otherwise, prompt the CHW to download it (~800 MB) — same dialog flow as
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
        // Open the chat sheet immediately so the CHW lands on a screen that
        // shows live download progress — replaces the earlier Toast which was
        // confusing because tapping the FAB again landed on the chat sheet
        // *before* it had observed the in-flight ModelState (see
        // ChatViewModel.currentModelNotReadyState for the race fix).
        CoachingChatBottomSheet.show(parentFragmentManager)
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
        val items = if (MicroCoachingSDK.isInitialized() &&
            menuEntity.none { it.menuId.equals(MenuConstants.COACHING_MENU_ID, ignoreCase = true) }
        ) {
            val isBangla = MicroCoachingSDK.getInstance().config.language == Language.BANGLA
            menuEntity + MenuEntity(
                id = -1L,
                menuId = MenuConstants.COACHING_MENU_ID,
                name = "Coaching",
                displayValue = if (isBangla) "কোচিং" else null,
                displayOrder = menuEntity.size,
            )
        } else {
            menuEntity
        }
        binding.rvActivitiesList.adapter = DashboardMenuItemsAdapter(items, this)
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

            MenuConstants.COACHING_MENU_ID -> {
                if (MicroCoachingSDK.isInitialized()) {
                    CoachingFlowActivity.launchLearn(requireContext(), chwId)
                }
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
