package io.github.deanalvero.parser.jsx.demoapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform