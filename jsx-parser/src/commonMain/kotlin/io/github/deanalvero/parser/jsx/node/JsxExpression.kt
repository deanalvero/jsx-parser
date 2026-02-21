package io.github.deanalvero.parser.jsx.node

import io.github.deanalvero.parser.jsx.SourceLocation
import io.github.deanalvero.parser.jsx.node.expression.JsxExpressionNode

data class JsxExpression(
    val expression: String,
    val node: JsxExpressionNode,
    override val location: SourceLocation
) : JsxNode
