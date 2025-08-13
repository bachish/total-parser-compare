package runners

import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.serialization.Serializable
import measure.ErrorInfo
import measure.ParseError
import parsers.AnalyzerType
import parsers.AnalyzerType.*
import parsers.ParserFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Run all experiments on given dataset in yaml-format.
 */
fun main(args: Array<String>) {
    CollectResult().main(args)
}

/**
 * Parsers which can be used in evluation.
 */
val parsers = listOf(
    AntlrJava8Analyzer, AntlrJavaAnalyzer, JavacAnalyzer,
    //JDTAnalyzer,
    TreeSitterAnalyzer
)

class CollectResult : CliktCommand() {
    private val input by option("-i", "--input").path(mustExist = true)
    private val output by option("-o", "--output").path()

    override fun run() {
        if (input == null) {
            echo("No input provided (-i)", err = true)
            return
        }

        if (output == null) {
            echo("No output provided (-o)", err = true)
            return
        }


        when {
            input!!.exists() && !input!!.isDirectory() -> {
                echo("Input path exists and is not a directory", err = true)
                return
            }

            !output!!.exists() -> Files.createDirectories(output!!)
        }
        evaluate(input!!, output!!)
    }
}

fun testRun() {
    evaluate(Path.of("gen","dataset"), Path.of("gen", "results"))
}

fun evaluate(pathWithData: Path, pathToStoreResults: Path) {
    for (testCase in Files.list(pathWithData)) {
        evaluateForAllParsers(testCase.readText(), testCase.nameWithoutExtension, pathToStoreResults)
    }
}


fun evaluateForAllParsers(yamlContent: String, yamlName: String, output: Path) {
    val request = Yaml.default.decodeFromString(EvaluateRecoveryRequest.serializer(), yamlContent)
    for (parser in parsers) {
        val pathToSave = output.resolve(yamlName + "_${parser}")
        if (pathToSave.exists()) {
            continue
        }
        val result = evaluate(request, parser, yamlName)
        val yamlResult = Yaml.default.encodeToString(EvaluateRecoveryResponse.serializer(), result)
        pathToSave.writeText(yamlResult)
    }
}

fun evaluate(
    request: EvaluateRecoveryRequest, parserType: AnalyzerType, originalYamlName: String? = null
): EvaluateRecoveryResponse {
    val parser = ParserFactory.create(parserType)
    val speedCorrect: Long = parser.measureParse(request.originalCode)
    val speedOnError: Long = parser.measureParse(request.brokenSnippet)
    val collectedErrors: List<ErrorInfo> = parser.getErrors(request.brokenSnippet)
    val ted: Double = parser.getTreeEditDistance(request.originalCode, request.brokenSnippet)
    val similarityScore: Double
    val similarityScoreWithCorrectCode: Double
    if(parser is parsers.javac.JavacAnalyzer) {
        //javac does not save most terminals in leaves,
        //so we can't calculate similarity for it in old way
        similarityScore = -1.0
        similarityScoreWithCorrectCode = -1.0
    }
    else{
        similarityScore = parser.calculateSimilarity(request.brokenSnippet)
        similarityScoreWithCorrectCode = parser.calculateSimilarity(request.originalCode, request.brokenSnippet)
    }

    return EvaluateRecoveryResponse(
        request.error,
        request.dataset,
        request.originalCode,
        request.brokenSnippet,
        speedCorrect,
        speedOnError,
        collectedErrors,
        ted,
        similarityScore,
        similarityScoreWithCorrectCode,
        parserType,
        originalYamlName
    )
}

@Serializable
data class EvaluateRecoveryRequest(
    val error: ParseError,
    val dataset: String,
    val originalCode: String,
    val brokenSnippet: String,
)

@Serializable
data class EvaluateRecoveryResponse(
    val originalError: ParseError,
    val dataset: String,
    val originalCode: String,
    val brokenSnippet: String,
    val speedCorrect: Long,
    val speedOnError: Long,
    val collectedErrors: List<ErrorInfo>,
    val ted: Double,
    val similarityScore: Double,
    val similarityScoreWithCorrectCode: Double,
    val parser: AnalyzerType,
    val originalYamlName: String?
)



