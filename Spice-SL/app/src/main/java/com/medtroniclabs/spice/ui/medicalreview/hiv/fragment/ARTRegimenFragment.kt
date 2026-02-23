package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.MemberID
import com.medtroniclabs.spice.model.ARTLoadRecord
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ARTRegimenFragment : BaseFragment() {
    private val viewModel: HivViewModel by viewModels()
    private var aRTRecordsState = mutableStateOf<List<ARTLoadRecord>>(emptyList())

    // viewModel.isViralLoad true means Viral Load fragment false means ART regimen fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ARTLoadTable(records = aRTRecordsState.value)
                }
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getARTData(
            arguments?.getString(DefinedParams.ID),
            limit = 5,
            category = DefinedParams.HIV,
            isActive = true,
            memberId = arguments?.getString(MemberID),
        )
        setupObserver()
    }

    private fun setupObserver() {
        viewModel.getARTLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    // Handle loading state if needed
                }

                ResourceState.ERROR -> {
                    // Handle error state if needed
                }

                ResourceState.SUCCESS -> {
                    val recordList = resourceState.data

                    if (!recordList.isNullOrEmpty()) {
                        val responseList = recordList.map { record ->
                            val startDate = DateUtils.formatDateToDDMMYYYY(record.prescribedSince)
                            val endDate = DateUtils.formatDateToDDMMYYYY(record.endDate)
                            val regimen = record.medicationName
                            val regimenLine = record.regimenLine
                            val reasonForChange = record.reasonsForChange
                            ARTLoadRecord(
                                startDate,
                                endDate,
                                regimen,
                                regimenLine ?: getString(R.string.empty__),
                                reasonForChange ?: getString(R.string.empty__),
                            )
                        }
                        aRTRecordsState.value = responseList
                    } else {
                        aRTRecordsState.value = listOf(
                            ARTLoadRecord(
                                getString(R.string.empty__),
                                getString(R.string.empty__),
                                getString(R.string.empty__),
                                getString(R.string.empty__),
                                getString(R.string.empty__),
                            ),
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ARTLoadTable(records: List<ARTLoadRecord>?) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White),
            ) {
                // Card Heading
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(id = R.color.card_background))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.current_medication),
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontFamily = FontFamily(Font(R.font.inter_bold)),
                    )
                }

                // Table Header Row
                Row(
                    Modifier
                        .absolutePadding(left = 16.dp, top = 16.dp, right = 16.dp)
                        .height(IntrinsicSize.Min) // ensures cells match tallest content
                        .clip(
                            RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 4.dp,
                            ),
                        ) // Only top corners curved
                        .background(Color(0xFFE6DDF3))
                        .border(
                            1.dp,
                            colorResource(id = R.color.edittext_stroke),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                        ),
                ) {
                    val headers =
                        listOf(
                            stringResource(R.string.start_date).uppercase(),
                            stringResource(R.string.end_date).uppercase(),
                            stringResource(R.string.regimen),
                            stringResource(R.string.regimen_line).uppercase(),
                            stringResource(R.string.reasons_for_change),
                        )

                    headers.forEach { label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(.2.dp, colorResource(id = R.color.edittext_stroke))
                                .padding(6.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.inter_medium)),
                            )
                        }
                    }
                }

                // Data Rows
                records?.forEachIndexed { rowIndex, record ->
                    val isLastRow = rowIndex == records.lastIndex
                    var data = listOf(
                        record.startDate,
                        record.endDate ?: "--",
                        record.regimen ?: "--",
                        record.regimenLine ?: "--",
                        record.reasonForChange ?: "--",
                    )

                    Row(
                        modifier = Modifier
                            .absolutePadding(
                                left = 16.dp,
                                bottom = if (isLastRow) 12.dp else 0.dp,
                                right = 16.dp,
                            ).fillMaxWidth()
                            .height(IntrinsicSize.Min) // ensure full height row
                            .background(colorResource(id = R.color.table_row_color)),
                    ) {
                        data.forEachIndexed { cellIndex, cell ->

                            val isLastCell = cellIndex == 3
                            val isFirstCell = cellIndex == 0

                            val cellShape = when {
                                isLastRow && isFirstCell -> RoundedCornerShape(bottomStart = 4.dp)
                                isLastRow && isLastCell -> RoundedCornerShape(bottomEnd = 4.dp)
                                else -> RoundedCornerShape(0.dp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(cellShape)
                                    .border(
                                        0.2.dp,
                                        colorResource(id = R.color.edittext_stroke),
                                        shape = cellShape,
                                    ).background(colorResource(id = R.color.table_row_color))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (cell != null) {
                                    Text(
                                        text = cell,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "ARTRegimenFragment"
    }

    private fun getOrdinal(number: Int): String =
        when {
            number % 100 in 11..13 -> "${number}th"
            number % 10 == 1 -> "${number}st"
            number % 10 == 2 -> "${number}nd"
            number % 10 == 3 -> "${number}rd"
            else -> "${number}th"
        }

    fun refreshFragment(
        patientReference: String?,
        id: String?,
    ) {
        viewModel.getARTData(
            id,
            limit = 5,
            category = DefinedParams.HIV,
            isActive = true,
            memberId = arguments?.getString(MemberID),
        )
    }
}
