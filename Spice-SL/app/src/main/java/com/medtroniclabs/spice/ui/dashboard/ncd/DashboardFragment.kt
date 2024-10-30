package com.medtroniclabs.spice.ui.dashboard.ncd

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.CardViewLayoutBinding
import com.medtroniclabs.spice.databinding.FragmentDashboardBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var cgCalender: TagListCustomView
    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        initializeChipItem()
        initializeCardView()
    }

    private fun initializeCardView() {
        val cardList = arrayListOf<Map<String, String>>()
        cardList.forEach { cardId ->
            cardId.let {
                val binding = CardViewLayoutBinding.inflate(LayoutInflater.from(context))
                binding.root.tag = it[DefinedParams.NAME]
                binding.txTitle.text = it[DefinedParams.NAME]
                binding.txCount.text = it[DefinedParams.ID]
                this.binding.dashboard.addView(binding.root)
            }
        }
    }

    private fun initializeChipItem() {
        val chipItemList = ArrayList<ChipViewItemModel>()
        cgCalender = TagListCustomView(requireContext(), binding.cgCalender) { _, _, _ ->
            val isVisible =
                cgCalender.getSelectedTags().any { it.name == getString(R.string.customize) }
            if (isVisible) {
                binding.clDateRange.visible()
            } else {
                binding.etFromDate.text = ""
                binding.etToDate.text = ""
                binding.clDateRange.visibility = View.GONE
            }
        }
        cgCalender.addChipItemList(chipItemList)
    }

    private fun initializeView() {
        binding.etFromDate.safeClickListener(this)
        binding.etToDate.safeClickListener(this)
    }

    companion object {
        const val TAG = "DashboardFragment"
        fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }

    private fun showDatePickerDialog(isFromDate: Boolean, text: String?) {
        var date: Triple<Int?, Int?, Int?>? = null
        if (!text.isNullOrBlank())
            date = DateUtils.convertedMMMToddMM(text)
        val minMaxDate = getMinDate(isFromDate)
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                date = date,
                minDate = minMaxDate.first,
                cancelCallBack = { datePickerDialog = null }) { _, year, month, dayOfMonth ->
                DateUtils.convertDateTimeToDate(
                    "$dayOfMonth-$month-$year",
                    DateUtils.DATE_FORMAT_ddMMyyyy,
                    DateUtils.DATE_ddMMyyyy
                ).let { stringDate ->
                    if (isFromDate) {
                        binding.etFromDate.text = stringDate
                        binding.etToDate.text = ""
                    } else {
                        binding.etToDate.text = stringDate
                    }
                }
                datePickerDialog = null
            }
        }
    }

    private fun getMinDate(isFromDate: Boolean): Pair<Long?, Long?> {
        val fromDate = binding.etFromDate.text?.toString()
        val toDate = binding.etToDate.text?.toString()
        return if (isFromDate) {
            if (!toDate.isNullOrBlank())
                Pair(null, DateUtils.convertDateToLong(toDate, DateUtils.DATE_ddMMyyyy))
            else Pair(null, System.currentTimeMillis())
        } else {
            if (!fromDate.isNullOrBlank())
                Pair(
                    DateUtils.convertDateToLong(fromDate, DateUtils.DATE_ddMMyyyy),
                    System.currentTimeMillis()
                )
            else Pair(null, System.currentTimeMillis())
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.etFromDate.id -> {
                showDatePickerDialog(true, binding.etFromDate.toString())
            }

            binding.etToDate.id -> {
                if (binding.etFromDate.text.toString().isNotEmpty()) {
                    showDatePickerDialog(false, binding.etToDate.text.toString())
                }
            }
        }
    }
}