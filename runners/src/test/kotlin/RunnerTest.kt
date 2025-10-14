import measure.MISSING_SEMICOLON
import org.junit.jupiter.api.Test
import runners.EvaluateRecoveryRequest
import runners.evaluateForAllParsers
import java.nio.file.Path

class RunnerTest {
    @Test
    fun evaluateOneFileTest() {
        evaluateForAllParsers(
            EvaluateRecoveryRequest(
                MISSING_SEMICOLON,
                "one_file_dataset",
                "class Main {int x;}",
                "class Main {int x}"
            ),
            "missed_semicolon", Path.of("gen", "results")
        )
    }
}