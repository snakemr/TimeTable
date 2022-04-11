package me.demo.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

@Composable
fun Table(data: MainViewModel) = BoxWithConstraints(Modifier.fillMaxSize()) {
    val days = remember { listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс") }
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM") }
    val visibleColumns by remember { derivedStateOf { if (data.inTabletMode) 5 else if (data.inPhoneMode) 1 else 3 } }
    val columnWidth by remember(maxWidth) { derivedStateOf { maxWidth / visibleColumns } }
    val rowHeight by remember(maxHeight) { derivedStateOf { maxHeight / 7.6f } }
    val fontSize = with(LocalDensity.current) { rowHeight.toSp() / 4.5 }
    val canGoLeft by remember { derivedStateOf { data.week != data.weeks.firstOrNull() } }
    val canGoRight by remember { derivedStateOf { data.week != data.weeks.lastOrNull() } }
    val start by remember { derivedStateOf { if (canGoLeft) 1 else 0 } }
    var day by remember(start) {
        val now = LocalDate.now()
        val today = if (now<data.week) 1 else now.dayOfWeek.value
        mutableStateOf(
            min(today + start - 1, 6 + start - visibleColumns)
        )
    }

    Pager(Modifier.fillMaxSize(), day, onSelect = {
        if (canGoLeft && it <= 0) {
            data.week = data.weeks[data.weeks.indexOf(data.week)-1]
            day = 6-visibleColumns
        } else if (canGoRight && it >= -visibleColumns + start + 8) {
            data.week = data.weeks[data.weeks.indexOf(data.week)+1]
            day = 1
        }
        else day = it
    }) {
        if (canGoLeft) item {
            Box(Modifier.fillMaxHeight().width(columnWidth)) {
                Icon(Icons.Default.ArrowBack, "",
                    Modifier.align(Alignment.Center), tint = MaterialTheme.colors.onBackground)
            }
        }

        items(7) { column ->
            val tableToday by remember(column) {
                derivedStateOf { data.timeTable.filter { it.day == column + start }.sortedBy { it.pair } }
            }
            LazyColumn(Modifier.fillMaxHeight().width(columnWidth)) {
                item {
                    Card(Modifier.fillMaxWidth().height(rowHeight/2).padding(2.dp), elevation = 2.dp) {
                        Box(Modifier.matchParentSize()) {
                            Text(
                                data.week.plusDays(column.toLong()).run {
                                    days[dayOfWeek.value-1] + ", " + format(formatter)
                                },
                                Modifier.align(Alignment.Center),
                                fontSize = fontSize,
                                maxLines = 1
                            )
                        }
                    }
                }

                items(7) { row ->
                    val tableNow by remember(row) {
                        derivedStateOf { tableToday.filter { it.pair == row + 1 } }
                    }
                    Box(Modifier.fillMaxWidth().height(rowHeight)) {
                        Cell(tableNow, data, fontSize)
                    }
                }
            }
        }

        if (canGoRight) item {
            Box(Modifier.fillMaxHeight().width(columnWidth)) {
                Icon(Icons.Default.ArrowForward, "",
                    Modifier.align(Alignment.Center), tint = MaterialTheme.colors.onBackground)
            }
        }
    }
}
