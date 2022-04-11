package me.demo.common

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.DpSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

expect open class ViewModel() {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
}

class MainViewModel : ViewModel() {
    private val ofState = mutableStateOf(Api.Of.Group)
    private val whatState = mutableStateOf(0)
    val of: Api.Of get() = ofState.value
    val group: Int? get() = whatState.value.takeIf { ofState.value == Api.Of.Group }
    val teacher: Int? get() = whatState.value.takeIf { ofState.value == Api.Of.Teacher }

    private val weekState = mutableStateOf(LocalDate.now().run {
        if (dayOfWeek > DayOfWeek.FRIDAY)
            plusDays(8L-dayOfWeek.value)
        else
            minusDays(dayOfWeek.value-1L)
    })
    var week: LocalDate get() = weekState.value
        set(value) {
            weekState.value = value
            loadData()
        }

    private val groupsState = mutableStateMapOf<Int,String>()
    val groups: Map<Int, String> get() = groupsState.toList().sortedBy { it.second }.toMap()

    private val teachersState = mutableStateMapOf<Int,String>()
    val teachers: Map<Int, String> get() = teachersState.toList().sortedBy { it.second }.toMap()

    private val roomsState = mutableStateMapOf<Int,String>()
    val rooms: Map<Int, String> get() = roomsState.toList().sortedBy { it.second }.toMap()

    private val weeksState = mutableStateListOf<LocalDate>()
    val weeks: List<LocalDate> get() = weeksState.sorted()

    private val pairsState = mutableStateMapOf<Int,Pair<String,String>>()
    val pairs: Map<Int, Pair<String,String>> get() = pairsState.toSortedMap()

    init {
        init(true)
    }

    private fun init(firstInit: Boolean) = CoroutineScope(Dispatchers.IO).launch {
        if (firstInit || groupsState.isEmpty())   groupsState   += Api.groups()   ?: mapOf()
        if (firstInit || teachersState.isEmpty()) teachersState += Api.teachers() ?: mapOf()
        if (firstInit || roomsState.isEmpty())    roomsState    += Api.rooms()    ?: mapOf()
        if (firstInit || weeksState.isEmpty())    weeksState    += Api.weeks()    ?: listOf()
        if (firstInit || pairsState.isEmpty())    pairsState    += Api.pairs()    ?: mapOf()
        if (firstInit) getString("get")?.let { get ->
            if (get.length>1) {
                ofState.value = if (get[0]==Api.Of.Group.parameter) Api.Of.Group else Api.Of.Teacher
                get.substring(1).toIntOrNull()?.let { whatState.value = it }
            }
        }
        if (whatState.value == 0) chooseMode = Api.Of.Group
        if (firstInit) lastUpdateState.value = Api.new()
        setTitle()
        loadData()
    }

    private val titleState = mutableStateOf("")
    val title: String get() = titleState.value

    private fun setTitle() {
        if (ofState.value == Api.Of.Teacher)
            titleState.value = teachers[whatState.value] ?: ""
        else {
            titleState.value = groups[whatState.value] ?: ""
            if (title != "")
                titleState.value = groups.values.filter {
                    it.startsWith(title) && (it==title || !it.contains('('))
                }.joinToString()
        }
    }

    private val subjectsState = mutableStateListOf<Triple<Int,Int,String>>()
    val subjects: List<Triple<Int,Int,String>> get() = subjectsState

    data class TimeTable(
        val day: Int,
        val pair: Int,
        val group: Int?,
        val subject: Int?,
        val teacher1: Int?,
        val teacher2: Int?,
        val room1: Int?,
        val room2: Int?
    )

    private val timeTableState = mutableStateListOf<TimeTable>()
    val timeTable: List<TimeTable> get() = timeTableState

    private fun loadData() = CoroutineScope(Dispatchers.IO).launch {
        val subjects = mutableListOf<Triple<Int,Int,String>>()
        val timeTable = mutableListOf<TimeTable>()
        subjects += Api.subjects(whatState.value, ofState.value) ?: listOf()
        timeTable += getTT(whatState.value, ofState.value) ?: listOf()
        if (ofState.value == Api.Of.Group) groups[whatState.value]?.let { name ->
            groups.filter { it.value != name && !it.value.contains('(') && it.value.startsWith(name) }.forEach {
                subjects += Api.subjects(it.key, ofState.value) ?: listOf()
                timeTable += getTT(it.key, ofState.value) ?: listOf()
            }
        }
        else {
            val all = subjects.groupBy { it.second }.keys
            all.mapNotNull { groups[it] }.map { name ->
               groups.filter { it.value != name && !it.value.contains('(') && it.value.startsWith(name) }
            }.flatMap { it.keys }.minus(all).forEach {
                timeTable += getTT(it, Api.Of.Group) ?: listOf()
            }
        }
        subjectsState.clear()
        subjectsState += subjects
        timeTableState.clear()
        timeTableState += timeTable
    }

    private fun getTT(what: Int, of: Api.Of) = Api.timetable(what, of, week)?.mapNotNull {
        if (it[0] == null || it[1] == null || it[3] == null)
            null
        else
            TimeTable(it[0]!!, it[1]!!, what.takeIf { of == Api.Of.Group }, it[3]!!, it[4], it[5], it[6], it[7])
    }

    fun get(of: Api.Of, what: Int) {
        ofState.value = of
        whatState.value = what
        saveString("get", "${of.parameter}$what")
        setTitle()
        loadData()
    }

    private val lastUpdateState = mutableStateOf<String?>(null)
    //val lastUpdate: String? get() = lastUpdateState.value
    private val noNetworkState = mutableStateOf<String?>(null)
    val noNetwork: String? get() = noNetworkState.value

    fun checkUpdate() = CoroutineScope(Dispatchers.IO).launch {
        val new = Api.new()
        if (new == null)
            noNetworkState.value = "Нет связи с сервером"
        else if (noNetwork != null) {
            init(false)
            noNetworkState.value = null
        }
        if (new != null && lastUpdateState.value != new) {
            lastUpdateState.value = new
            println("reload update $new")
            loadData()
        }
    }

    private val windowSizeState = mutableStateOf(DpSize.Zero)
    private val inPhoneModeState = mutableStateOf(false)
    private val inTabletModeState = mutableStateOf(false)
    var windowSize: DpSize get() = windowSizeState.value
        set(value) {
            windowSizeState.value = value
            inPhoneModeState.value = value.width * 4 <= value.height * 3
            inTabletModeState.value = value.width * 3 >= value.height * 4
        }
    val inPhoneMode: Boolean get() = inPhoneModeState.value
    val inTabletMode: Boolean get() = inTabletModeState.value

    private val fullScreenState = mutableStateOf(false)
    var fullScreen: Boolean get() = fullScreenState.value
        set(value) {
            fullScreenState.value = value
            saveString("fullscreen", value.toString())
        }

    private val darkModeState = mutableStateOf(false)
    var darkMode: Boolean get() = darkModeState.value
        set(value) {
            darkModeState.value = value
            saveString("dark", value.toString())
        }

    fun getViewParams() {
        fullScreenState.value = getString("fullscreen").toBoolean()
        darkModeState.value = getString("dark").toBoolean()
    }

    private val chooseModeState = mutableStateOf<Api.Of?>(null)
    var chooseMode: Api.Of? get() = chooseModeState.value
        set(value) { chooseModeState.value = value }
}
