package me.demo.common

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun App(data: MainViewModel) {

    if (data.chooseMode == null) {

        if (!data.fullScreen)
            WeekMenu(data.week, data.weeks) { data.week = it }

        Row {
            Pairs(data)
            Table(data)
        }

    } else if (data.inTabletMode) Row(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxHeight().weight(1f).padding(10.dp)) {
            Chooser(data, Api.Of.Group)
        }
        Box(Modifier.fillMaxHeight().weight(1f).padding(10.dp)) {
            Chooser(data, Api.Of.Teacher)
        }
    } else if (data.chooseMode == Api.Of.Group) Box(Modifier.fillMaxSize().padding(10.dp)) {
        Chooser(data, Api.Of.Group)
    } else Box(Modifier.fillMaxSize().padding(10.dp)) {
        Chooser(data, Api.Of.Teacher)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            if (data.chooseMode == null) data.checkUpdate()
        }
    }
}



