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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.label
import com.medtroniclabs.spice.model.ViralLoadRecord
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViralLoadFragment : BaseFragment() {

    private val viewModel: HivViewModel by  viewModels()

    // viewModel.isViralLoad true means Viral Load fragment false means ART regimen fragment
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return ComposeView(requireContext()).apply {
                setContent {
                    MaterialTheme {
                        val data = listOf(
                            ViralLoadRecord(null, null, null, null),
                            ViralLoadRecord(null, null, null, null),
                            ViralLoadRecord(null, null, null, null)
                        )
                        ViralLoadTable(records = data)
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.isViralLoad = arguments?.getBoolean(DefinedParams.VIRAL_LOAD,false) == true
    }

    @Composable
    fun ViralLoadTable(records: List<ViralLoadRecord>?) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier
                .background(Color.White)) {
                // Card Heading
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(id = R.color.card_background))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                         contentAlignment = Alignment.Center

                ) {
                    Text(
                        text = if (viewModel.isViralLoad) "Viral Load" else "ART Regimen",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontFamily = FontFamily(Font(R.font.inter_bold))
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
                                topEnd = 4.dp
                            )
                        ) // Only top corners curved
                        .background(Color(0xFFE6DDF3))
                        .border(
                            1.dp,
                            colorResource(id = R.color.edittext_stroke),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                ) {

                    val headers = if (viewModel.isViralLoad) {
                        listOf(
                            stringResource(R.string.viral_load),
                            stringResource(R.string.collection_date),
                            stringResource(R.string.gestation_at_collection_date),
                            stringResource(R.string.results)
                        )
                    } else {
                        listOf(
                            "START DATE",
                            "END DATE",
                            "REGIMEN",
                            "REGIMEN LINE",
                            "REASONS FOR CHANGE"
                        )
                    }
                    headers.forEach { label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(.2.dp, colorResource(id = R.color.edittext_stroke))
                                .padding(6.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center

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

                    Row(
                        modifier = Modifier
                            .absolutePadding(
                                left = 16.dp,
                                bottom = if (isLastRow) 12.dp else 0.dp,
                                right = 16.dp
                            )
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min) // ensure full height row
                            .background(colorResource(id = R.color.table_row_color))
                    ) {
                        listOf(
                            record.label,
                            record.collectionDate ?: "--",
                            record.gestationAtDate ?: "--",
                            record.result ?: "--"
                        ).forEachIndexed { cellIndex, cell ->

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
                                        shape = cellShape
                                    )
                                    .background(colorResource(id = R.color.table_row_color))
                                    .padding(6.dp)
                            ) {
                                if (cell != null) {
                                    Text(
                                        text = cell,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily(Font(R.font.inter_regular))
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ViralLoadTablePreview() {
        val sampleData = listOf(
            ViralLoadRecord(null, null, null, null),
            ViralLoadRecord(null, null, null, null),
            ViralLoadRecord(null, null, null, null)
        )

        ViralLoadTable(records = sampleData)
    }
    companion object {
        const val TAG = "ViralLoadFragment"
        const val TAG_ART = "ARTRegimenFragment"
    }
}