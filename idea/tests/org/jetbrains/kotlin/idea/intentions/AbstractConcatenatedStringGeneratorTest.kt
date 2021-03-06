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

package org.jetbrains.kotlin.idea.intentions

import junit.framework.TestCase
import org.jetbrains.kotlin.idea.intentions.copyConcatenatedStringToClipboard.ConcatenatedStringGenerator
import org.jetbrains.kotlin.idea.test.KotlinCodeInsightTestCase
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.io.File

abstract class AbstractConcatenatedStringGeneratorTest : KotlinCodeInsightTestCase() {

    @Throws(Exception::class)
    protected fun doTest(path: String) {
        val mainFile = File(path)
        val readText = mainFile.readText()
        val resultFile = File("$path.result")
        val expectedText = resultFile.readText()
        val expression = KtPsiFactory(project).createExpression(readText) as? KtBinaryExpression
        TestCase.assertNotNull("Invalid expression: $readText", expression)
        val generatedString = ConcatenatedStringGenerator().create(expression!!)
        TestCase.assertEquals("mismatch '$expectedText' - '$generatedString'", expectedText, generatedString)
    }
}