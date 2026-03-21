package io.github.deanalvero.parser.jsx.demoapp

import io.github.deanalvero.parser.jsx.JsxParser

object Processor {
    fun process(source: String): InspectorState {
        val result = JsxParser.parse(source)
        return result.fold(
            onSuccess = { ast ->
                InspectorState(source, ast, null)
            },
            onFailure = { error ->
                InspectorState(source, null, error)
            }
        )
    }
}
