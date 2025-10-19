package io.github.deanalvero.parser.jsx.node

import io.github.deanalvero.parser.jsx.SourceLocation

data class JsxElement(
    val name: String,
    val attributes: List<JsxAttribute>,
    val children: List<JsxNode>,
    override val location: SourceLocation
) : JsxNode
