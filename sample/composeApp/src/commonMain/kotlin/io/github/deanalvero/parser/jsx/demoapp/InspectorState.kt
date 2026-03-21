package io.github.deanalvero.parser.jsx.demoapp

import io.github.deanalvero.parser.jsx.node.JsxNode

data class InspectorState(
    val source: String = "",
    val ast: List<JsxNode>? = null,
    val error: Throwable? = null
)
