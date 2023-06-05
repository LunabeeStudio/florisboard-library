package dev.patrickgold.florisboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun Modifier.keyboardTextfield(
    isKeyboardVisible: () -> Boolean,
    toggleKeyboardVisibility: () -> Unit,
    textFieldValue: () -> TextFieldValue,
    setTextFieldValue: (TextFieldValue) -> Unit,
): Modifier {
    return this then Modifier
        .composed {
            val context = LocalContext.current
            val editorInstance by remember(context) {
                mutableStateOf((context.editorInstance().value as InterceptEditorInstance))
            }
            onFocusChanged { state ->
                if (state.hasFocus) {
                    if (!isKeyboardVisible()) {
                        toggleKeyboardVisibility()
                    }
                    editorInstance.intercept = { newText ->
                        val fieldValue = textFieldValue()
                        val selection = fieldValue.selection
                        val text = fieldValue.text.replaceRange(selection.start, selection.end, newText)
                        setTextFieldValue(TextFieldValue(text, TextRange(selection.start + newText.length)))
                        true
                    }
                    editorInstance.deleteBackwards = {
                        val fieldValue = textFieldValue()
                        val selection = fieldValue.selection
                        val text: String
                        val position: Int
                        if (selection.start == selection.end) {
                            text = fieldValue.text.removeRange(
                                (selection.start - 1).coerceAtLeast(0),
                                selection.start
                            )
                            position = (selection.start - 1).coerceAtLeast(0)
                        } else {
                            text = fieldValue.text.removeRange(selection.start, selection.end)
                            position = selection.start
                        }
                        setTextFieldValue(TextFieldValue(text, TextRange(position)))
                        true
                    }
                } else {
                    editorInstance.intercept = null
                    editorInstance.deleteBackwards = null
                }
            }
        }
}
