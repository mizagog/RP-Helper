package app

import counterUI
import CounterDate
import Eh
import kotlinx.html.js.onClickFunction
import makeId
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import kotlin.browser.document
import kotlin.browser.localStorage

var startElement: CounterDate? = null
var undoRemove: Pair<CounterDate, Int>? = null
var selectedIndex: Int = 0
var masterList: ArrayList<MutableList<CounterDate>> = arrayListOf()

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
//        val s = localStorage.getItem("masterList")
//        val i = localStorage.getItem("masterListIndex")
//        if (s != null && i != null && i.toIntOrNull() != null) {
//            println(i.toInt())
//            masterList = JSON.parse(s)
//            state.list = masterList[i.toInt()]
//        } else {
        list = mutableListOf()
        val data = localStorage.getItem("list")
        if (data != null) {
            list = JSON.parse<Array<CounterDate>>(data).toMutableList()
        } else {
            list.add(CounterDate("Stats", isShowCount = false))
            list.add(CounterDate("HP", 10, max = 10))
            val m = CounterDate("MP", 15, max = 15)
            list.add(m)
            list.add(CounterDate("XP", 10, max = 10))
            list.add(CounterDate("", isShowCount = false))
            list.add(CounterDate("CP", 15))
            list.add(CounterDate("SP", 3))
            list.add(CounterDate("", isShowCount = false))
            list.add(CounterDate("Items", isShowCount = false))
            list.add(CounterDate("Bronze Sword", currentCount = 5, isHideActionButtons = true))
            list.add(CounterDate("Leather Hood", currentCount = 2, isHideActionButtons = true))
            list.add(CounterDate("", isShowCount = false))
            list.add(CounterDate("Skills", isShowCount = false))
            list.add(CounterDate(freeText = "Cleave", currentCount = 0, increment = 0, linkedCounterID = m.id, showUseButton = true, linkIncrement = 2, isShowCount = false))
            list.add(CounterDate(freeText = "Jump Good", currentCount = 0, increment = 0, linkedCounterID = m.id, showUseButton = true, linkIncrement = 1, isShowCount = false))
            list.add(CounterDate(freeText = "God Bless (3/Day)", currentCount = 3, increment = 1, linkedCounterID = m.id, linkIncrement = 3))
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
            if (startElement != null) it.preventDefault()
        })
    }

    private fun addAllListeners() {
//        state.list.forEach {
//            val id = it.hashCode()
//            attachListener(document.getElementById("${id}l")) {
//                val pair = getDataByPosition(it)
//                if (pair != null && startElement != null) {
//                    linkCounters(startElement!!, pair.first)
//                    startElement = null
//                }
//            }
//        }
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

    private fun attachListener(element: Element?, callback: (Event) -> Unit) {
        if (element != null) {
        }
    }

    private fun linkCounters(me: CounterDate, other: CounterDate) {
        if (me != other) {
            me.linkedCounterID = other.id
            updateSave()
        }
    }

    override fun RBuilder.render() {
//        wow()
//        div { attrs.jsStyle = js { height = "1001px" } }
        button {
            +"Clear All"
            attrs {
                onClickFunction = {
                    localStorage.removeItem("list")
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
        if (undoRemove != null)
            button {
                +"Undo remove"
                attrs.onClickFunction = {
                    if (undoRemove != null) {
                        state.list.add(undoRemove!!.second, undoRemove!!.first)
                        undoRemove = null
                        updateSave()
                    }
                }
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
                    }

                    override fun setStartElement(start: CounterDate) {
                        startElement = start
                        println(start.id)
                    }

                    override fun locationChange(e: Event, data: CounterDate) {
                        if (startElement != null) {
                            val (y, x, element) = getElementFromPos(e)
                            val id = element?.id
                            println("id: " + id)
                            if (!id.isNullOrEmpty() && element != null) {
                                val rect = element.getBoundingClientRect()
                                println("me: ${data.id}, ${data.freeText}")
                                println("startElement!!.id: " + data.id)
                                val d = rect.height / 2 + rect.y
                                val isUp = y < d
                                println("isUp: " + isUp)
                                putUnder(state.list.first { it.id == id!! }, data, isUp)
                            } else {
                                val root = document.getElementById("root")
                                if (root != null) {
                                    println(x)
                                    val rect = root.getBoundingClientRect()
                                    if (x > rect.width / 3) {
                                        dup(data)
                                    }
                                }
                            }
                            startElement = null
                            updateSave()
                        }
                    }

                    private fun getElementFromPos(e: Event): Triple<Double, Double, Element?> {
                        val y = e.asDynamic().changedTouches[0].clientY as Double
                        val x = e.asDynamic().changedTouches[0].clientX as Double
                        return Triple(y, x, document.elementFromPoint(x, y))
                    }

                    override fun getCounter(linkedCounterID: String): CounterDate? {
                        return state.list.firstOrNull { it.id == linkedCounterID }
                    }

                    override fun linkOK(counterID: String, increment: Int, mod: Int): Boolean {
                        println(counterID)
                        state.list.forEach { println(it.id) }
                        val data = state.list.firstOrNull { it.id == counterID }
                        return if (data != null) {
                            if (data.currentCount + increment * mod < 0) false
                            else {
                                data.currentCount += increment * mod
                                updateSave()
                                true
                            }
                        } else true
                    }

                    override fun duplicate(d: CounterDate) {
                        dup(d)
                    }

                    //                    override fun moveUp(d: CounterDate) {
//                        val i = state.list.indexOf(d)
//                        if (i > 0) {
//                            state.list.remove(d)
//                            state.list.add(i - 1, d)
//                            updateSave()
//                        }
//                    }
//                    override fun moveDown(d: CounterDate) {
//                        val i = state.list.indexOf(d)
//                        if (i < state.list.size - 1) {
//                            state.list.remove(d)
//                            state.list.add(i + 1, d)
//                            updateSave()
//                        }
//                    }
                    override fun updateAndSave() {
                        updateSave()
                    }

                    override fun remove(d: CounterDate) {
                        undoRemove = Pair(d, state.list.indexOf(d))
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
        println(duplicate.id)
        println(d.id)
        state.list.add(state.list.indexOf(d), duplicate)
        updateSave()
    }

    private fun del(d: CounterDate) {
        undoRemove = Pair(d, state.list.indexOf(d))
        state.list.remove(d)
        updateSave()
    }

    private fun updateSave() {
        localStorage.setItem("list", JSON.stringify(state.list.toTypedArray()))
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
            println(absoluteValue)
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
}

fun RBuilder.app() = child(App::class) {}