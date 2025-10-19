# JSX Parser for Kotlin

A lightweight, zero-dependency, pure Kotlin Multiplatform library for parsing JSX syntax into a type-safe Abstract Syntax Tree (AST).

This parser is written 100% in `commonMain` and works on all Kotlin targets (JVM, JS, Native, Wasm).

## Dependency
TODO

## Usage
```
import io.github.deanalvero.parser.jsx.JsxParser

fun main() {
    val source = """
        <div className="container">
            <h1>Hello, World!</h1>
            <p>{message}</p>
        </div>
    """.trimIndent()

    val result = JsxParser.parse(source)
    result.onSuccess { nodes ->
        println("Parsed AST: $nodes")
    }.onFailure { error ->
        println("Parse error: ${error.message}")
    }
}
```

## Notes
Enjoy!
