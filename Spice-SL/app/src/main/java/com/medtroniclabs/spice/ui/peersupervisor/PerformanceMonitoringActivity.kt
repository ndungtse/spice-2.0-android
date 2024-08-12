package com.medtroniclabs.spice.ui.peersupervisor

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.paging.LoadState
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.getFirstAndLastDateOfMonth
import com.medtroniclabs.spice.appextensions.getLongTime
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.hideView
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.appextensions.showView
import com.medtroniclabs.spice.appextensions.toString
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.getDatePatternDDMMYYYY
import com.medtroniclabs.spice.common.DateUtils.getTimeInMilliFromDate
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.performance.CheckBoxSpinnerData
import com.medtroniclabs.spice.data.performance.Preference
import com.medtroniclabs.spice.databinding.ActivityPerformanceMonitoringBinding
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.peersupervisor.adapter.CheckBoxSpinnerAdapter
import com.medtroniclabs.spice.ui.peersupervisor.adapter.PerformanceMonitoringAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.Calendar


@AndroidEntryPoint
class PerformanceMonitoringActivity : BaseActivity() {

    lateinit var binding: ActivityPerformanceMonitoringBinding

    private val viewModel: PerformanceMonitoringViewModel by viewModels()
    private lateinit var adapter: PerformanceMonitoringAdapter
    private lateinit var yearsAdapter: CustomSpinnerAdapter
    private lateinit var monthsAdapter: CustomSpinnerAdapter

    private lateinit var chwFilterAdapter: CheckBoxSpinnerAdapter
    private lateinit var villageFilterAdapter: CheckBoxSpinnerAdapter
    private var shouldTriggerYearSpinner = true
    private var shouldTriggerMonthSpinner = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerformanceMonitoringBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.performance_monitoring),
        )
        initView()
        setViewListener()
        initObserver()
    }

    private fun setFilterValueForDates(preference: Preference) {
        viewModel.anyFilterChanged.value = false
        val yearMonth = preference.getYearAndMonth()
        val fromToDate = preference.getFromToDate()

        shouldTriggerYearSpinner = false
        shouldTriggerMonthSpinner = false
        //Set Year
        yearMonth.first.let { year ->
            val index = LocalDate.now().year - year
            binding.yearSpinner.setSelection(index)
            viewModel.updateFilter(year = year)
        }

        yearMonth.second.let { month ->
            binding.monthSpinner.setSelection(month)
            viewModel.updateFilter(month = month)
        }

        binding.etFromDate.text = fromToDate.first.toString(DATE_ddMMyyyy)
        binding.etEndDate.text = fromToDate.second.toString(DATE_ddMMyyyy)
        setMinAndMaxDate()
    }

    private fun initObserver() {
        viewModel.userFilterPreferenceLiveData.observe(this) {
            when (it.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    it.data?.preference?.let { pref ->
                        viewModel.updateUserPreference(pref)
                        viewModel.getLinkedChwDetails()
                        setFilterValueForDates(pref)
                        viewModel.initPagination()
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    Toast.makeText(
                        this,
                        "Something went wrong in fetching user preference",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.saveUserFilterPreferenceLiveData.observe(this) {
            when (it.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                   // hideLoading()
                    it.data?.preference?.let { pref ->
                        viewModel.updateUserPreference(pref)
                    }
                    Toast.makeText(this, "Saved Filter Preference Successfully", Toast.LENGTH_LONG)
                        .show()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    Toast.makeText(
                        this,
                        "Something went wrong in Saved Filter Preference",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.dataFlow.observe(this) {
            adapter.submitData(lifecycle, it)
        }


        viewModel.filterChwListLiveData.observe(this) {
            when (it.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    viewModel.updateChwFilterListLiveData()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    Toast.makeText(
                        this,
                        "Something went wrong in fetching village",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }

        viewModel.chwFilterListLiveData.observe(this) {
            chwFilterAdapter.updateData(it)
            binding.chwSpinner.isEnabled = !it.isNullOrEmpty()
            viewModel.updateVillageListLiveData(it.filter { item -> item.isSelected })
        }

        viewModel.villageFilterListLiveData.observe(this) {
            villageFilterAdapter.updateData(it)
            binding.villageSpinner.isEnabled = !it.isNullOrEmpty()
        }

        viewModel.anyFilterChanged.observe(this) {
            binding.btnApply.isEnabled = it
        }
    }

    private fun setViewListener() {
        binding.ivFilter.setOnClickListener {
            if (binding.clFilterView.isVisible()) {
                binding.clFilterView.hideView()
            } else {
                binding.clFilterView.showView()
            }
        }

        binding.ivRefreshPage.setOnClickListener {
            if (connectivityManager.isNetworkAvailable()) {
                viewModel.initPagination()
            } else {
                showErrorDialogue(
                    getString(R.string.title_no_network),
                    getString(R.string.message_no_network),
                    isNegativeButtonNeed = false
                ) { _ -> }
            }
        }

        binding.flLockButton.setOnClickListener {
            if (connectivityManager.isNetworkAvailable()) {
                validateFilterInputs(true)
            } else {
                showErrorDialogue(
                    getString(R.string.title_no_network),
                    getString(R.string.message_no_network),
                    isNegativeButtonNeed = false
                ) { _ -> }
            }
        }

        binding.btnCancel.setOnClickListener {
            viewModel.userPreference?.let {
                setFilterValueForDates(it)
                viewModel.updateChwFilterListLiveData()
            }
        }

        binding.btnApply.setOnClickListener {
            if (connectivityManager.isNetworkAvailable()) {
                validateFilterInputs()
            } else {
                showErrorDialogue(
                    getString(R.string.title_no_network),
                    getString(R.string.message_no_network),
                    isNegativeButtonNeed = false
                ) { _ -> }
            }
        }

        binding.yearSpinner.onItemSelectedListener = yearSpinnerChangeListener
        binding.monthSpinner.onItemSelectedListener = monthSpinnerChangeListener

        binding.etFromDate.setOnClickListener {
            val yearMonthWeek = if (binding.etFromDate.text.isNotEmpty()) {
                DateUtils.getYearMonthAndDate(binding.etFromDate.text.toString())
            } else null
            showDatePicker(
                context = this,
                disableFutureDate = false,
                minDate = viewModel.filterModel.startDate,
                maxDate = viewModel.filterModel.endDate,
                date = yearMonthWeek
            ) { _, year, month, dayOfMonth ->
                viewModel.anyFilterChanged.value = true
                resetToDate()
                val stringDate = "$dayOfMonth-$month-$year"
                val parsedDate = getDatePatternDDMMYYYY().parse(stringDate)
                parsedDate?.let {
                    binding.etFromDate.text = DateUtils.getDateDDMMYYYY().format(it)
                    viewModel.updateFilter(fromDate = DateUtils.getDateDDMMYYYY().format(it))
                }
            }

        }

        binding.etEndDate.setOnClickListener {
            val yearMonthWeek = if (binding.etEndDate.text.isNotEmpty()) {
                DateUtils.getYearMonthAndDate(binding.etEndDate.text.toString())
            } else null
            var minDate: Long? = null
            if (viewModel.filterModel.fromDate != null) {
                minDate = getTimeInMilliFromDate(viewModel.filterModel.fromDate!!)
            } else {
                minDate = viewModel.filterModel.startDate
            }
            showDatePicker(
                context = this,
                disableFutureDate = false,
                minDate = minDate,
                maxDate = viewModel.filterModel.endDate,
                date = yearMonthWeek,
            ) { _, year, month, dayOfMonth ->
                viewModel.anyFilterChanged.value = true
                val stringDate = "$dayOfMonth-$month-$year"
                val parsedDate = getDatePatternDDMMYYYY().parse(stringDate)
                parsedDate?.let {
                    binding.etEndDate.text = DateUtils.getDateDDMMYYYY().format(it)
                    viewModel.updateFilter(toDate = DateUtils.getDateDDMMYYYY().format(it))
                }
            }

        }
    }

    private fun validateFilterInputs(shouldSave: Boolean = false) {
       val fromDate = binding.etFromDate.text.toString()
        if (fromDate.isNullOrEmpty()) {
            binding.tvInvalidFilterInputs.visible()
            return
        }

        val toDate = binding.etEndDate.text.toString()
        if (toDate.isNullOrEmpty()) {
            binding.tvInvalidFilterInputs.visible()
            return
        }

        val selectedCHWs = chwFilterAdapter.getSelectedItems()
        if (selectedCHWs.isNullOrEmpty()){
            binding.tvInvalidFilterInputs.visible()
            return
        }

        val selectedVillages = villageFilterAdapter.getSelectedItems()
        if (selectedVillages.isNullOrEmpty()) {
            binding.tvInvalidFilterInputs.visible()
            return
        }

        binding.tvInvalidFilterInputs.gone()

        val serverFromDate = DateUtils.convertDateFormat(fromDate, DATE_ddMMyyyy, DateUtils.DATE_FORMAT_yyyyMMdd)
        val serverToDate = DateUtils.convertDateFormat(toDate, DATE_ddMMyyyy, DateUtils.DATE_FORMAT_yyyyMMdd)

        viewModel.updatePaginationWithNewFilter(
            serverFromDate,
            serverToDate,
            selectedCHWs.filter { it.id != 0L }.map { it.id },
            selectedVillages.filter { it.id != 0L }.map { it.id },
            shouldSave
        )
    }

    private val yearSpinnerChangeListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {

            val selectedItem =
                (binding.yearSpinner.adapter as CustomSpinnerAdapter).getData(position = position)
            selectedItem?.let {
                val selectedId = it[DefinedParams.ID]
                if (selectedId is Int) {
                    viewModel.updateFilter(year = selectedId)
                    if (shouldTriggerYearSpinner) {
                        resetMinDateMaxDate()
                        setMinAndMaxDate()
                    }
                    shouldTriggerYearSpinner = true
                }
            }
            viewModel.anyFilterChanged.value = true

        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }

    private val monthSpinnerChangeListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            val selectedItem =
                (binding.monthSpinner.adapter as CustomSpinnerAdapter).getData(position = position)
            selectedItem?.let {
                val selectedId = it[DefinedParams.ID]
                if (selectedId is Int) {
                    viewModel.updateFilter(month = selectedId)
                    if (shouldTriggerMonthSpinner) {
                        resetMinDateMaxDate()
                        setMinAndMaxDate()
                    }
                    shouldTriggerMonthSpinner = true
                }
            }
            viewModel.anyFilterChanged.value = true
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
    }

    private fun resetMinDateMaxDate() {
        binding.etFromDate.text = ""
        viewModel.updateFilter(startDate = null, endDate = null, fromDate = null)
        resetToDate()
    }

    private fun resetToDate() {
        binding.etEndDate.text = ""
        viewModel.updateFilter(toDate = null)
    }

    private fun setMinAndMaxDate() {
        val year = viewModel.filterModel.year
        val month = viewModel.filterModel.month
        if (year != null && month != null) {
            val calendar = Calendar.getInstance()

            // Set the year and month
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)

            // Set to the first day of the month
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startDate = calendar.timeInMillis

           // binding.etFromDate.text = DateUtils.getDateDDMMYYYY().format(startDate)

            // Get the last day of the month
            val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            calendar.set(Calendar.DAY_OF_MONTH, lastDay)
            val endDate = calendar.timeInMillis
           // binding.etEndDate.text = DateUtils.getDateDDMMYYYY().format(endDate)

            viewModel.updateFilter(startDate = startDate, endDate = endDate)
        }
    }

    private fun initView() {
        adapter = PerformanceMonitoringAdapter()
        binding.rvPerformanceList.adapter = adapter
        adapter.addLoadStateListener { loadState ->
           if (loadState.refresh is LoadState.Loading)
               showLoading()
            else
                hideLoading()

           if (loadState.append is LoadState.Loading)
               binding.paginationLoader.visible()
            else
                binding.paginationLoader.gone()

        }

        chwFilterAdapter = CheckBoxSpinnerAdapter(this, mutableListOf(), chwSpinnerListener)
        binding.chwSpinner.adapter = chwFilterAdapter

        villageFilterAdapter = CheckBoxSpinnerAdapter(this, mutableListOf(), villageSpinnerListener)
        binding.villageSpinner.adapter = villageFilterAdapter

        yearsAdapter = CustomSpinnerAdapter(this@PerformanceMonitoringActivity, false)
        yearsAdapter.setData(viewModel.getYearList())
        binding.yearSpinner.adapter = yearsAdapter

        monthsAdapter = CustomSpinnerAdapter(this@PerformanceMonitoringActivity, false)
        monthsAdapter.setData(viewModel.getMonthList())
        binding.monthSpinner.adapter = monthsAdapter
    }

    private val chwSpinnerListener = object : CheckBoxSpinnerAdapter.OnCheckBoxSpinnerListener {
        override fun onCheckBoxSpinnerItemClick(selectedItems: List<CheckBoxSpinnerData>) {
             viewModel.anyFilterChanged.value = true
             viewModel.updateVillageListLiveData(selectedItems, false)
        }
    }

    private val villageSpinnerListener = object : CheckBoxSpinnerAdapter.OnCheckBoxSpinnerListener {
        override fun onCheckBoxSpinnerItemClick(selectedItems: List<CheckBoxSpinnerData>) {
            viewModel.anyFilterChanged.value = true
            val selectedVillageCHWs = selectedItems.filter { it.id != 0L }.map { it.chwId }
            chwFilterAdapter.updateSelectedItems(selectedVillageCHWs)
        }
    }

    private fun showDatePicker(
        context: Context,
        disableFutureDate: Boolean = false,
        minDate: Long? = null,
        maxDate: Long? = null,
        date: Triple<Int?, Int?, Int?>? = null,
        cancelCallBack: (() -> Unit)? = null,
        callBack: (dialog: DatePicker, year: Int, month: Int, dayOfMonth: Int) -> Unit,
    ): DatePickerDialog {

        val calendar = Calendar.getInstance()
        var thisYear = calendar.get(Calendar.YEAR)
        var thisMonth = calendar.get(Calendar.MONTH)
        var thisDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dialog: DatePickerDialog?

        if (date?.first != null && date.second != null && date.third != null) {
            thisYear = date.first!!
            thisMonth = date.second!!
            thisDay = date.third!!
        }

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
                callBack.invoke(datePicker, year, month + 1, dayOfMonth)
            }

        dialog = DatePickerDialog(
            context,
            dateSetListener,
            thisYear,
            thisMonth,
            thisDay
        )

        if (cancelCallBack != null) {
            dialog.setOnCancelListener {
                cancelCallBack.invoke()
            }
        }

        minDate?.let {
            dialog.datePicker.minDate = it
        }
        maxDate?.let {
            dialog.datePicker.maxDate = it
        }

        if (disableFutureDate) dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.setCancelable(false)

        dialog.show()

        return dialog

    }

}