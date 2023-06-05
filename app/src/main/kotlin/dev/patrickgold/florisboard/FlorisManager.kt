/*
 * Copyright (C) 2021 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import androidx.core.os.UserManagerCompat
import dev.patrickgold.florisboard.app.florisPreferenceModel
import dev.patrickgold.florisboard.ime.clipboard.ClipboardManager
import dev.patrickgold.florisboard.ime.core.SubtypeManager
import dev.patrickgold.florisboard.ime.dictionary.DictionaryManager
import dev.patrickgold.florisboard.ime.editor.EditorInstance
import dev.patrickgold.florisboard.ime.keyboard.KeyboardManager
import dev.patrickgold.florisboard.ime.media.emoji.FlorisEmojiCompat
import dev.patrickgold.florisboard.ime.nlp.NlpManager
import dev.patrickgold.florisboard.ime.text.gestures.GlideTypingManager
import dev.patrickgold.florisboard.ime.theme.ThemeManager
import dev.patrickgold.florisboard.lib.NativeStr
import dev.patrickgold.florisboard.lib.cache.CacheManager
import dev.patrickgold.florisboard.lib.crashutility.CrashUtility
import dev.patrickgold.florisboard.lib.devtools.Flog
import dev.patrickgold.florisboard.lib.devtools.LogTopic
import dev.patrickgold.florisboard.lib.devtools.flogError
import dev.patrickgold.florisboard.lib.devtools.flogInfo
import dev.patrickgold.florisboard.lib.ext.ExtensionManager
import dev.patrickgold.florisboard.lib.io.AssetManager
import dev.patrickgold.florisboard.lib.io.deleteContentsRecursively
import dev.patrickgold.florisboard.lib.io.subFile
import dev.patrickgold.florisboard.lib.toNativeStr
import dev.patrickgold.jetpref.datastore.JetPref
import org.florisboard.lib.kotlin.tryOrNull
import java.lang.ref.WeakReference

/**
 * Global weak reference for the [FlorisManager] class. This is needed as in certain scenarios an application
 * reference is needed, but the Android framework hasn't finished setting up
 */
private var FlorisManagerReference = WeakReference<FlorisManager?>(null)

class FlorisManager(
    val context: Lazy<Context>,
    val assetManager: Lazy<AssetManager> = lazy { AssetManager(context.value) },
    val cacheManager: Lazy<CacheManager> = lazy { CacheManager(context.value) },
    val clipboardManager: Lazy<ClipboardManager> = lazy { ClipboardManager(context.value) },
    var editorInstance: Lazy<EditorInstance> = lazy { EditorInstance(context.value) },
    val extensionManager: Lazy<ExtensionManager> = lazy { ExtensionManager(context.value) },
    val glideTypingManager: Lazy<GlideTypingManager> = lazy { GlideTypingManager(context.value) },
    val keyboardManager: Lazy<KeyboardManager> = lazy { KeyboardManager(context.value) },
    val nlpManager: Lazy<NlpManager> = lazy { NlpManager(context.value) },
    val subtypeManager: Lazy<SubtypeManager> = lazy { SubtypeManager(context.value) },
    val themeManager: Lazy<ThemeManager> = lazy { ThemeManager(context.value) },
) {
    companion object {
        private const val ICU_DATA_ASSET_PATH = "icu4c/icudt73l.dat"

        private external fun nativeInitICUData(path: NativeStr): Int

        init {
            try {
                System.loadLibrary("florisboard-native")
            } catch (_: Exception) {
            }
        }
    }

    private val prefs by florisPreferenceModel()
    private val mainHandler by lazy { Handler(context.value.mainLooper) }

    fun initialize(
        installCrashUtility: Boolean = false,
    ) {
        val appContext = context.value
        FlorisManagerReference = WeakReference(this)
        try {
            JetPref.configure(saveIntervalMs = 500)
            Flog.install(
                context = appContext,
                isFloggingEnabled = BuildConfig.DEBUG,
                flogTopics = LogTopic.ALL,
                flogLevels = Flog.LEVEL_ALL,
                flogOutputs = Flog.OUTPUT_CONSOLE,
            )
            if (installCrashUtility) {
                CrashUtility.install(appContext)
            }
            FlorisEmojiCompat.init(appContext)

            if (!UserManagerCompat.isUserUnlocked(appContext)) {
                appContext.cacheDir?.deleteContentsRecursively()
                appContext.extensionManager().value.init()
                appContext.registerReceiver(BootComplete(), IntentFilter(Intent.ACTION_USER_UNLOCKED))
            } else {
                init()
            }
        } catch (e: Exception) {
            if (installCrashUtility) {
                CrashUtility.stageException(e)
            }
        }
    }

    private fun init() {
        val appContext = context.value
        initICU(appContext)
        appContext.cacheDir?.deleteContentsRecursively()
        prefs.initializeBlocking(appContext)
        appContext.extensionManager().value.init()
        appContext.clipboardManager().value.initializeForContext(appContext)
        DictionaryManager.init(appContext)
    }

    private fun initICU(context: Context): Boolean {
        try {
            val androidAssetManager = context.assets ?: return false
            val icuTmpDataFile = context.cacheDir.subFile("icudt.dat")
            icuTmpDataFile.outputStream().use { os ->
                androidAssetManager.open(ICU_DATA_ASSET_PATH).use { it.copyTo(os) }
            }
            val status = nativeInitICUData(icuTmpDataFile.absolutePath.toNativeStr())
            icuTmpDataFile.delete()
            return if (status != 0) {
                flogError { "Native ICU data initializing failed with error code $status!" }
                false
            } else {
                flogInfo { "Successfully loaded ICU data!" }
                true
            }
        } catch (e: Exception) {
            flogError { e.toString() }
            return false
        }
    }

    inner class BootComplete : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            if (intent.action == Intent.ACTION_USER_UNLOCKED) {
                try {
                    this@FlorisManager.context.value.unregisterReceiver(this)
                } catch (e: Exception) {
                    flogError { e.toString() }
                }
                mainHandler.post { init() }
            }
        }
    }
}

private tailrec fun Context.florisManager(): FlorisManager {
    return when (this) {
        is FlorisManagerProvider -> this.florisManager()
        is ContextWrapper -> when {
            this.baseContext != null -> this.baseContext.florisManager()
            else -> FlorisManagerReference.get()!!
        }

        else -> tryOrNull { (this.applicationContext as FlorisManagerProvider).florisManager() }
            ?: FlorisManagerReference.get()!!
    }
}

fun Context.appContext() = lazyOf(this.applicationContext)

fun Context.assetManager() = this.florisManager().assetManager

fun Context.cacheManager() = this.florisManager().cacheManager

fun Context.clipboardManager() = this.florisManager().clipboardManager

fun Context.editorInstance() = this.florisManager().editorInstance

fun Context.extensionManager() = this.florisManager().extensionManager

fun Context.glideTypingManager() = this.florisManager().glideTypingManager

fun Context.keyboardManager() = this.florisManager().keyboardManager

fun Context.nlpManager() = this.florisManager().nlpManager

fun Context.subtypeManager() = this.florisManager().subtypeManager

fun Context.themeManager() = this.florisManager().themeManager
