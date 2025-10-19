package io.github.deanalvero.parser.jsx.demoapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "JSXParserDemoApp",
    ) {
        App()
    }
}