package io.github.deanalvero.parser.jsx.node

import io.github.deanalvero.parser.jsx.SourceLocation
import io.github.deanalvero.parser.jsx.attributevalue.JsxAttributeValue

data class JsxAttribute(
    val key: String,
    val value: JsxAttributeValue,
    override val location: SourceLocation
) : JsxNode
