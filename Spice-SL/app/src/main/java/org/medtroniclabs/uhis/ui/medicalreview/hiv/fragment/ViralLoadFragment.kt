package org.medtroniclabs.uhis.ui.medicalreview.hiv.fragment

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
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.data.model.ViralLoadResponse
import org.medtroniclabs.uhis.model.ViralLoadRecord
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.medicalreview.hiv.viewmodel.HivViewModel

@AndroidEntryPoint
class ViralLoadFragment : BaseFragment() {
    private val viewModel: HivViewModel by viewModels()
    private var viralLoadRecordsState = mutableStateOf<List<ViralLoadRecord>>(emptyList())

    // viewModel.isViralLoad true means Viral Load fragment false means ART regimen fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ViralLoadTable(records = viralLoadRecordsState.value)
                }
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getViralLoadData(
            arguments?.getString(DefinedParams.PatientReference),
            arguments?.getString(DefinedParams.MemberReference),
        )
        setupObserver()
    }

    private fun setupObserver() {
        viewModel.getViralLoadLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                }

                ResourceState.ERROR -> {
                }

                ResourceState.SUCCESS -> {
                    val records = resourceState.data.orEmpty()

                    val responseList = if (records.isNullOrEmpty()) {
                        listOf(
                            ViralLoadRecord(
                                getString(R.string._1st),
                                getString(R.string.empty__),
                                getString(R.string.empty__),
                                getString(R.string.empty__),
                            ),
                        )
                    } else {
                        records.mapIndexed { index, record ->
                            if (record.collectionDate.isNullOrBlank() ||
                                record.gestationAtCollection.isNullOrBlank() ||
                                record.result.isNullOrBlank()
                            ) {
                                createPlaceholderRecord(getString(R.string._1st))
                            } else {
                                record.toViralLoadRecord(index + 1)
                            }
                        }
                    }

                    viralLoadRecordsState.value = responseList
                }

                ResourceState.SUCCESS -> {
                    val recordList = resourceState.data

                    if (!recordList.isNullOrEmpty()) {
                        viralLoadRecordsState.value = recordList.mapIndexed { index, record ->
                            if (record.collectionDate.isNullOrBlank() ||
                                record.gestationAtCollection.isNullOrBlank() ||
                                record.result.isNullOrBlank()
                            ) {
                                createPlaceholderRecord(getString(R.string._1st))
                            } else {
                                record.toViralLoadRecord(index + 1)
                            }
                        }
                    } else {
                        viralLoadRecordsState.value = listOf(
                            ViralLoadRecord(
                                getString(R.string._1st),
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
    fun ViralLoadTable(records: List<ViralLoadRecord>?) {
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
                        text = stringResource(R.string.viral_load),
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
                            stringResource(R.string.viral_load),
                            stringResource(R.string.collection_date),
                            stringResource(R.string.gestation_at_collection_date),
                            stringResource(R.string.results),
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
                    var data =
                        listOf(
                            record.label,
                            DateUtils.formatDateToDDMMYYYY(record.collectionDate.toString()),
                            record.gestationAtDate.toString(),
                            record.result ?: requireContext().getString(R.string.empty__),
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
        const val TAG = "ViralLoadFragment"
    }

    private fun getOrdinal(number: Int): String =
        when {
            number % 100 in 11..13 -> "${number}th"
            number % 10 == 1 -> "${number}st"
            number % 10 == 2 -> "${number}nd"
            number % 10 == 3 -> "${number}rd"
            else -> "${number}th"
        }

    // Extension function to map ViralLoadResponse to ViralLoadRecord with label ordinal
    private fun ViralLoadResponse.toViralLoadRecord(position: Int): ViralLoadRecord =
        ViralLoadRecord(
            label = getOrdinal(position),
            collectionDate = collectionDate!!,
            gestationAtDate = gestationAtCollection!!,
            result = result!!,
        )

    // Helper to create placeholder record with hyphens and given label
    private fun createPlaceholderRecord(label: String) =
        ViralLoadRecord(
            label = label,
            collectionDate = getString(R.string.empty__),
            gestationAtDate = getString(R.string.empty__),
            result = getString(R.string.empty__),
        )

    fun refreshFragment(
        patientReference: String?,
        memberReference: String?,
    ) {
        viewModel.getViralLoadData(patientReference, memberReference)
    }
}
