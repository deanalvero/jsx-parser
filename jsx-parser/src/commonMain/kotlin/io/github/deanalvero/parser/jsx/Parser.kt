package io.github.deanalvero.parser.jsx

import io.github.deanalvero.parser.jsx.attributevalue.JsxExpressionContainer
import io.github.deanalvero.parser.jsx.attributevalue.JsxStringLiteral
import io.github.deanalvero.parser.jsx.data.Token
import io.github.deanalvero.parser.jsx.data.TokenType
import io.github.deanalvero.parser.jsx.node.JsxAttribute
import io.github.deanalvero.parser.jsx.node.JsxElement
import io.github.deanalvero.parser.jsx.node.JsxExpression
import io.github.deanalvero.parser.jsx.node.JsxNode
import io.github.deanalvero.parser.jsx.node.JsxText

internal class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): List<JsxNode> {
        val nodes = mutableListOf<JsxNode>()
        while (!isAtEnd()) {
            nodes.add(parseNode())
        }
        return nodes
    }

    private fun parseNode(): JsxNode = when {
        check(TokenType.OpenAngle) -> parseElement()
        check(TokenType.OpenBrace) -> parseExpression()
        check(TokenType.Text) -> parseText()
        else -> error(peek(), "Unexpected token while parsing node.")
    }

    private fun parseText(): JsxText {
        val token = consume(TokenType.Text, "Expected text node.")
        return JsxText(token.lexeme, token.location)
    }

    private fun parseExpression(): JsxExpression {
        val openBrace = consume(TokenType.OpenBrace, "Expected '{' to start an expression.")
        val expression = if (check(TokenType.ExpressionContent)) {
            advance().lexeme
        } else {
            ""
        }
        consume(TokenType.CloseBrace, "Expected '}' to close an expression.")
        return JsxExpression(expression, openBrace.location)
    }

    private fun parseElement(): JsxElement {
        val openAngle = consume(TokenType.OpenAngle, "Expected '<' to start an element.")

        if (check(TokenType.CloseAngle)) {
            consume(TokenType.CloseAngle, "Expected '>' for fragment start.")
            val children = parseChildrenFragment()
            consume(TokenType.OpenAngle, "Expected '<' to start closing fragment.")
            consume(TokenType.Slash, "Expected '/' in closing fragment.")
            consume(TokenType.CloseAngle, "Expected '>' to close fragment.")
            return JsxElement("", emptyList(), children, openAngle.location)
        }

        val tagToken = consume(TokenType.Identifier, "Expected element tag name.")
        val tagName = tagToken.lexeme

        val attributes = parseAttributes()

        if (match(TokenType.Slash)) {
            consume(TokenType.CloseAngle, "Expected '>' to close self-closing tag.")
            return JsxElement(tagName, attributes, emptyList(), openAngle.location)
        }

        consume(TokenType.CloseAngle, "Expected '>' to close opening tag.")

        val children = parseChildren(tagName)

        consume(TokenType.OpenAngle, "Expected '<' to start closing tag.")
        consume(TokenType.Slash, "Expected '/' for closing tag.")
        val closingTagName = consume(TokenType.Identifier, "Expected element name for closing tag.")
        if (closingTagName.lexeme != tagName) {
            error(closingTagName, "Mismatched closing tag. Expected </$tagName> but got </${closingTagName.lexeme}>.")
        }
        consume(TokenType.CloseAngle, "Expected '>' to close closing tag.")

        return JsxElement(tagName, attributes, children, openAngle.location)
    }

    private fun parseAttributes(): List<JsxAttribute> {
        val attrs = mutableListOf<JsxAttribute>()
        while (true) {
            if (check(TokenType.Identifier)) {
                val keyTok = advance()
                val key = keyTok.lexeme
                if (check(TokenType.Equals)) {
                    consume(TokenType.Equals, "Expected '=' after attribute name '$key'.")
                    val value = when {
                        check(TokenType.String) -> JsxStringLiteral(advance().lexeme)
                        check(TokenType.OpenBrace) -> {
                            consume(TokenType.OpenBrace, "Expected '{' for attribute expression.")
                            val expr = if (check(TokenType.ExpressionContent)) advance().lexeme else ""
                            consume(TokenType.CloseBrace, "Expected '}' to close attribute expression.")
                            JsxExpressionContainer(expr)
                        }
                        else -> error(peek(), "Attribute value must be a string literal or an expression for '$key'.")
                    }
                    attrs.add(JsxAttribute(key, value, keyTok.location))
                } else {
                    attrs.add(JsxAttribute(key, JsxExpressionContainer("true"), keyTok.location))
                }
            } else if (check(TokenType.OpenBrace)) {
                val braceTok = advance()
                val expr = if (check(TokenType.ExpressionContent)) advance().lexeme else ""
                consume(TokenType.CloseBrace, "Expected '}' to close attribute expression.")
                val trimmed = expr.trimStart()
                if (trimmed.startsWith("...")) {
                    val after = trimmed.removePrefix("...").trim()
                    attrs.add(JsxAttribute("...", JsxExpressionContainer(after), braceTok.location))
                } else {
                    attrs.add(JsxAttribute("", JsxExpressionContainer(expr), braceTok.location))
                }
            } else {
                break
            }
        }
        return attrs
    }

    private fun parseChildren(parentTagName: String): List<JsxNode> {
        val children = mutableListOf<JsxNode>()
        while (!isAtEnd() && !(check(TokenType.OpenAngle) && checkNext(TokenType.Slash))) {
            children.add(parseNode())
        }
        return children
    }

    private fun parseChildrenFragment(): List<JsxNode> {
        val children = mutableListOf<JsxNode>()
        while (!isAtEnd()) {
            if (check(TokenType.OpenAngle) && checkNext(TokenType.Slash) && tokens.getOrNull(current + 2)?.type == TokenType.CloseAngle) break
            children.add(parseNode())
        }
        return children
    }

    private fun match(vararg types: TokenType): Boolean {
        for (t in types) if (check(t)) { advance(); return true }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        error(peek(), message)
    }

    private fun check(type: TokenType): Boolean = if (isAtEnd()) false else peek().type == type

    private fun checkNext(type: TokenType): Boolean {
        if (isAtEnd() || current + 1 >= tokens.size) return false
        return tokens[current + 1].type == type
    }

    private fun advance(): Token = if (!isAtEnd()) tokens[current++] else previous()
    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF
    private fun peek(): Token = tokens[current]
    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String): Nothing {
        throw JsxParseError(message, token.location.line, token.location.column)
    }
}
