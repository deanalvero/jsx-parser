package io.github.deanalvero.parser.jsx

import io.github.deanalvero.parser.jsx.attributevalue.JsxExpressionContainer
import io.github.deanalvero.parser.jsx.attributevalue.JsxStringLiteral
import io.github.deanalvero.parser.jsx.node.JsxElement
import io.github.deanalvero.parser.jsx.node.JsxExpression
import io.github.deanalvero.parser.jsx.node.JsxNode
import io.github.deanalvero.parser.jsx.node.JsxText
import io.github.deanalvero.parser.jsx.node.expression.JsxExpressionNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class JsxParserTest {

    @Test
    fun singleSelfClosingElement() {
        val result = JsxParser.parse("<div />")
        val element = assertSuccess(result).first() as JsxElement

        assertEquals("div", element.name)
        assertTrue(element.attributes.isEmpty())
        assertTrue(element.children.isEmpty())
    }

    @Test
    fun elementWithAttributes() {
        val result = JsxParser.parse("""<h1 id="title" className={styles.main}>Hello</h1>""")
        val element = assertSuccess(result).first() as JsxElement

        assertEquals("h1", element.name)
        assertEquals(2, element.attributes.size)

        val attr1 = element.attributes[0]
        assertEquals("id", attr1.key)
        assertEquals("title", (attr1.value as JsxStringLiteral).value)

        val attr2 = element.attributes[1]
        assertEquals("className", attr2.key)
        assertEquals("styles.main", (attr2.value as JsxExpressionContainer).expression)

        assertEquals(1, element.children.size)
        val textNode = element.children[0] as JsxText
        assertEquals("Hello", textNode.text)
    }

    @Test
    fun nestedElements() {
        val jsx = """
            <div className="container">
                <p>Some text</p>
                <br />
            </div>
        """
        val result = JsxParser.parse(jsx)
        val root = assertSuccess(result).first() as JsxElement

        assertEquals("div", root.name)
        assertEquals(2, root.children.size)

        val p = root.children[0] as JsxElement
        assertEquals("p", p.name)
        assertEquals("Some text", (p.children[0] as JsxText).text)

        val br = root.children[1] as JsxElement
        assertEquals("br", br.name)
        assertTrue(br.children.isEmpty())
    }

    @Test
    fun topLevelTextAndExpression() {
        val jsx = "Hello {name}!"
        val result = JsxParser.parse(jsx)
        val nodes = assertSuccess(result)

        assertEquals(3, nodes.size)
        assertEquals("Hello", (nodes[0] as JsxText).text)
        assertEquals("name", (nodes[1] as JsxExpression).expression)
        assertEquals("!", (nodes[2] as JsxText).text)
    }

    @Test
    fun nestedExpressionContent() {
        val jsx = "<div style={{ color: 'red', margin: 10 }} />"
        val result = JsxParser.parse(jsx)
        val element = assertSuccess(result).first() as JsxElement
        val attrValue = element.attributes.first().value as JsxExpressionContainer
        assertEquals("{ color: 'red', margin: 10 }", attrValue.expression)
    }

    @Test
    fun emptyExpression() {
        val jsx = "<div>{}</div>"
        val result = JsxParser.parse(jsx)
        val element = assertSuccess(result).first() as JsxElement
        assertEquals(1, element.children.size)
        val expr = element.children[0] as JsxExpression
        assertEquals("", expr.expression)
    }

    @Test
    fun emptyAttributeExpression() {
        val jsx = "<div data={} />"
        val result = JsxParser.parse(jsx)
        val element = assertSuccess(result).first() as JsxElement
        assertEquals(1, element.attributes.size)
        val attr = element.attributes[0]
        assertEquals("data", attr.key)
        assertEquals("", (attr.value as JsxExpressionContainer).expression)
    }

    @Test
    fun multipleRootNodes() {
        val jsx = "<div></div><p></p>{true}"
        val result = JsxParser.parse(jsx)
        val nodes = assertSuccess(result)

        assertEquals(3, nodes.size)
        assertEquals("div", (nodes[0] as JsxElement).name)
        assertEquals("p", (nodes[1] as JsxElement).name)
        assertEquals("true", (nodes[2] as JsxExpression).expression)
    }

    @Test
    fun mismatchedTagError() {
        val result = JsxParser.parse("<div></p>")
        val error = assertFailure(result)
        assertTrue(error.message.contains("Mismatched closing tag. Expected </div> but got </p>"))
    }

    @Test
    fun unclosedTagError() {
        val result = JsxParser.parse("<div><p></div>")
        val error = assertFailure(result)
        assertTrue(error.message.contains("Mismatched closing tag"))
    }

    @Test
    fun invalidAttributeError() {
        val result = JsxParser.parse("<div class=>")
        val error = assertFailure(result)
        assertTrue(error.message.contains("Attribute value must be a string literal or an expression"))
    }

    @Test
    fun unmatchedExpressionBraceError() {
        val result = JsxParser.parse("<div>{name</div>")
        val error = assertFailure(result)
        assertTrue(error.message.contains("Unmatched '{' in expression"))
    }

    @Test
    fun unterminatedStringError() {
        val result = JsxParser.parse("<div class=\"hello></div>")
        val error = assertFailure(result)
        assertTrue(error.message.contains("Unterminated string"))
    }

    @Test
    fun booleanAttribute() {
        val source = """<input disabled />"""
        val result = JsxParser.parse(source)
        val nodes = result.getOrThrow()
        assertEquals(1, nodes.size)
        val el = nodes[0] as JsxElement
        assertEquals("input", el.name)
        assertEquals(1, el.attributes.size)
    }

    @Test
    fun hyphenatedAttribute() {
        val source = """<div data-test="hello"></div>"""
        val nodes = JsxParser.parse(source).getOrThrow()
        val el = nodes.single() as JsxElement
        val attr = el.attributes.single()
        assertEquals("data-test", attr.key)
        assertTrue(attr.value is JsxStringLiteral)
    }

    @Test
    fun singleQuotedString() {
        val source = "<div title='Hi'></div>"
        val nodes = JsxParser.parse(source).getOrThrow()
        val el = nodes.single() as JsxElement
        val value = (el.attributes.single().value as JsxStringLiteral).value
        assertEquals("Hi", value)
    }

    @Test
    fun escapedQuoteInString() {
        val source = """<div title="a \"b\" c"></div>"""
        val nodes = JsxParser.parse(source).getOrThrow()
        val el = nodes.single() as JsxElement
        val value = (el.attributes.single().value as JsxStringLiteral).value
        assertEquals("""a "b" c""", value)
    }

    @Test
    fun spreadAttribute() {
        val source = "<div {...props}></div>"
        val nodes = JsxParser.parse(source).getOrThrow()
        val el = nodes.single() as JsxElement
        assertTrue(el.attributes.any { it.key == "..." || it.value is JsxExpressionContainer })
    }

    @Test
    fun fragmentShorthand() {
        val source = "<>hello</>"
        val nodes = JsxParser.parse(source).getOrThrow()
        assertEquals(1, nodes.size)
        val child = nodes.single()
        val foundText = when (child) {
            is JsxElement -> child.children.any { it is JsxText && it.text == "hello" }
            else -> false
        }
        assertTrue(foundText)
    }

    @Test
    fun memberExpressionTag() {
        val source = "<Svg.Rect />"
        val nodes = JsxParser.parse(source).getOrThrow()
        val el = nodes.single() as JsxElement
        assertEquals("Svg.Rect", el.name)
    }

    @Test
    fun expressionContainingStringWithBraces() {
        val source = """<div>{ someFn("}") }</div>"""
        val nodes = JsxParser.parse(source).getOrThrow()
        val el = nodes.single() as JsxElement
        assertEquals(1, el.children.size)
        val expr = el.children[0]
        assertTrue(expr is JsxExpression)
        assertTrue((expr as JsxExpression).expression.contains("someFn"))
    }

    @Test
    fun parsesIdentifier() {
        val expr = JsxParser.parse("{counter}")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.Identifier
        assertEquals("counter", node.name)
    }

    @Test
    fun parsesMemberExpression() {
        val expr = JsxParser.parse("{styles.main}")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.MemberExpression
        assertEquals(listOf("styles", "main"), node.parts)
    }

    @Test
    fun parsesNumberLiteral() {
        val expr = JsxParser.parse("{42}")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.NumberLiteral
        assertEquals(42.0, node.value)
    }

    @Test
    fun parsesStringLiteral() {
        val expr = JsxParser.parse("{\"hello\"}")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.StringLiteral
        assertEquals("hello", node.value)
    }

    @Test
    fun parsesCallExpression() {
        val expr = JsxParser.parse("{increment(counter)}")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.CallExpression
        assertEquals("increment", node.callee)
        assertEquals(1, node.arguments.size)
    }

    @Test
    fun parsesObjectLiteral() {
        val expr = JsxParser.parse("{ { color: \"red\", size: 12 } }")
            .getOrThrow().first() as JsxExpression

        val obj = expr.node as JsxExpressionNode.ObjectLiteral
        assertEquals(2, obj.properties.size)
        assertTrue(obj.properties["color"] is JsxExpressionNode.StringLiteral)
        assertTrue(obj.properties["size"] is JsxExpressionNode.NumberLiteral)
    }

    @Test
    fun unknownExpressionFallsBack() {
        val expr = JsxParser.parse("{a + b}")
            .getOrThrow().first() as JsxExpression
        assertTrue(expr.node is JsxExpressionNode.Unknown)
    }

    private fun assertSuccess(result: Result<List<JsxNode>>): List<JsxNode> {
        if (result.isFailure) {
            fail("Expected success but was failure: ${result.exceptionOrNull()?.message}", result.exceptionOrNull())
        }
        return result.getOrNull()!!
    }

    private fun assertFailure(result: Result<List<JsxNode>>): JsxParseError {
        if (result.isSuccess) {
            fail("Expected failure but was success.")
        }
        val exception = result.exceptionOrNull()
        assertIs<JsxParseError>(exception, "Exception was not a JsxParseError")
        return exception
    }
}
