package com.nighthelper.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nighthelper.app.MainViewModel
import com.nighthelper.app.R
import com.nighthelper.app.data.DayRecord
import com.nighthelper.app.ui.theme.Hairline
import com.nighthelper.app.ui.theme.Lavender
import com.nighthelper.app.ui.theme.MintSoft
import com.nighthelper.app.ui.theme.MoonGold
import com.nighthelper.app.ui.theme.TextDim
import com.nighthelper.app.util.toFa
import com.nighthelper.app.util.toJalaliFa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {
    val history by vm.history.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = null,
                            tint = Lavender
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.history_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(MoonGold.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_moon),
                        contentDescription = null,
                        tint = MoonGold,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.history_empty),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.history_empty_sub),
                    fontSize = 14.sp,
                    color = TextDim
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 20.dp, vertical = 8.dp
                )
            ) {
                items(history, key = { it.date }) { record ->
                    DayCard(record = record)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DayCard(record: DayRecord) {
    val ratio = if (record.totalItems > 0) {
        record.completedItems.toFloat() / record.totalItems.toFloat()
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = record.date.toJalaliFa(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (record.closedAt != null) {
                        stringResource(R.string.history_closed_fmt)
                    } else {
                        stringResource(R.string.history_open_fmt)
                    },
                    fontSize = 11.sp,
                    color = if (record.closedAt != null) MintSoft else TextDim
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Hairline.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ratio)
                        .height(8.dp)
                        .background(MoonGold, RoundedCornerShape(4.dp))
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.history_completed_fmt,
                    record.completedItems.toFa(),
                    record.totalItems.toFa()
                ),
                fontSize = 13.sp,
                color = Lavender,
                fontWeight = FontWeight.Medium
            )
            record.gratitude?.let { note ->
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_heart),
                        contentDescription = null,
                        tint = MintSoft,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = note,
                        fontSize = 13.sp,
                        color = TextDim
                    )
                }
            }
            if (record.missedTitles.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.history_missed_fmt,
                        record.missedTitles.joinToString("، ")
                    ),
                    fontSize = 12.sp,
                    color = TextDim.copy(alpha = 0.75f)
                )
            }
        }
    }
}
