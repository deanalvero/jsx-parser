package io.github.deanalvero.parser.jsx.node

import io.github.deanalvero.parser.jsx.SourceLocation

data class JsxText(
    val text: String,
    override val location: SourceLocation
) : JsxNode
