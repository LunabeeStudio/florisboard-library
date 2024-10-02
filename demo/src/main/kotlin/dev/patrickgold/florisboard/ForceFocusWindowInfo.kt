package dev.patrickgold.florisboard

import androidx.compose.ui.platform.WindowInfo

object ForceFocusWindowInfo : WindowInfo {
    override val isWindowFocused: Boolean = true
}
