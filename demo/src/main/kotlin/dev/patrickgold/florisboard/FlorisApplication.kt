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

import android.app.Application

class FlorisApplication : Application(), FlorisManagerProvider {
    private val florisManager = FlorisManager(
        context = lazy { this },
        editorInstance = lazy { InterceptEditorInstance(this) },
        keyboardManager = lazy { InterceptKeyboardManager(this) },
    )

    override fun onCreate() {
        super.onCreate()
        florisManager.initialize(
            installCrashUtility = true,
        )
    }

    override fun florisManager(): FlorisManager = florisManager
}
