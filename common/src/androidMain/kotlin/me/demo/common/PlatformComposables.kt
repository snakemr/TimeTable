package me.demo.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
actual fun WeekMenu(current: LocalDate, weeks: List<LocalDate>, onChange: (LocalDate)->Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Pager(Modifier.fillMaxWidth(), lazyListState, scope) {
        items(weeks) {
            Card(Modifier.padding(2.dp).clickable { onChange(it) }, elevation = 2.dp) {
                Text(it.format(formatter), Modifier.background(
                    if (current==it) MaterialTheme.colors.primary else Color.Unspecified
                ).padding(4.dp),
                    color = if (current==it) MaterialTheme.colors.onPrimary else Color.Unspecified
                )
            }
        }
    }
    LaunchedEffect(current, weeks.size) {
        val index = weeks.indexOf(current)
        val visible = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } != null
        if (index >= 0 && !visible) scope.launch {
            lazyListState.animateScrollToItem(index)
        }
    }
}

private fun LazyListState.isScrolledToTheEnd() : Boolean {
    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
    return lastItem == null || lastItem.size + lastItem.offset <= layoutInfo.viewportEndOffset
}

@Composable
actual fun BoxScope.PlatformLazyColumn(items: LazyListScope.() -> Unit) {
    LazyColumn(Modifier.matchParentSize()) {
        items()
    }
}

@Composable
actual fun Pager(modifier: Modifier, items: LazyListScope.() -> Unit) =
    Pager(modifier, rememberLazyListState(), rememberCoroutineScope(), items = items)

@Composable
actual fun Pager(
    modifier: Modifier,
    selected: Int,
    onSelect: (Int)->Unit,
    items: LazyListScope.() -> Unit
) = Pager(modifier, rememberLazyListState(), rememberCoroutineScope(), selected, onSelect, items)

@Composable
private fun Pager(
    modifier: Modifier,
    lazyListState: LazyListState,
    scope: CoroutineScope,
    selected: Int = -1,
    onSelect: (Int)->Unit = {},
    items: LazyListScope.() -> Unit
) {
    var local by remember { mutableStateOf(-1) }
    val connection = object : NestedScrollConnection {
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val offset = lazyListState.firstVisibleItemScrollOffset
            val size = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size
            val index = lazyListState.firstVisibleItemIndex +
                    if (size!=null && offset>size/2 || lazyListState.isScrolledToTheEnd()) 1 else 0
            local = index
            onSelect(index)
            scope.launch { lazyListState.animateScrollToItem(index) }
            return super.onPostFling(consumed, available)
        }
    }
    LazyRow(modifier.nestedScroll(connection), state = lazyListState) {
        items()
    }
    LaunchedEffect(selected, items) {
        if (selected >= 0 && selected != local) scope.launch {
            local = selected
            lazyListState.animateScrollToItem(selected)
        }
    }
}
