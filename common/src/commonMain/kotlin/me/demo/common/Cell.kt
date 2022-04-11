package me.demo.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity

@Composable
fun BoxScope.Cell(table: List<MainViewModel.TimeTable>, data: MainViewModel, fontSize: TextUnit) {
    val colorRange = remember(data.of) {
        if (data.of == Api.Of.Group) data.teachers.keys.toSortedSet() else data.groups.keys.toSortedSet()
    }
    val my by remember(table, data.of, data.teacher) { derivedStateOf {
        if (data.of == Api.Of.Teacher)
            table.filter {
                it.teacher1==data.teacher || it.teacher2==data.teacher
            }.sortedBy { entry ->
                data.groups[data.subjects.firstOrNull { it.first==entry.subject }?.second]
            }
        else
            table.sortedBy { data.groups[it.group] }
    } }
    val other by remember(table) { derivedStateOf {
        table - my.toSet()
    } }
    val count = my.size
    val padding = 6.dp * count - 4.dp

    val strokeWidth = with(LocalDensity.current) {  2.dp.toPx() }
    val strokeColor = MaterialTheme.colors.error

    var order by remember(table) { mutableStateOf(0) }

    for (i in my.indices) {
        val index = (i + order) % my.size
        val entry = my[index]
        val offset = 6.dp * index
        Card(Modifier.matchParentSize()
            .padding(start = 2.dp, top = 2.dp, end = padding, bottom = padding)
            .offset(offset, offset)
            .clickable(my.size > 1) { order++ },
            border = BorderStroke(0.5.dp, MaterialTheme.colors.primaryVariant),
            elevation = 2.dp
        ) {
            val subject by remember(entry.subject) { derivedStateOf {
                data.subjects.firstOrNull { it.first == entry.subject }?.run { second to third }
            } }
            val groupId by remember(entry.subject, entry.group) { derivedStateOf {
                subject?.first ?: entry.group
            } }
            val group by remember(entry.subject, entry.group) { derivedStateOf {
                data.groups[groupId]
            } }
            val teacher by remember(entry.teacher1, entry.teacher2) { derivedStateOf {
                data.teachers[entry.teacher1]?.plus(entry.teacher2?.let { ", " + data.teachers[it] } ?: "")
            } }
            val room by remember(entry.room1, entry.room2) { derivedStateOf {
                data.rooms[entry.room1]?.plus(entry.room2?.let { ", " + data.rooms[it] } ?: "")
            } }
            val goneBy by remember(table, entry.group) { derivedStateOf {
                other.firstOrNull { data.groups[it.group]?.startsWith(group?:"-") ?: false }
            } }
            val color by remember(data.of, entry.subject, entry.group, entry.teacher1) { derivedStateOf {
                if (data.of == Api.Of.Group) entry.teacher1?.let {
                    rangeColor(colorRange.indexOf(it), colorRange.size, data.darkMode)
                } ?: Color.Unspecified
                else groupId?.let {
                    rangeColor(colorRange.indexOf(it), colorRange.size, data.darkMode)
                } ?: Color.Unspecified
            } }

            Column(Modifier.matchParentSize().background(color)) {
                Text(subject?.second ?: "", Modifier.padding(horizontal = 6.dp), fontSize = fontSize, maxLines = 2)
                Spacer(Modifier.weight(1f))
                Row {
                    Text((if (data.of==Api.Of.Group) teacher else group) ?: "",
                        Modifier.padding(start = 6.dp), fontSize = fontSize, maxLines = 1)
                    Spacer(Modifier.weight(1f))
                        Text(if (goneBy?.group != null)
                                data.teachers[goneBy?.teacher1] + " " + data.rooms[goneBy?.room1]
                            else
                                room ?: "",
                            Modifier.padding(end = 6.dp), fontSize = fontSize, maxLines = 1)
                }
            }
            if (goneBy != null) Canvas(Modifier.matchParentSize()) {
                drawLine(strokeColor, Offset.Zero, Offset(size.width, size.height), strokeWidth)
                drawLine(strokeColor, Offset(0f, size.height), Offset(size.width, 0f), strokeWidth)
            }
        }
    }
}

private fun rangeColor(index: Int, rangeSize: Int, darkTheme: Boolean = false) : Color {
    val comp = (if (index<0) 0 else index) * (if (darkTheme) 64 else 128) / rangeSize
    return if (darkTheme)
        Color(64-comp, 64 - (if (comp>32) comp*2-64 else 64-comp*2), comp)
    else
        Color(255-comp, 255 - (if (comp>64) comp*2-128 else 128-comp*2), 127+comp)
}