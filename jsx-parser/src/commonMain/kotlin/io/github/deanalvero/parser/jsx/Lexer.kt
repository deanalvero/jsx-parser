package io.github.deanalvero.parser.jsx

import io.github.deanalvero.parser.jsx.data.Token
import io.github.deanalvero.parser.jsx.data.TokenType

internal class Lexer(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1
    private var lineStart = 0
    private enum class Mode { CONTENT, TAG }
    private var mode = Mode.CONTENT

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            when (mode) {
                Mode.CONTENT -> scanContent()
                Mode.TAG -> scanTagToken()
            }
        }
        tokens.add(Token(TokenType.EOF, "", SourceLocation(line, current - lineStart + 1)))
        return tokens
    }

    private fun scanContent() {
        val contentStart = current
        val sb = StringBuilder()

        while (!isAtEnd() && peek() != '<' && peek() != '{') {
            val c = advance()
            if (c == '\n') {
                line++
                lineStart = current
            }
            sb.append(c)
        }

        if (sb.isNotEmpty()) {
            val text = sb.toString().trim()
            if (text.isNotEmpty()) {
                addToken(TokenType.Text, text, contentStart)
            }
        }

        if (isAtEnd()) return

        when (peek()) {
            '<' -> {
                advance()
                addToken(TokenType.OpenAngle, "<", current - 1)
                mode = Mode.TAG
            }
            '{' -> {
                advance()
                addToken(TokenType.OpenBrace, "{", current - 1)
                scanExpressionContent()
            }
        }
    }

    private fun scanTagToken() {
        while (!isAtEnd()) {
            start = current
            val c = advance()
            when (c) {
                '<' -> addToken(TokenType.OpenAngle, "<", start)
                '>' -> {
                    addToken(TokenType.CloseAngle, ">", start)
                    mode = Mode.CONTENT
                    return
                }
                '/' -> addToken(TokenType.Slash, "/", start)
                '=' -> addToken(TokenType.Equals, "=", start)
                '{' -> {
                    addToken(TokenType.OpenBrace, "{", start)
                    scanExpressionContent()
                }
                '}' -> addToken(TokenType.CloseBrace, "}", start)
                '"', '\'' -> {
                    string(c, start)
                }
                ' ', '\r', '\t' -> { /* skip */ }
                '\n' -> {
                    line++
                    lineStart = current
                }
                else -> {
                    if (isIdentifierStart(c)) {
                        identifier(start, initialChar = c)
                    } else if (isIdentifierPart(c)) {
                        identifier(start, initialChar = c)
                    } else {
                        addToken(TokenType.Identifier, c.toString(), start)
                    }
                }
            }
            if (mode == Mode.CONTENT) return
        }
    }

    private fun scanExpressionContent() {
        val exprStart = current
        val exprLoc = SourceLocation(line, current - lineStart + 1)
        val sb = StringBuilder()
        var braceLevel = 1

        while (!isAtEnd()) {
            val ch = peek()
            when (ch) {
                '{' -> {
                    braceLevel++
                    sb.append(advance())
                }
                '}' -> {
                    braceLevel--
                    if (braceLevel == 0) {
                        val content = sb.toString()
                        if (content.isNotEmpty()) {
                            tokens.add(Token(TokenType.ExpressionContent, content, exprLoc))
                        }
                        advance()
                        addToken(TokenType.CloseBrace, "}", current - 1)
                        return
                    } else {
                        sb.append(advance())
                    }
                }
                '\n' -> {
                    sb.append(advance())
                    line++
                    lineStart = current
                }
                '\'', '"' -> {
                    sb.append(consumeStringInExpression(advance()))
                }
                '`' -> {
                    sb.append(consumeTemplateLiteral())
                }
                '/' -> {
                    val next = if (current + 1 < source.length) source[current + 1] else '\u0000'
                    if (next == '/') {
                        sb.append(consumeLineComment())
                    } else if (next == '*') {
                        sb.append(consumeBlockComment())
                    } else {
                        sb.append(advance())
                    }
                }
                else -> sb.append(advance())
            }
        }

        throw JsxParseError("Unmatched '{' in expression.", exprLoc.line, exprLoc.column)
    }

    private fun consumeStringInExpression(open: Char): String {
        val sb = StringBuilder()
        sb.append(open)
        while (!isAtEnd()) {
            val c = advance()
            sb.append(c)
            if (c == '\\') {
                if (!isAtEnd()) sb.append(advance())
            } else if (c == open) {
                return sb.toString()
            } else if (c == '\n') {
                line++
                lineStart = current
            }
        }
        throw JsxParseError("Unterminated string in expression.", line, current - lineStart + 1)
    }

    private fun consumeTemplateLiteral(): String {
        val sb = StringBuilder()
        sb.append(advance())
        while (!isAtEnd()) {
            val c = advance()
            sb.append(c)
            if (c == '\\') {
                if (!isAtEnd()) sb.append(advance())
            } else if (c == '`') {
                return sb.toString()
            } else if (c == '$' && peek() == '{') {
                sb.append(advance())
                var nested = 1
                while (!isAtEnd() && nested > 0) {
                    val ch = peek()
                    when (ch) {
                        '{' -> { nested++; sb.append(advance()) }
                        '}' -> { nested--; sb.append(advance()) }
                        '\'' , '"' -> sb.append(consumeStringInExpression(advance()))
                        '`' -> sb.append(consumeTemplateLiteral())
                        '/' -> {
                            val nx = if (current + 1 < source.length) source[current + 1] else '\u0000'
                            if (nx == '/') sb.append(consumeLineComment()) else if (nx == '*') sb.append(consumeBlockComment()) else sb.append(advance())
                        }
                        '\n' -> { sb.append(advance()); line++; lineStart = current }
                        else -> sb.append(advance())
                    }
                }
            } else if (c == '\n') {
                line++
                lineStart = current
            }
        }
        throw JsxParseError("Unterminated template literal in expression.", line, current - lineStart + 1)
    }

    private fun consumeLineComment(): String {
        val sb = StringBuilder()
        sb.append(advance())
        sb.append(advance())
        while (!isAtEnd()) {
            val c = peek()
            if (c == '\n') break
            sb.append(advance())
        }
        return sb.toString()
    }

    private fun consumeBlockComment(): String {
        val sb = StringBuilder()
        sb.append(advance())
        sb.append(advance())
        while (!isAtEnd()) {
            val c = advance()
            sb.append(c)
            if (c == '*' && peek() == '/') {
                sb.append(advance())
                return sb.toString()
            }
            if (c == '\n') {
                line++
                lineStart = current
            }
        }
        throw JsxParseError("Unterminated block comment in expression.", line, current - lineStart + 1)
    }

    private fun identifier(tokenStart: Int, initialChar: Char) {
        val sb = StringBuilder()
        sb.append(initialChar)
        while (!isAtEnd() && isIdentifierPart(peek())) {
            sb.append(advance())
        }
        addToken(TokenType.Identifier, sb.toString(), tokenStart)
    }

    private fun string(quoteChar: Char, tokenStart: Int) {
        val stringLoc = SourceLocation(line, tokenStart - lineStart + 1)
        val sb = StringBuilder()
        while (!isAtEnd()) {
            val c = advance()
            if (c == '\\') {
                if (isAtEnd()) break
                val next = advance()
                val escaped = when (next) {
                    'n' -> '\n'
                    'r' -> '\r'
                    't' -> '\t'
                    '\\' -> '\\'
                    '"' -> '"'
                    '\'' -> '\''
                    else -> next
                }
                sb.append(escaped)
            } else if (c == quoteChar) {
                addToken(TokenType.String, sb.toString(), tokenStart)
                return
            } else {
                if (c == '\n') {
                    line++
                    lineStart = current
                }
                sb.append(c)
            }
        }
        throw JsxParseError("Unterminated string.", stringLoc.line, stringLoc.column)
    }

    private fun addToken(type: TokenType, literal: String, tokenStartOverride: Int? = null) {
        val tokenStart = tokenStartOverride ?: start
        tokens.add(Token(type, literal, SourceLocation(line, tokenStart - lineStart + 1)))
    }

    private fun currentLocation() = SourceLocation(line, current - lineStart + 1)
    private fun isIdentifierStart(c: Char) = c.isLetter() || c == '_' || c == '$'
    private fun isIdentifierPart(c: Char) = c.isLetterOrDigit() || c == '_' || c == '-' || c == '.' || c == ':' || c == '$'
    private fun isAtEnd() = current >= source.length
    private fun advance(): Char = source[current++]
    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[current]
}
