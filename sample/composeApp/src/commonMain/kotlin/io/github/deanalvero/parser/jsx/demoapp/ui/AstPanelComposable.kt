package io.github.deanalvero.parser.jsx.demoapp.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.deanalvero.parser.jsx.demoapp.AstViewNode
import io.github.deanalvero.parser.jsx.demoapp.InspectorState
import io.github.deanalvero.parser.jsx.demoapp.toView

@Composable
fun AstPanelComposable(
    state: InspectorState,
    modifier: Modifier
) {
    Column(
        modifier.padding(8.dp).fillMaxHeight()
    ) {
        Text("AST")

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface)
                .padding(8.dp)
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            state.ast?.forEach {
                AstNodeViewComposable(it.toView())
            }
        }
    }
}

@Composable
private fun AstNodeViewComposable(node: AstViewNode, indent: Int = 0) {
    Column {
        Text(" ".repeat(indent * 2) + node.label)
        node.children.forEach {
            AstNodeViewComposable(it, indent + 1)
        }
    }
}