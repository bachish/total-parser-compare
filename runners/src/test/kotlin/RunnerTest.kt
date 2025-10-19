import measure.MISSING_SEMICOLON
import org.junit.jupiter.api.Test
import runners.evaluateForAllParsers
import java.nio.file.Path
import dev.errecfuzz.EvaluateRecoveryRequest

class RunnerTest {
    @Test
    fun evaluateOneFileTest() {
        evaluateForAllParsers(
            EvaluateRecoveryRequest(
                MISSING_SEMICOLON,
                "one_file_dataset",
                "class Main {int x;}",
                """
                    package org.junit.tests.experimental.theories.extendingwithstubs;

                    public interface Correspondent { {

                        String getAnswer(String question, String... bucket);

                    }
                """.trimIndent()
            ),
            "missed_semicolon", Path.of("gen", "results")
        )
    }
}

