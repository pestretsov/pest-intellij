package com.pestphp.pest.templates

import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.postfixCompletion.PhpPostfixUtils
import com.jetbrains.php.postfixCompletion.PhpStringBasedPostfixTemplate
import com.pestphp.pest.isPestTestFile

/**
 * Adds a postfix template for `describe` tests.
 */
class PestDescribePostfixTemplate : PhpStringBasedPostfixTemplate(
    "describe",
    "describe($EXPR, function...)",
    PhpPostfixUtils.selectorTopmost()
) {
    override fun isApplicable(context: PsiElement, copyDocument: Document, newOffset: Int): Boolean {
        return context.containingFile.isPestTestFile() && context.parent is StringLiteralExpression
    }

    override fun getTemplateString(element: PsiElement): String {
        val dollar = "$"

        return """
            describe(${dollar}$EXPR${dollar}, function() {
                ${dollar}END${dollar}
            });
        """.trimIndent()
    }
}