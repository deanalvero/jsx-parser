package io.github.deanalvero.parser.jsx.node.expression

object ExpressionParser {
    private val IDENTIFIER = Regex("^[A-Za-z_$][A-Za-z0-9_$]*$")
    private val MEMBER = Regex("^[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)+$")
    private val NUMBER = Regex("^-?\\d+(\\.\\d+)?$")
    private val CALL = Regex("^([A-Za-z_$][A-Za-z0-9_$.]*)\\((.*)\\)$")

    fun parse(raw: String): JsxExpressionNode {
        val text = raw.trim()
        if (text.isEmpty()) {
            return JsxExpressionNode.Unknown(raw)
        }
        parseStringLiteral(text)?.let { return it }
        parseNumberLiteral(text)?.let { return it }
        parseObjectLiteral(text)?.let { return it }
        parseCallExpression(text)?.let { return it }
        parseMember(text)?.let { return it }
        parseIdentifier(text)?.let { return it }
        return JsxExpressionNode.Unknown(raw)
    }

    private fun parseStringLiteral(text: String): JsxExpressionNode.StringLiteral? {
        if ((text.startsWith("\"") && text.endsWith("\"")) ||
            (text.startsWith("'") && text.endsWith("'"))
        ) {
            return JsxExpressionNode.StringLiteral(
                text.substring(1, text.length - 1)
            )
        }
        return null
    }

    private fun parseNumberLiteral(text: String): JsxExpressionNode.NumberLiteral? {
        if (NUMBER.matches(text)) {
            return JsxExpressionNode.NumberLiteral(text.toDouble())
        }
        return null
    }

    private fun parseIdentifier(text: String): JsxExpressionNode.Identifier? {
        if (IDENTIFIER.matches(text)) {
            return JsxExpressionNode.Identifier(text)
        }
        return null
    }

    private fun parseMember(text: String): JsxExpressionNode.MemberExpression? {
        if (MEMBER.matches(text)) {
            return JsxExpressionNode.MemberExpression(text.split('.'))
        }
        return null
    }

    private fun parseCallExpression(text: String): JsxExpressionNode.CallExpression? {
        val match = CALL.matchEntire(text) ?: return null
        val callee = match.groupValues[1]
        val argsRaw = match.groupValues[2]
        val args = splitTopLevel(argsRaw)
            .filter { it.isNotBlank() }
            .map { parse(it) }
        return JsxExpressionNode.CallExpression(callee, args)
    }

    private fun parseObjectLiteral(text: String): JsxExpressionNode.ObjectLiteral? {
        if (!text.startsWith("{") || !text.endsWith("}")) return null
        val inner = text.substring(1, text.length - 1).trim()
        if (inner.isEmpty()) {
            return JsxExpressionNode.ObjectLiteral(emptyMap())
        }
        val props = mutableMapOf<String, JsxExpressionNode>()
        val entries = splitTopLevel(inner)

        for (entry in entries) {
            val colon = entry.indexOf(':')
            if (colon <= 0) return null
            val key = entry.substring(0, colon).trim()
            val value = entry.substring(colon + 1).trim()
            val normalizedKey =
                key.removeSurrounding("\"")
                    .removeSurrounding("'")
            props[normalizedKey] = parse(value)
        }
        return JsxExpressionNode.ObjectLiteral(props)
    }

    private fun splitTopLevel(input: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()

        var brace = 0
        var paren = 0
        var quote: Char? = null

        for (c in input) {
            if (quote != null) {
                if (c == quote) quote = null
                sb.append(c)
                continue
            }
            when (c) {
                '"', '\'' -> {
                    quote = c
                    sb.append(c)
                }
                '{' -> {
                    brace++
                    sb.append(c)
                }
                '}' -> {
                    brace--
                    sb.append(c)
                }
                '(' -> {
                    paren++
                    sb.append(c)
                }
                ')' -> {
                    paren--
                    sb.append(c)
                }
                ',' -> {
                    if (brace == 0 && paren == 0) {
                        result += sb.toString()
                        sb.clear()
                    } else {
                        sb.append(c)
                    }
                }
                else -> sb.append(c)
            }
        }
        if (sb.isNotEmpty()) {
            result += sb.toString()
        }
        return result
    }
}