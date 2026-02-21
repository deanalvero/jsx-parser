package io.github.deanalvero.parser.jsx.attributevalue

import io.github.deanalvero.parser.jsx.node.expression.JsxExpressionNode

data class JsxExpressionContainer(
    val expression: String,
    val node: JsxExpressionNode
) : JsxAttributeValue
