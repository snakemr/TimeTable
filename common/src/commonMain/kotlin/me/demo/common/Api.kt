package me.demo.common

import java.net.URL
import java.nio.charset.Charset
import java.time.LocalDate

object Api {
    private const val tt = "https://www.lrmk.ru/tt/"
    private val charset: Charset = Charset.forName("cp1251")
    private val regHTML = Regex("&#\\d*;")
    private val regInSup = Regex("<.*>")

    fun teachers() = get(tt+"teachers")?.mapOfHtml()

    fun groups() = get(tt+"groups")?.mapOfHtml()

    fun rooms() = get(tt+"rooms")?.replace("&middot;","·")?.mapOfHtml()?.map {
        it.key to if (it.key==53) "ДО" else it.value.replace(regInSup, "")
    }?.toMap()

    fun pairs() = get(tt+"pairs")?.listOfHtml()?.map {
        it.split(',')
    }?.filter { it.size==3 }?.associate {
        it[0].toInt() to Pair(it[1], it[2])
    }

    fun weeks() = get(tt+"weeks")?.split("<br/>")
        ?.mapNotNull {
            try {
                LocalDate.parse(it)
            } catch (pe: Exception) {
                null
            }
        }?.toList()

    enum class Of(val parameter: Char) { Group('g'), Teacher('t') }

    fun subjects(group: Int, of: Of) = get(tt+"discplines?${of.parameter}=$group")?.mapOfHtml()
        ?.map { it.key to it.value.split("&nbsp;") }?.map {
            Triple(it.first, it.second[0].toInt(), it.second[1])
        }

    fun timetable(group: Int, of: Of, week: LocalDate) = get(tt+"timetable?w=$week&${of.parameter}=$group")
        ?.split("<br/>")
        ?.map { row -> row.split(",").map { it.toIntOrNull() } }
        ?.filter { it.size==8 }

    fun new() = get(tt+"whatsnew")

    private fun get(url: String) : String? {
        return try {
            val browser = URL(url)
            browser.readText(charset)
        } catch ( ex: Exception ) {
            println("NETWORKING ERROR: $ex")
            null
        }
    }

//    private fun get(url: String, cookies: String) : String? {
//        return try {
//            val browser = URL(url)
//            val conn = browser.openConnection()
//            conn.setRequestProperty("Cookie", cookies)
//            val reader = conn.getInputStream().bufferedReader()
//            reader.readText().apply { println(this) }.also { reader.close() }
//        } catch ( ex: Exception ) {
//            println("NETWORKING ERROR: $ex")
//            null
//        }
//    }

//    @Suppress("SameParameterValue")
//    private fun post(url: String, xml: String) : String? {
//        return try {
//            val browser = URL(url)
//            val conn = browser.openConnection() as? HttpURLConnection
//            conn?.run {
//                requestMethod = "POST"
//                setRequestProperty("Content-Type", "application/xml")
//                setRequestProperty("Accept", "text/plain")
//                doOutput = true
//                outputStream.bufferedWriter().run {
//                    write(xml)
//                    close()
//                }
//                inputStream.bufferedReader().use(BufferedReader::readText)
//            }
//        } catch ( ex: Exception ) {
//            "NETWORKING ERROR: $ex"
//        }
//    }

    private fun String.listOfHtml() = replace(regHTML) {
        String(Character.toChars(it.value.substring(2, it.value.length - 1).toInt()))
    }.split("<br/>")

    private fun String.mapOfHtml() = listOfHtml()
        .zipWithNext()
        .filterIndexed { index, _ -> index % 2 == 0 }
        .associate { it.first.toInt() to it.second }
}