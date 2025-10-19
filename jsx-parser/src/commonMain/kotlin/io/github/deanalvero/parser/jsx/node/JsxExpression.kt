package io.github.deanalvero.parser.jsx.node

import io.github.deanalvero.parser.jsx.SourceLocation

data class JsxExpression(
    val expression: String,
    override val location: SourceLocation
) : JsxNode
