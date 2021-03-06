/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.inline.clean

import com.google.dart.compiler.backend.js.ast.*
import com.google.dart.compiler.backend.js.ast.metadata.imported

fun removeUnusedImports(root: JsNode) {
    val collector = UsedImportsCollector()
    root.accept(collector)
    NodeRemover(JsVars::class.java) { statement ->
        if (statement.vars.size == 1) {
            val name = statement.vars[0].name
            name.imported && name !in collector.usedImports
        }
        else {
            false
        }
    }.accept(root)
}

private class UsedImportsCollector : RecursiveJsVisitor() {
    val usedImports = mutableSetOf<JsName>()

    override fun visitNameRef(nameRef: JsNameRef) {
        val name = nameRef.name
        if (name != null && name.imported) {
            usedImports += name
        }
        super.visitNameRef(nameRef)
    }
}
