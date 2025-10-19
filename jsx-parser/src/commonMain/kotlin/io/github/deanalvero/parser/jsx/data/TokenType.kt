package io.github.deanalvero.parser.jsx.data

enum class TokenType {
    OpenAngle, CloseAngle, Slash, Equals,
    OpenBrace, CloseBrace,
    Identifier, String, Text, ExpressionContent,
    EOF
}