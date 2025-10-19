package io.github.deanalvero.parser.jsx

class JsxParseError(
    override val message: String,
    val line: Int,
    val column: Int
) : RuntimeException(message)
