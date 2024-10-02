package dev.patrickgold.florisboard

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
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
    override fun AboveImeView(ImeUi: @Composable () -> Unit) {
        var isKeyboardVisible by remember {
            mutableStateOf(true)
        }
        Column {
            AboveKeyboardUi(
                modifier = Modifier.weight(1f, false),
                isKeyboardVisible = isKeyboardVisible,
                toggleKeyboardVisibility = { isKeyboardVisible = !isKeyboardVisible },
            )
            if (isKeyboardVisible) {
                ImeUi()
            }
        }
    }

    @Composable
    override fun ThemeImeView() {
        Box(
            Modifier
                .background(Color.White)
                .fillMaxWidth()
                .height(16.dp)
        )
    }

    override fun calculateTouchableTopY(visibleTopY: Int, needAdditionalOverlay: Boolean): Int {
        return 0
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
                Modifier.fillMaxHeight(0.9f)
            } else {
                editorInstance.blockInput = false
                Modifier.wrapContentHeight()
            }.let(::mutableStateOf)
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF000000).copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .then(heightModifier)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .drawBehind { drawRect(Color(0xFFFF0000)) }
            ) {
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
                            CompositionLocalProvider(LocalWindowInfo.provides(ForceFocusWindowInfo)) {
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
                    }
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            modifier = Modifier.padding(start = 8.dp),
                            onClick = {
                                focusManager.clearFocus()
                            },
                        ) {
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
                        Button(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = {
                                themeManager.updateActiveTheme()
                            },
                        ) {
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

    override fun onComposeViewTouchEvent(ev: MotionEvent?) {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            flogDebug { "Touch down at ${ev.x}, ${ev.y}" }
        }
    }
}
