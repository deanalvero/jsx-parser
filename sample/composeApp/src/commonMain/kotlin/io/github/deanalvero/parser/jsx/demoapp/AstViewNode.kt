package io.github.deanalvero.parser.jsx.demoapp

data class AstViewNode(
    val label: String,
    val children: List<AstViewNode> = emptyList()
)
