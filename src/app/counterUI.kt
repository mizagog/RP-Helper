import kotlinext.js.js
import kotlinx.html.*
import kotlinx.html.js.*
import react.RBuilder
import react.dom.*
import kotlin.browser.document
import kotlin.js.Date

interface Eh {
    fun updateAndSave()
    fun remove(d: CounterDate)
    fun duplicate(d: CounterDate)
    fun linkOK(counterID: Int, increment: Int, mod: Int): Boolean
    fun getCounter(linkedCounterID: Int): CounterDate?
    //    fun moveUp(d: CounterDate)
//    fun moveDown(d: CounterDate)
//    fun swap(me: CounterDate, otherHash: Int)
}

fun RBuilder.counterUI(data: CounterDate, eh: Eh) {
    div(classes = "counter_root") {
        attrs.jsStyle { if (data.isEditMode) border = "solid thin" }
        div {
            attrs.jsStyle { display = "flex" }
            button(classes = "edit_button") {
                +"E"
                attrs {
                    id = data.hashCode().toString() + "e"
                    onClickFunction = {
                        data.isEditMode = !data.isEditMode
                        eh.updateAndSave()
                    }
                    onDoubleClickFunction = {
                        eh.duplicate(data)
                    }
                }
            }
            div(if (data.isHeadline) "headline" else "") {
                inputBind(InputType.text, true, "", data.freeText, eh) {
                    data.freeText = it
                }
            }
            if (data.isShowCount) {
                span {
                    +": "
                    attrs.jsStyle { fontSize = "larger" }
                }

                inputBind(InputType.number, true, "", data.currentCount.toString(), eh)
                {
                    data.currentCount = it.toInt()
                    checkMinMax(data)
                }
            }
            span {
                +if (data.max != 0) "/ ${data.max}" else ""
                attrs.jsStyle { marginTop = "3px" }
            }
            if (data.isShowCount && !data.isHideActionButtons) {
                button(classes = "action_buttons") {
                    +"+"
                    attrs.onClickFunction = {
                        increment(1, data, eh)
                    }
                }
                button(classes = "action_buttons") {
                    +"-"
                    attrs.onClickFunction = {
                        increment(-1, data, eh)
                    }
                }
            }
            if (data.showUseButton) {
                button {
                    +"${data.linkIncrement} ${eh.getCounter(data.linkedCounterID)?.freeText}"
                    attrs.onClickFunction = {
                        increment(-1, data, eh)
                    }
                }
            }
        }
        if (data.isEditMode) {
            div {
                attrs.style = js {
                    paddingLeft = "60px"
                }
                button {
                    +"Duplicate"
                    attrs {
                        onClickFunction = {
                            data.isEditMode = false
                            eh.duplicate(data)
                        }
                    }
                }
                br { }
                button {
                    +"Link"
                    attrs.id = data.hashCode().toString() + "l"
                    attrs.onDoubleClickFunction = {
                        println("Wow")
                        data.linkedCounterID = 0
                        data.showUseButton = false
                        eh.updateAndSave()
                    }
                }
                if (data.linkedCounterID != 0) {
                    inputBind(InputType.number, false, "Link increment:", data.linkIncrement.toString(), eh) {
                        data.linkIncrement = it.toInt()
                    }
                    inputCheckboxBind(eh, "Show Use Button?", data.showUseButton, {
                        data.showUseButton = it
                        data.isHideActionButtons = if (it == true) true else data.isHideActionButtons
                    })
                }
                inputCheckboxBind(eh, "Show counter?", data.isShowCount, {
                    data.isShowCount = it
                })
                if (data.isShowCount) {
                    inputCheckboxBind(eh, "Hide action buttons?", data.isHideActionButtons, {
                        data.isHideActionButtons = it
                    })
                    inputBind(InputType.number, false, "Change increment:", data.increment.toString(), eh) {
                        data.increment = it.toInt()
                    }
                    inputBind(InputType.number, false, "Change Max:", data.max.toString(), eh) {
                        data.max = it.toInt()
                        data.currentCount = data.max
                        checkMinMax(data)
                    }
                    inputBind(InputType.number, false, "Change Min:", data.min.toString(), eh) {
                        data.min = it.toInt()
                        data.currentCount = data.min
                        checkMinMax(data)
                    }
                }
                button {
                    +"Del"
                    attrs {
                        onClickFunction = {
                            eh.remove(data)
                        }
                    }
                }
            }
        }
    }
}

private fun RBuilder.inputCheckboxBind(eh: Eh, label: String, b: Boolean, f: (Boolean) -> Unit) {
    div {
        input(InputType.checkBox) {
            attrs {
                checked = b
                onChangeFunction = {
                    f(it.currentTarget.asDynamic().checked)
                    eh.updateAndSave()
                }
            }
        }
        label {
            +label
        }
    }
}

private fun increment(mod: Int, data: CounterDate, eh: Eh) {
    if (eh.linkOK(data.linkedCounterID, data.linkIncrement, mod)) {
        data.currentCount += data.increment * mod
        checkMinMax(data)
        eh.updateAndSave()
    }
}

private fun checkMinMax(data: CounterDate) {
    if (data.max != 0 && data.currentCount > data.max) data.currentCount = data.max
//    if (data.min != 0 && data.currentCount < data.min) data.currentCount = data.min
    if (data.currentCount < data.min) data.currentCount = data.min
}

private fun RBuilder.inputBind(inputType: InputType, isDoubleClick: Boolean, desc: String, label: String, eh: Eh, f: (v: String) -> Unit) {
    div {
        label { +desc }
        val classes = if (isDoubleClick) "counter_name" else ""
        textArea(classes = classes) {
            if (isDoubleClick) {
                val baseHeight = 18.0
                val w = when {
                    label.isEmpty() -> 100.0
                    else -> (label.length + 2) * 8.0
                }
                val h = when {
                    label.isEmpty() -> baseHeight
                    else -> label.split(Regex("\n")).size * baseHeight
                }
                attrs.jsStyle {
                    width = "${w}px"
                    height = "${h}px"
                }
            }
            attrs {
                //                wrap = js { "hard" }
                if (isDoubleClick) {
                    readonly = true
                    onDoubleClickFunction = {
                        it.currentTarget.asDynamic().readOnly = false
                        if (document.activeElement != it.currentTarget)
                            it.currentTarget.asDynamic().select()
                    }
                    onBlurFunction = {
                        it.currentTarget.asDynamic().readOnly = true
                        it.currentTarget.asDynamic().blur()
                    }
                } else {
                    onFocusFunction = {
                        it.currentTarget.asDynamic().select()
                    }
                }
                value = label
                onChangeFunction = {
                    it.currentTarget.asDynamic().style.width = (it.currentTarget.asDynamic().value.length + 1) * 7
                    val v: String = it.currentTarget.asDynamic().value
                    var s: String = v
                    if (inputType == InputType.number) {
                        s = when (v.toIntOrNull()) {
                            null -> "0"
                            else -> v
                        }
                    }
                    f(s)
                    eh.updateAndSave()
                }
            }
        }
    }
}

class CounterDate(
        var freeText: String = "Counter",
        var currentCount: Int = 0,
        var increment: Int = 1,
        var max: Int = 0,
        var min: Int = 0,
        var isEditMode: Boolean = false,
        var isShowCount: Boolean = true,
        var isHideActionButtons: Boolean = false,
        var isHeadline: Boolean = true,
        var linkedCounterID: Int = 0,
        var linkIncrement: Int = 1,
        var showUseButton: Boolean = false
) {
    val id = this.hashCode()
}
