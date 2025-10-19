package io.github.deanalvero.parser.jsx.data

import io.github.deanalvero.parser.jsx.SourceLocation

data class Token(
    val type: TokenType,
    val lexeme: String,
    val location: SourceLocation
)
