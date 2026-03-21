package io.github.deanalvero.parser.jsx.demoapp.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.deanalvero.parser.jsx.demoapp.InspectorState

@Composable
fun PreviewPanelComposable(
    state: InspectorState,
    modifier: Modifier
) {
    Column(
        modifier.padding(8.dp)
    ) {
        Text("Preview")
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface)
                .padding(8.dp)
                .weight(1f)
                .fillMaxWidth()
        ) {
            RenderPreviewComposable(state.ast)
        }
    }
}
