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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.*

/**
 * Run all experiments on given dataset in yaml-format.
 */
fun main(args: Array<String>) {
    CollectResult().main(args)
}

val sdf = SimpleDateFormat("dd-M-yyyy_hh-mm-ss")
val currentDate = sdf.format(Date())
fun getResultFileName(output: Path, yamlName: String, parser: AnalyzerType): Path {
    return output
        // .resolve(currentDate.toString()) //if we need folder per date
        .resolve(yamlName + "_${parser}")
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

fun evaluate(pathWithData: Path, pathToStoreResults: Path) {
    for (testCase in Files.list(pathWithData)) {
        evaluateForAllParsers(testCase.readText(), testCase.nameWithoutExtension, pathToStoreResults)
    }
}
fun evaluateForAllParsers(yamlContent: String, yamlName: String, output: Path) {
    val request = Yaml.default.decodeFromString(EvaluateRecoveryRequest.serializer(), yamlContent)
    evaluateForAllParsers(request, yamlName, output)
}

fun evaluateForAllParsers(request: EvaluateRecoveryRequest, yamlName: String, output: Path) {
    for (parser in parsers) {
        val pathToSave = getResultFileName(output, yamlName, parser)
        if (pathToSave.exists()) {
            continue
        }
        Files.createDirectories(pathToSave.parent)
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
    val similarityScore_LexerVsParserTokens: Double
    val similarityScore_RecoveredVsCorrectCodeTokens: Double
    if(parser is parsers.javac.JavacAnalyzer) {
        //javac does not save most terminals in leaves,
        //so we can't calculate similarity for it in old way
        similarityScore_LexerVsParserTokens = -1.0
        similarityScore_RecoveredVsCorrectCodeTokens = -1.0
    }
    else{
        similarityScore_LexerVsParserTokens = parser.calculateSimilarity(request.brokenSnippet)
        similarityScore_RecoveredVsCorrectCodeTokens = parser.calculateSimilarity(request.originalCode, request.brokenSnippet)
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
        similarityScore_LexerVsParserTokens,
        similarityScore_RecoveredVsCorrectCodeTokens,
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
    val similarityScore_LexerVsParserTokens: Double,
    val similarityScore_RecoveredVsCorrectCodeTokens: Double,
    val parser: AnalyzerType,
    val originalYamlName: String?
)



