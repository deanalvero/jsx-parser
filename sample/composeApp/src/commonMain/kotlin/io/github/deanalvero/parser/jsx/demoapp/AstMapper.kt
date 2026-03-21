package io.github.deanalvero.parser.jsx.demoapp

import io.github.deanalvero.parser.jsx.attributevalue.JsxExpressionContainer
import io.github.deanalvero.parser.jsx.attributevalue.JsxStringLiteral
import io.github.deanalvero.parser.jsx.node.JsxAttribute
import io.github.deanalvero.parser.jsx.node.JsxElement
import io.github.deanalvero.parser.jsx.node.JsxExpression
import io.github.deanalvero.parser.jsx.node.JsxNode
import io.github.deanalvero.parser.jsx.node.JsxText
import io.github.deanalvero.parser.jsx.node.expression.JsxExpressionNode

fun JsxNode.toView(): AstViewNode =
    when (this) {
        is JsxElement -> AstViewNode(
            label = "Element: $name",
            children = attributes.map { it.toView() } +
                    children.map { it.toView() }
        )
        is JsxText ->
            AstViewNode("Text: \"${text.trim()}\"")
        is JsxExpression ->
            AstViewNode("Expression", listOf(node.toView()))
        else ->
            AstViewNode(this::class.simpleName ?: "Node")
    }

private fun JsxAttribute.toView(): AstViewNode {
    val valueNode = when (val v = value) {
        is JsxStringLiteral -> AstViewNode("String: ${v.value}")
        is JsxExpressionContainer -> AstViewNode("Expr", listOf(v.node.toView()))
    }
    return AstViewNode("Attr: $key", listOf(valueNode))
}

private fun JsxExpressionNode.toView(): AstViewNode =
    when (this) {
        is JsxExpressionNode.Identifier ->
            AstViewNode("Identifier($name)")
        is JsxExpressionNode.MemberExpression ->
            AstViewNode("Member(${parts.joinToString(".")})")
        is JsxExpressionNode.StringLiteral ->
            AstViewNode("String(\"$value\")")
        is JsxExpressionNode.NumberLiteral ->
            AstViewNode("Number($value)")
        is JsxExpressionNode.CallExpression ->
            AstViewNode(
                "Call($callee)",
                arguments.map { it.toView() }
            )
        is JsxExpressionNode.ObjectLiteral ->
            AstViewNode(
                "Object",
                properties.map { (k, v) ->
                    AstViewNode(k, listOf(v.toView()))
                }
            )
        is JsxExpressionNode.Unknown ->
            AstViewNode("Unknown($source)")
    }
