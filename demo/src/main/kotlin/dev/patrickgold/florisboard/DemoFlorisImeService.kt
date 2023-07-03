package dev.patrickgold.florisboard

import android.view.KeyEvent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.ime.editor.EditorInstance
import dev.patrickgold.florisboard.ime.theme.ThemeManager
import dev.patrickgold.florisboard.lib.devtools.flogDebug

class DemoFlorisImeService : FlorisImeService() {

    private val themeManager: ThemeManager by themeManager()
    private val _editorInstance: EditorInstance by editorInstance()
    private val editorInstance: InterceptEditorInstance
        get() = _editorInstance as InterceptEditorInstance

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        flogDebug { "event = $event" }
        return super.onKeyUp(keyCode, event)
    }

    @Composable
    override fun DecoratedIme(ImeUi: @Composable () -> Unit) {
        var isKeyboardVisible by remember {
            mutableStateOf(true)
        }

        Column {
            AboveKeyboardUi(
                modifier = Modifier
                    .weight(1f, false),
                isKeyboardVisible = isKeyboardVisible,
                toggleKeyboardVisibility = { isKeyboardVisible = !isKeyboardVisible },
            )
            if (isKeyboardVisible) {
                ImeUi()
            }
        }
    }

    @Composable
    private fun AboveKeyboardUi(
        modifier: Modifier,
        isKeyboardVisible: Boolean,
        toggleKeyboardVisibility: () -> Unit,
    ) {
        val textFieldValues = remember {
            mutableStateListOf(TextFieldValue(), TextFieldValue())
        }
        val focusManager = LocalFocusManager.current

        var isExpanded by remember {
            mutableStateOf(false)
        }
        val heightModifier by remember(isExpanded) {
            if (isExpanded) {
                editorInstance.blockInput = true
                Modifier.fillMaxHeight()
            } else {
                editorInstance.blockInput = false
                Modifier.wrapContentHeight()
            }.let(::mutableStateOf)
        }

        Box(
            modifier
                .fillMaxWidth()
                .then(heightModifier)
                .drawBehind { drawRect(Color(0x80FF0000)) }
        )
        {
            val scrollableState = rememberScrollState()

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                    .verticalScroll(scrollableState),
            ) {
                Text("Hello top UI")
                textFieldValues.forEachIndexed { index, fieldValue ->
                    key(index) {
                        TextField(
                            value = fieldValue,
                            onValueChange = { newField ->
                                textFieldValues.removeAt(index)
                                textFieldValues.add(index, newField)
                            },
                            modifier = Modifier
                                .keyboardTextfield(
                                    { isKeyboardVisible },
                                    toggleKeyboardVisibility,
                                    { textFieldValues[index] }
                                ) { newField ->
                                    textFieldValues.removeAt(index)
                                    textFieldValues.add(index, newField)
                                },
                        )
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = {
                        focusManager.clearFocus()
                    }) {
                        Text(text = "Release focus")
                    }
                    Button(onClick = {
                        isExpanded = !isExpanded
                    }) {
                        Text(text = if (isExpanded) "Wrap" else "Full height")
                    }
                    Button(onClick = {
                        toggleKeyboardVisibility()
                    }) {
                        Text(text = if (isKeyboardVisible) "Hide keyboard" else "Show keyboard")
                    }
                    Button(onClick = {
                        themeManager.updateActiveTheme(forceNight = true)
                    }) {
                        Text(text = "Force night")
                    }
                    Button(onClick = {
                        themeManager.updateActiveTheme()
                    }) {
                        Text(text = "Update theme")
                    }
                }
                if (!isKeyboardVisible) {
                    Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)))
                }
            }
        }
    }
}
