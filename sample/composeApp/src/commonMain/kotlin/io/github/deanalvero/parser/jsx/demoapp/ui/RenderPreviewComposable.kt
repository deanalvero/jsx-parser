package io.github.deanalvero.parser.jsx.demoapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.deanalvero.parser.jsx.node.JsxElement
import io.github.deanalvero.parser.jsx.node.JsxExpression
import io.github.deanalvero.parser.jsx.node.JsxNode
import io.github.deanalvero.parser.jsx.node.JsxText

@Composable
fun RenderPreviewComposable(ast: List<JsxNode>?) {
    if (ast == null) {
        Text("No preview")
    } else {
        Column {
            ast.forEach { RenderNodeComposable(it) }
        }
    }
}

@Composable
private fun RenderNodeComposable(node: JsxNode) {
    when (node) {
        is JsxElement -> {
            Text("<${node.name}>")
            node.children.forEach { RenderNodeComposable(it) }
            Text("</${node.name}>")
        }
        is JsxText -> Text(node.text)
        is JsxExpression -> Text("{expr}")
        else -> Text("node")
    }
}
