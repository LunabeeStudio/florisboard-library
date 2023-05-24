package dev.patrickgold.florisboard

import android.content.Context
import dev.patrickgold.florisboard.ime.keyboard.KeyData
import dev.patrickgold.florisboard.ime.keyboard.KeyboardManager
import dev.patrickgold.florisboard.lib.devtools.flogDebug

class InterceptKeyboardManager(context: Context) : KeyboardManager(context) {
    override fun onInputKeyUp(data: KeyData) {
        flogDebug { "Data = $data" }
        super.onInputKeyUp(data)
    }
}
