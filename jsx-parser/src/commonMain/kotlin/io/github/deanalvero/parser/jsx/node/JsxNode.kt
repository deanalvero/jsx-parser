package io.github.deanalvero.parser.jsx.node

import io.github.deanalvero.parser.jsx.SourceLocation

sealed interface JsxNode {
    val location: SourceLocation
}
