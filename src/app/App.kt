package app

import counterUI
import CounterDate
import Eh
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import kotlin.browser.document
import kotlin.browser.localStorage

var startElement: CounterDate? = null

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
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
            list.add(CounterDate(freeText = "Cleave", currentCount = 0, increment = 0, linkedCounterID = m.hashCode(), showUseButton = true, linkIncrement = 2, isShowCount = false))
            list.add(CounterDate(freeText = "Jump Good", currentCount = 0, increment = 0, linkedCounterID = m.hashCode(), showUseButton = true, linkIncrement = 1, isShowCount = false))
            list.add(CounterDate(freeText = "God Bless (3/Day)", currentCount = 3, increment = 1, linkedCounterID = m.hashCode(), linkIncrement = 3))
            list.add(CounterDate("", isShowCount = false))
            list.add(CounterDate("Bio", isShowCount = false))
            list.add(CounterDate("""
                Sword Fighting
                Talk to Fish
            """.trimIndent(), isShowCount = false))
        }
    }

    override fun componentDidMount() {
        addAllListeners()
    }

    override fun componentDidUpdate(prevProps: RProps, prevState: AppState) {
        addAllListeners()
    }

    private fun addAllListeners() {
        state.list.forEach {
            val id = it.hashCode()
            attachListener(document.getElementById("${id}e")) {
                val pair = getDataByPosition(it)
                if (startElement != null) {
                    if (pair != null) {
                        putUnder(pair.first, startElement!!, pair.second)
                        startElement = null
                    } else {
                    }
                }
            }
            attachListener(document.getElementById("${id}l")) {
                val pair = getDataByPosition(it)
                if (pair != null && startElement != null) {
                    linkCounters(startElement!!, pair.first)
                    startElement = null
                }
            }
        }
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
            element.addEventListener("touchstart", { startElement = getDataByPosition(it)?.first })
            element.addEventListener("touchmove", { if (startElement != null) it.preventDefault() })
            element.addEventListener("touchend", callback)
        }
    }

    private fun linkCounters(me: CounterDate, other: CounterDate) {
        if (me != other) {
            me.linkedCounterID = other.id
            updateSave()
        }
    }

    private fun getDataByPosition(it: Event): Pair<CounterDate, Boolean>? {
        val y: Double = it.asDynamic().changedTouches[0].clientY
        val element = document.elementFromPoint(it.asDynamic().changedTouches[0].clientX, y)
        val id = element?.id
        if (!id.isNullOrEmpty() && element != null) {
            println(y)
            val rect = element.getBoundingClientRect()
            val d = rect.height / 2 + rect.y
            println(y > d)
            return Pair(state.list.first { it.hashCode().toString() == id!!.dropLast(1) }, (y < d))
        }
        return null
    }

    override fun RBuilder.render() {
//        wow()
//        div { attrs.jsStyle = js { height = "1000px" } }
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
        div("list-container") {
            state.list.forEach {
                counterUI(it, eh = object : Eh {
                    override fun getCounter(linkedCounterID: Int): CounterDate? {
                        return state.list.firstOrNull { it.id == linkedCounterID }
                    }

                    override fun linkOK(counterID: Int, increment: Int, mod: Int): Boolean {
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
                        val counterDate = CounterDate(
                                d.freeText,
                                d.currentCount,
                                d.increment,
                                d.max,
                                d.min,
                                d.isEditMode,
                                d.isShowCount,
                                d.isHideActionButtons,
                                true,
                                d.linkedCounterID,
                                d.linkIncrement,
                                d.showUseButton
                        )
                        state.list.add(state.list.indexOf(d), counterDate)
                        updateSave()
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
                        state.list.remove(d)
                        updateSave()
                    }
                })
            }
        }
    }

    private fun updateSave() {
        localStorage.setItem("list", JSON.stringify(state.list.toTypedArray()))
        setState { }
    }

    private fun putUnder(me: CounterDate, other: CounterDate, isUnder: Boolean) {
//        println("swaappy")
        val list = state.list
        println("me:${me.freeText} ${list.indexOf(me)} other:${other.freeText} ${list.indexOf(other)}")
        if (me != other) {
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