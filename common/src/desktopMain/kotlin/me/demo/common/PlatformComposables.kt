package me.demo.common

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.sign

@Composable
actual fun WeekMenu(current: LocalDate, weeks: List<LocalDate>, onChange: (LocalDate)->Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Pager(Modifier.fillMaxWidth(), lazyListState, scope, false) {
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
actual fun BoxScope.PlatformLazyColumn(items: LazyListScope.()->Unit) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LazyColumn(Modifier.matchParentSize().draggable(rememberDraggableState { delta ->
        scope.launch { lazyListState.scrollBy(-delta) }
    }, Orientation.Vertical), lazyListState) {
        items()
    }
    VerticalScrollbar(ScrollbarAdapter(lazyListState), Modifier.align(Alignment.TopEnd))
}

@Composable
actual fun Pager(modifier: Modifier, items: LazyListScope.() -> Unit) =
    Pager(modifier, rememberLazyListState(), rememberCoroutineScope(), true, items = items)

@Composable
actual fun Pager(
    modifier: Modifier,
    selected: Int,
    onSelect: (Int)->Unit,
    items: LazyListScope.() -> Unit
) = Pager(
    modifier, rememberLazyListState(), rememberCoroutineScope(), true, selected, onSelect, items
)

@Composable
private fun Pager(
    modifier: Modifier,
    lazyListState: LazyListState,
    scope: CoroutineScope,
    animateScroll: Boolean,
    selected: Int = -1,
    onSelect: (Int)->Unit = {},
    items: LazyListScope.() -> Unit
) = Box(modifier) {
    var local by remember { mutableStateOf(-1) }
    LazyRow(Modifier.fillMaxWidth().draggable(rememberDraggableState { delta ->
        scope.launch { lazyListState.scrollBy(-delta) }
    }, Orientation.Horizontal, onDragStopped = {
        val offset = lazyListState.firstVisibleItemScrollOffset
        val size = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size
        val scrolledToEnd = lazyListState.isScrolledToTheEnd()
        val index = lazyListState.firstVisibleItemIndex +
                if (size!=null && offset>size/2 || scrolledToEnd) 1 else 0
        local = index
        onSelect(index)
        scope.launch { lazyListState.animateScrollToItem(index) }
    }).scrollable(rememberScrollableState { delta ->
        val index = (lazyListState.firstVisibleItemIndex - delta.sign.toInt()).takeIf { it>=0 } ?: 0
        local = index
        onSelect(index)
        if (animateScroll)
            scope.launch { lazyListState.animateScrollToItem(index) }
        else
            scope.launch { lazyListState.scrollToItem(index) }
        delta
    }, Orientation.Vertical), lazyListState) {
        items()
    }
    HorizontalScrollbar(ScrollbarAdapter(lazyListState), Modifier.align(Alignment.BottomStart))
    LaunchedEffect(selected, items) {
        if (selected >= 0 && selected != local) scope.launch {
            local = selected
            lazyListState.animateScrollToItem(selected)
        }
    }
}