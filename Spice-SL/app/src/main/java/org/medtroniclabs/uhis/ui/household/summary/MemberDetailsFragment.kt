package org.medtroniclabs.uhis.ui.household.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.databinding.FragmentMemberDetailsBinding
import org.medtroniclabs.uhis.ui.household.viewmodel.MemberSummaryViewModel

class MemberDetailsFragment : Fragment() {
    private lateinit var binding: FragmentMemberDetailsBinding
    private val memberSummaryViewModel: MemberSummaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMemberDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        attachObservers()
    }

    private fun attachObservers() {
    }
}
