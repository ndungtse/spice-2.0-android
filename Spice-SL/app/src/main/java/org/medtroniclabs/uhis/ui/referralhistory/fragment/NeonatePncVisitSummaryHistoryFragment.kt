package org.medtroniclabs.uhis.ui.referralhistory.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.medtroniclabs.uhis.databinding.FragmentReferralTicketBinding

class NeonatePncVisitSummaryHistoryFragment : Fragment() {
    lateinit var binding: FragmentReferralTicketBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentReferralTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "NeonatePncVisitSummaryHistoryFragment"

        fun newInstance(): NeonatePncVisitSummaryHistoryFragment = NeonatePncVisitSummaryHistoryFragment()
    }
}
