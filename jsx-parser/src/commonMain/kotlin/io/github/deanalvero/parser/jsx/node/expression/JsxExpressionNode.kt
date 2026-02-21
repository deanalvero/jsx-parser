package io.github.deanalvero.parser.jsx.node.expression

sealed interface JsxExpressionNode {

    data class Identifier(
        val name: String
    ) : JsxExpressionNode

    data class MemberExpression(
        val parts: List<String>
    ) : JsxExpressionNode {
        val root: String get() = parts.first()
        val member: String get() = parts.last()
    }

    data class StringLiteral(
        val value: String
    ) : JsxExpressionNode

    data class NumberLiteral(
        val value: Double
    ) : JsxExpressionNode

    data class ObjectLiteral(
        val properties: Map<String, JsxExpressionNode>
    ) : JsxExpressionNode

    data class CallExpression(
        val callee: String,
        val arguments: List<JsxExpressionNode>
    ) : JsxExpressionNode

    data class Unknown(
        val source: String
    ) : JsxExpressionNode
}
