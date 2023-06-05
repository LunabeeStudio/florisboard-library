package dev.patrickgold.florisboard

import android.content.Context
import dev.patrickgold.florisboard.ime.editor.EditorInstance

class InterceptEditorInstance(context: Context) : EditorInstance(context) {

    var intercept: ((String) -> Boolean)? = null
    var deleteBackwards: (() -> Boolean)? = null
    var blockInput: Boolean = false

    override fun commitChar(char: String): Boolean {
        return when {
            intercept != null -> {
                intercept?.invoke(char)
                true
            }
            blockInput -> true
            else -> super.commitChar(char)
        }
    }

    override fun commitText(text: String): Boolean {
        return when {
            intercept != null -> {
                intercept?.invoke(text)
                true
            }
            blockInput -> true
            else -> super.commitText(text)
        }
    }

    override fun deleteBackwards(): Boolean {
        return when {
            deleteBackwards != null -> {
                deleteBackwards?.invoke()
                true
            }
            blockInput -> true
            else -> super.deleteBackwards()
        }
    }
}
