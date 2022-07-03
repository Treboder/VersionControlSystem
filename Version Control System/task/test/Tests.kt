import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.exception.outcomes.WrongAnswer
import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testing.TestedProgram
import kotlin.random.Random


class TestStage1 : StageTest<String>() {
    @DynamicTest
    fun helpPageTest(): CheckResult {
        checkHelpPageOutput(TestedProgram().start())
        checkHelpPageOutput(TestedProgram().start("--help"))

        return CheckResult.correct()
    }

    @DynamicTest
    fun configTest(): CheckResult {
        checkOutputString(TestedProgram().start("config"), "Get and set a username.")
        return CheckResult.correct()
    }

    @DynamicTest
    fun addTest(): CheckResult {

        checkOutputString(TestedProgram().start("add"), "Add a file to the index.")
        return CheckResult.correct()
    }

    @DynamicTest
    fun logTest(): CheckResult {
        checkOutputString(TestedProgram().start("log"), "Show commit logs.")
        return CheckResult.correct()
    }

    @DynamicTest
    fun commitTest(): CheckResult {
        checkOutputString(TestedProgram().start("commit"), "Save changes.")
        return CheckResult.correct()
    }

    @DynamicTest
    fun checkoutTest(): CheckResult {
        checkOutputString(TestedProgram().start("checkout"), "Restore a file.")
        return CheckResult.correct()
    }

    @DynamicTest
    fun wrongArgTest(): CheckResult {
        val suffix = Random.nextInt(0,1000)
        checkOutputString(TestedProgram().start("wrongArg$suffix"), "'wrongArg$suffix' is not a SVCS command.")
        return CheckResult.correct()
    }

    private fun prepareString(s: String) = s.trim().split(" ").filter { it.isNotBlank() }.joinToString(" ")

    private fun checkHelpPageOutput(got: String) {
        val helpPage = "These are SVCS commands:\n" +
                "config     Get and set a username.\n" +
                "add        Add a file to the index.\n" +
                "log        Show commit logs.\n" +
                "commit     Save changes.\n" +
                "checkout   Restore a file."

        if (got.isBlank()) {
            throw WrongAnswer(
                    "Your program should output:\n$helpPage\n\n" +
                            "But printed nothing"
            )
        } else if (!prepareString(got).equals(prepareString(helpPage), true)) {
            throw WrongAnswer(
                    "Your program should output:\n$helpPage\n\n" +
                            "But printed:\n$got"
            )
        }
    }

    private fun checkOutputString(got: String, want: String) {
        if (got.isBlank()) {
            throw WrongAnswer(
                    "Your program should output \"$want\",\n" +
                            "but printed nothing"
            )
        } else if (!prepareString(got).equals(want, true)) {
            throw WrongAnswer(
                    "Your program should output \"$want\",\n" +
                            "but printed: \"$got\""
            )
        }
    }

}