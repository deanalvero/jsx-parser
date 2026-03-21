package io.github.deanalvero.parser.jsx.demoapp.data

val SAMPLES = mapOf(
    "Simple" to """
        <Text>Hello world</Text>
    """.trimIndent(),

    "Binding" to """
        <Text>{counter}</Text>
    """.trimIndent(),

    "Member" to """
        <View style={{ color: "red" }}>
            <Text>{theme.primary}</Text>
        </View>
    """.trimIndent(),

    "Call" to """
        <Button onPress={increment(counter)} />
    """.trimIndent(),

    "Object" to """
        <View style={{ padding: 10, color: "blue" }} />
    """.trimIndent(),

    "Invalid" to """
        <View>
            <Text>
        </View>
    """.trimIndent()
)
