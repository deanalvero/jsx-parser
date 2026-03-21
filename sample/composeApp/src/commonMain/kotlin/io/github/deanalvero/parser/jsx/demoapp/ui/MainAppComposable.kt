package io.github.deanalvero.parser.jsx.demoapp.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.deanalvero.parser.jsx.demoapp.Processor
import io.github.deanalvero.parser.jsx.demoapp.data.SAMPLES

@Composable
fun MainAppComposable() {
    var source by remember { mutableStateOf(SAMPLES.values.first()) }
    var state by remember { mutableStateOf(Processor.process(source)) }

    val update = fun(text: String) {
        source = text
        state = Processor.process(text)
    }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopBarComposable(update)
        }
    ) {
        Row(
            Modifier.padding(it)
        ) {
            EditorPanelComposable(
                source = source,
                error = state.error,
                onChange = update,
                modifier = Modifier.weight(1f)
            )

            AstPanelComposable(
                state = state,
                modifier = Modifier.weight(1f)
            )

            PreviewPanelComposable(
                state = state,
                modifier = Modifier.weight(1f)
            )
        }
    }
}