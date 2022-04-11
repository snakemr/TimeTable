package me.demo.common

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.LocalDate

@Composable
expect fun WeekMenu(current: LocalDate, weeks: List<LocalDate>, onChange: (LocalDate)->Unit)

@Composable
expect fun BoxScope.PlatformLazyColumn(items: LazyListScope.() -> Unit)

@Composable
expect fun Pager(modifier: Modifier = Modifier, items: LazyListScope.() -> Unit)

@Composable
expect fun Pager(
    modifier: Modifier,
    selected: Int,
    onSelect: (Int)->Unit = {},
    items: LazyListScope.() -> Unit
)