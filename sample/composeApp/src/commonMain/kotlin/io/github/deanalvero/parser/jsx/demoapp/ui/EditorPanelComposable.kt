package io.github.deanalvero.parser.jsx.demoapp.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditorPanelComposable(
    source: String,
    error: Throwable?,
    onChange: (String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier.padding(8.dp)
    ) {
        Text("JSX Source")
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface)
                .padding(8.dp)
                .weight(1f)
                .fillMaxWidth()
        ) {
            BasicTextField(
                value = source,
                onValueChange = onChange,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (error != null) {
            Text(
                text = "Error: ${error.message}",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}