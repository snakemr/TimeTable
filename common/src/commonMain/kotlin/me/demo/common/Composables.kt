package me.demo.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

@Composable
fun RowScope.CommonAppBar(data: MainViewModel) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    if (data.chooseMode != null) IconButton({ data.chooseMode = null }) {
        Icon(Icons.Default.ArrowBack, "Назад")
    }

    Text(
        when {
            data.noNetwork != null -> data.noNetwork ?: ""
            data.chooseMode != null && data.inTabletMode -> "Выбор группы или преподавателя"
            data.chooseMode == Api.Of.Group -> "Выбор группы"
            data.chooseMode == Api.Of.Teacher -> "Выбор преподавателя"
            else -> data.title +
                    (if (data.inTabletMode) " — расписание на неделю с " else " — ") +
                    data.week.format(formatter)
        },
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )

    Spacer(Modifier.weight(1f))

    IconButton({ data.chooseMode = Api.Of.Group.takeIf { data.chooseMode != Api.Of.Group } }) {
        Icon(Icons.Default.Star, "Выбрать группу")
    }
    IconButton({ data.chooseMode = Api.Of.Teacher.takeIf { data.chooseMode != Api.Of.Teacher } }) {
        Icon(Icons.Default.Person, "Выбрать преподавателя")
    }
    IconButton({ data.darkMode = !data.darkMode }) {
        Icon(Icons.Default.Settings, "Тёмный режим")
    }
}

@Composable
fun BoxScope.Chooser(data: MainViewModel, of: Api.Of) {
    PlatformLazyColumn {
        items(
            if (of == Api.Of.Group)
                data.groups.filter { it.value.noEmoji() } .toList()
            else
                data.teachers.toList()
        ) {
            Text(it.second, Modifier.fillMaxWidth().clickable {
                data.get(of, it.first)
                data.chooseMode = null
            }.padding(10.dp),
            color = MaterialTheme.colors.onBackground)
            Divider()
        }
    }
}

private fun String.noEmoji() = maxOf { it.code } <= 1103

@Composable
fun Pairs(data: MainViewModel) = BoxWithConstraints(Modifier.fillMaxHeight()) {
    val rowHeight by remember(maxHeight) { derivedStateOf { maxHeight / 7.6f } }
    val fontSize = with(LocalDensity.current) { rowHeight.toSp() / 4.5 }
    Column(Modifier.fillMaxHeight().width(IntrinsicSize.Min)) {
        IconButton({ data.fullScreen = !data.fullScreen },
            Modifier.height(rowHeight / 2).fillMaxWidth()) {
            Icon(if(data.fullScreen) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                "Полный экран", tint = MaterialTheme.colors.onBackground)
        }
        data.pairs.forEach {
            Card(Modifier.height(rowHeight).padding(2.dp), elevation = 2.dp) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(it.value.first, fontSize = fontSize)
                    Text(it.key.toString(), fontSize = fontSize)
                    Text(it.value.second, fontSize = fontSize)
                }
            }
        }
    }
}
