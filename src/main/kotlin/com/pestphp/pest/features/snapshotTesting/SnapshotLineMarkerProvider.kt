package com.pestphp.pest.features.snapshotTesting

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.PhpUse
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl
import com.pestphp.pest.PestBundle

private class SnapshotLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        if (!PhpPsiUtil.isOfType(element, PhpTokenTypes.IDENTIFIER)) {
            return
        }

        val functionReference = element.parent as? FunctionReferenceImpl ?: return

        if (!functionReference.isSnapshotAssertionCall) {
            return
        }

        if (functionReference.parent is PhpUse) {
            return
        }

        val snapshotFiles = functionReference.snapshotFiles

        val builder = NavigationGutterIconBuilder.create(AllIcons.Nodes.DataSchema)
            .setTargets(snapshotFiles)
            .setTooltipText(PestBundle.message("TOOLTIP_NAVIGATE_TO_SNAPSHOT_FILES"))
        result.add(builder.createLineMarkerInfo(element))
    }
}