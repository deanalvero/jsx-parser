package io.github.deanalvero.parser.jsx

import io.github.deanalvero.parser.jsx.node.JsxNode

object JsxParser {
    fun parse(source: String): Result<List<JsxNode>> {
        return try {
            val tokens = Lexer(source).scanTokens()
            val nodes = Parser(tokens).parse()
            Result.success(nodes)
        } catch (e: JsxParseError) {
            Result.failure(e)
        }
    }
}
