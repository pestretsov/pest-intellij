package com.pestphp.pest.features.mutate

import com.intellij.execution.PsiLocation
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.psi.PsiElement
import com.intellij.testFramework.TestDataPath
import com.jetbrains.php.config.interpreters.PhpInterpreter
import com.jetbrains.php.config.interpreters.PhpInterpretersManagerImpl
import com.jetbrains.php.testFramework.PhpTestFrameworkConfiguration
import com.jetbrains.php.testFramework.PhpTestFrameworkSettingsManager
import com.jetbrains.php.testFramework.PhpTestFrameworkVersionCache
import com.pestphp.pest.PestFrameworkType
import com.pestphp.pest.PestLightCodeFixture
import com.pestphp.pest.configuration.PestRunConfiguration
import com.pestphp.pest.configuration.PestRunConfigurationProducer

@TestDataPath("/com/pestphp/pest/features/mutate")
class PestParallelProgramRunnerTest : PestLightCodeFixture() {
    private lateinit var configurationsBackup: List<PhpTestFrameworkConfiguration>

    override fun getTestDataPath(): String = "src/test/resources/com/pestphp/pest/features/mutate"

    fun testCannotRunWrongExecutorId() = doTest {
        val configuration = createConfiguration(myFixture.file, "3.0.0")
        assertFalse(PestMutateProgramRunner().canRun(PestMutateTestExecutor.EXECUTOR_ID + "1", configuration))
    }

    fun testCannotRunOldVersion() = doTest {
        val configuration = createConfiguration(myFixture.file, "2.27.2")
        assertFalse(PestMutateProgramRunner().canRun(PestMutateTestExecutor.EXECUTOR_ID + "1", configuration))
    }

    fun testCanRunFile() = doTest {
        val configuration = createConfiguration(myFixture.file, "3.0.0")
        assertTrue(PestMutateProgramRunner().canRun(PestMutateTestExecutor.EXECUTOR_ID, configuration))
    }

    fun testCanRunFunction() = doTest {
        val testElement = myFixture.file?.firstChild?.lastChild?.firstChild ?: return@doTest
        val configuration = createConfiguration(testElement, "3.0.0")
        assertTrue(PestMutateProgramRunner().canRun(PestMutateTestExecutor.EXECUTOR_ID, configuration))
    }

    fun testCanRunDirectory() = doTest {
        val testElement = myFixture.file?.containingDirectory ?: return@doTest
        val configuration = createConfiguration(testElement, "3.0.0")
        assertTrue(PestMutateProgramRunner().canRun(PestMutateTestExecutor.EXECUTOR_ID, configuration))
    }

    private fun createConfiguration(psiElement: PsiElement, version: String): PestRunConfiguration {
        createPestFrameworkConfiguration()
        val context = ConfigurationContext.createEmptyContextForLocation(PsiLocation.fromPsiElement(psiElement))
        val runConfiguration = PestRunConfigurationProducer().createConfigurationFromContext(context)?.configuration
            as? PestRunConfiguration
        runConfiguration!!.settings.commandLineSettings.interpreterSettings.interpreterName = getTestName(false)
        val pestConfiguration = PhpTestFrameworkSettingsManager.getInstance(project).getOrCreateByInterpreter(
            PestFrameworkType.instance,
            runConfiguration.interpreter,
            runConfiguration.getBaseFile(null, runConfiguration.interpreter),
            true
        ) ?: return runConfiguration
        PhpTestFrameworkVersionCache.setCache(project, pestConfiguration, version)
        return runConfiguration
    }

    private fun doTest(block: () -> Unit) {
        val file = myFixture.configureByFile("ATest.php")
        myFixture.openFileInEditor(file.virtualFile)
        block()
    }

    override fun setUp() {
        super.setUp()
        val interpreter = PhpInterpreter().apply {
            name = getTestName(false)
            homePath = "$testDataPath/php"
        }
        PhpInterpretersManagerImpl.getInstance(project).addInterpreter(interpreter)
        configurationsBackup = PhpTestFrameworkSettingsManager.getInstance(project).getConfigurations(PestFrameworkType.instance)
    }

    override fun tearDown() {
        try {
            PhpTestFrameworkSettingsManager.getInstance(project).setConfigurations(PestFrameworkType.instance, configurationsBackup)
            PhpInterpretersManagerImpl.getInstance(project).interpreters = emptyList()
        } catch (e: Throwable) {
            addSuppressedException(e)
        } finally {
            super.tearDown()
        }
    }
}
