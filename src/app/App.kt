package app

import counterUI
import CounterDate
import Eh
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import makeId
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import kotlin.browser.document
import kotlin.browser.localStorage

var startElement: CounterDate? = null
var selectedIndex: Int = 0
var isExport = false

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        list = mutableListOf()
        val data = localStorage.getItem("list")
        undoList = localStorage.getItem("undoList")?.let { JSON.parse<Array<String>>(it).toMutableList() } ?: arrayListOf()

        if (data != null) {
            list = JSON.parse<Array<CounterDate>>(data).toMutableList()
        } else {
            list.add(CounterDate("Stats", isShowCount = false))
            list.add(CounterDate("HP", 10.0, max = 10.0))
            val m = CounterDate("MP", 15.0, max = 15.0)
            list.add(m)
            list.add(CounterDate("XP", 10.0, max = 10.0))
            list.add(CounterDate("", isShowCount = false))
            list.add(CounterDate("CP", 15.0))
            list.add(CounterDate("SP", 3.0))
            list.add(CounterDate("", isShowCount = false))
            list.add(CounterDate("Items", isShowCount = false))
            list.add(CounterDate("Bronze Sword", currentCount = 5.0, isHideActionButtons = true))
            list.add(CounterDate("Leather Hood", currentCount = 2.0, isHideActionButtons = true))
            list.add(CounterDate("", isShowCount = false))
            list.add(CounterDate("Skills", isShowCount = false))
            list.add(CounterDate(freeText = "Cleave", currentCount = 0.0, increment = 0.0, linkedCounterID = m.id, showUseButton = true, linkIncrement = 2.0, isShowCount = false))
            list.add(CounterDate(freeText = "Jump Good", currentCount = 0.0, increment = 0.0, linkedCounterID = m.id, showUseButton = true, linkIncrement = 1.0, isShowCount = false))
            list.add(CounterDate(freeText = "God Bless (3/Day)", currentCount = 3.0, increment = 1.0, linkedCounterID = m.id, linkIncrement = 3.0))
            list.add(CounterDate("", isShowCount = false))
            list.add(CounterDate("Bio", isShowCount = false))
            list.add(CounterDate("""
                Sword Fighting
                Talk to Fish
            """.trimIndent(), isShowCount = false))
        }
//        }
    }

    override fun componentDidMount() {
        document.getElementById("root")!!.addEventListener("touchmove", {
            if (startElement != undefined && startElement != null) {
                it.preventDefault()
            }
        })
    }

    private fun wow() {
        println("${arr.size} ${arr2.size}")
        arr.forEachIndexed({ index, s ->
            //${arr2[index]}
            run {
                println("""
dn: $s
changetype: modify
add: member
${arr2[index].split("|").filter { it.isNotEmpty() }.map { "member: $it" }.joinToString("\n")}
-
            """)
            }
        })
    }

    override fun RBuilder.render() {
//        wow()
//        div { attrs.jsStyle = js { height = "1001px" } }
        button {
            +"Clear All"
            attrs {
                onClickFunction = {
                    localStorage.clear()
                }
            }
        }
        button {
            +"New Counter"
            attrs.onClickFunction = {
                val element = CounterDate("")
                state.list.add(element)
                updateSave()
            }
        }

        button {
            +"Export / Import"
            attrs.onClickFunction = {
                isExport = !isExport
                val element = document.getElementById("expImp")
                if (element != null) {
                    val s: String = element.asDynamic().value!!
                    if (!s.isEmpty()) {
                        localStorage.setItem("list", s)
                        console.log(localStorage.getItem("list"))
                    }
                }
                setState { }
            }
        }
        if (state.undoList.size > 1)
            button {
                +"Undo"
                attrs.onClickFunction = {
                    val lastIndex = state.undoList.lastIndex
                    state.undoList.removeAt(lastIndex)
                    state.list = JSON.parse<Array<CounterDate>>(state.undoList[lastIndex - 1]).toMutableList()
                    setState { }
                }
            }
        if (isExport)
            textArea {
                attrs.id = "expImp"
                +"${localStorage.getItem("list")}"
            }
        div("list-container") {
            state.list.forEach {
                counterUI(it, eh = object : Eh {
                    override fun linkCounter(e: Event, data: CounterDate) {
                        val (x, y, element) = getElementFromPos(e)
                        if (element != null && element.id.isNotEmpty()) {
                            val id = state.list.first { it.id == element.id }.id
                            if (!data.id.equals(id)) {
                                data.linkedCounterID = id
                                startElement = null
                                updateSave()
                            }
                        }
                        startElement = null
                    }

                    override fun setStartElement(start: CounterDate) {
                        startElement = start
//                        println(start.id)
                    }

                    override fun locationChange(e: Event, data: CounterDate) {
                        if (startElement != null) {
                            val (y, x, element) = getElementFromPos(e)
                            val id = element?.id
//                            println("id: " + id)
                            if (!id.isNullOrEmpty() && element != null) {
                                val rect = element.getBoundingClientRect()
//                                println("me: ${data.id}, ${data.freeText}")
                                println("startElement!!.id: " + data.id)
                                val d = rect.height / 2 + rect.y
                                val isUp = y < d
//                                println("isUp: " + isUp)
                                putUnder(state.list.first { it.id == id!! }, data, isUp)
                            } else {
                                val root = document.getElementById("root")
                                if (root != null) {
//                                    println(x)
                                    val rect = root.getBoundingClientRect()
                                    if (x > rect.width / 3) {
                                        dup(data)
                                    }
                                }
                            }
                        }
                        startElement = null
                    }

                    private fun getElementFromPos(e: Event): Triple<Double, Double, Element?> {
                        val y = e.asDynamic().changedTouches[0].clientY as Double
                        val x = e.asDynamic().changedTouches[0].clientX as Double
                        return Triple(y, x, document.elementFromPoint(x, y))
                    }

                    override fun getCounter(linkedCounterID: String): CounterDate? {
                        return state.list.firstOrNull { it.id == linkedCounterID }
                    }

                    override fun linkOK(counterID: String, increment: Double, mod: Int): Boolean {
//                        println(counterID)
                        state.list.forEach { println(it.id) }
                        val data = state.list.firstOrNull { it.id == counterID }
                        return if (data != null) {
                            if (data.currentCount + increment * mod < 0) false
                            else {
                                data.currentCount += increment * mod
                                updateSave()
//                               if(data.currentCount>data.max)data.currentCount = data.max
                                if (data.max != 0.0)
                                    data.currentCount = minOf(data.currentCount, data.max)
                                true
                            }
                        } else true
                    }

                    override fun duplicate(d: CounterDate) {
                        dup(d)
                    }

                    override fun updateAndSave() {
                        updateSave()
                    }

                    override fun remove(d: CounterDate) {
                        state.list.remove(d)
                        updateSave()
                    }
                })
            }
        }
    }

    private fun dup(d: CounterDate) {
        val duplicate = JSON.parse<CounterDate>(JSON.stringify(d))
        duplicate.id = makeId()
//        duplicate.freeText = ""
//        println(duplicate.id)
//        println(d.id)
        state.list.add(state.list.indexOf(d) + 1, duplicate)
        updateSave()
    }

    private fun del(d: CounterDate) {
        state.list.remove(d)
        updateSave()
    }

    private fun updateSave() {
        console.log("saved")
        val current = JSON.stringify(state.list.toTypedArray())
        state.undoList.add(current)
        localStorage.setItem("list", current)
        localStorage.setItem("undoList", JSON.stringify(state.undoList.toTypedArray()))
        setState { }
    }

    //    private fun updateSave() {
//        localStorage.setItem("masterList", JSON.stringify(masterList))
//        localStorage.setItem("masterListIndex", JSON.stringify(selectedIndex))
//        setState { }
//    }
    private fun putUnder(me: CounterDate, other: CounterDate, isUnder: Boolean) {
//        println("swaappy")
        val list = state.list
        println("me:${me.freeText} ${list.indexOf(me)} other:${other.freeText} ${list.indexOf(other)}")
        if (me.id != other.id) {
            val absoluteValue = list.indexOf(other) - list.indexOf(me)
//            println(absoluteValue)
            list.remove(other)
            when (absoluteValue) {
                1 -> list.add(list.indexOf(me), other)
                -1 -> list.add(list.indexOf(me) + 1, other)
                else -> list.add(list.indexOf(me) + (if (isUnder) 0 else 1), other)
            }
            updateSave()
        }
    }
}

interface AppState : RState {
    var list: MutableList<CounterDate>
    var undoList: MutableList<String>
}

fun RBuilder.app() = child(App::class) {}