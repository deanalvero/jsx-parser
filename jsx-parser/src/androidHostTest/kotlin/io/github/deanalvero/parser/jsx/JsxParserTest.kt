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

    @Test
    fun parsesTrueLiteral() {
        val expr = JsxParser.parse("{true}")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.BooleanLiteral
        assertEquals(true, node.value)
    }

    @Test
    fun parsesFalseLiteral() {
        val expr = JsxParser.parse("{false}")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.BooleanLiteral
        assertEquals(false, node.value)
    }

    @Test
    fun parsesBooleanAsAttributeValue() {
        val element = JsxParser.parse("<Switch disabled={true} />")
            .getOrThrow().first() as JsxElement

        val attr = element.attributes.first()
        assertEquals("disabled", attr.key)

        val container = attr.value as JsxExpressionContainer
        val node = container.node as JsxExpressionNode.BooleanLiteral
        assertEquals(true, node.value)
    }

    @Test
    fun parsesBooleanInsideObjectLiteral() {
        val expr = JsxParser.parse("{ { visible: true, disabled: false } }")
            .getOrThrow().first() as JsxExpression

        val obj = expr.node as JsxExpressionNode.ObjectLiteral
        assertEquals(2, obj.properties.size)

        val visible = obj.properties["visible"] as JsxExpressionNode.BooleanLiteral
        assertEquals(true, visible.value)

        val disabled = obj.properties["disabled"] as JsxExpressionNode.BooleanLiteral
        assertEquals(false, disabled.value)
    }

    @Test
    fun trueAndFalseAreNotParsedAsIdentifiers() {
        val trueExpr = JsxParser.parse("{true}").getOrThrow().first() as JsxExpression
        val falseExpr = JsxParser.parse("{false}").getOrThrow().first() as JsxExpression

        assertIs<JsxExpressionNode.BooleanLiteral>(trueExpr.node)
        assertIs<JsxExpressionNode.BooleanLiteral>(falseExpr.node)
    }

    @Test
    fun parsesEmptyArray() {
        val expr = JsxParser.parse("{[]}")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.ArrayLiteral
        assertTrue(node.items.isEmpty())
    }

    @Test
    fun parsesNumberArray() {
        val expr = JsxParser.parse("{[1, 2, 3]}")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.ArrayLiteral

        assertEquals(3, node.items.size)
        assertEquals(1.0, (node.items[0] as JsxExpressionNode.NumberLiteral).value)
        assertEquals(2.0, (node.items[1] as JsxExpressionNode.NumberLiteral).value)
        assertEquals(3.0, (node.items[2] as JsxExpressionNode.NumberLiteral).value)
    }

    @Test
    fun parsesStringArray() {
        val expr = JsxParser.parse("""{ ["row", "column"] }""")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.ArrayLiteral

        assertEquals(2, node.items.size)
        assertEquals("row",    (node.items[0] as JsxExpressionNode.StringLiteral).value)
        assertEquals("column", (node.items[1] as JsxExpressionNode.StringLiteral).value)
    }

    @Test
    fun parsesMixedArray() {
        val expr = JsxParser.parse("""{ [1, "two", true, false] }""")
            .getOrThrow().first() as JsxExpression
        val node = expr.node as JsxExpressionNode.ArrayLiteral

        assertEquals(4, node.items.size)
        assertIs<JsxExpressionNode.NumberLiteral>(node.items[0])
        assertIs<JsxExpressionNode.StringLiteral>(node.items[1])
        assertIs<JsxExpressionNode.BooleanLiteral>(node.items[2])
        assertIs<JsxExpressionNode.BooleanLiteral>(node.items[3])
        assertEquals(true,  (node.items[2] as JsxExpressionNode.BooleanLiteral).value)
        assertEquals(false, (node.items[3] as JsxExpressionNode.BooleanLiteral).value)
    }

    @Test
    fun parsesNestedArray() {
        val expr = JsxParser.parse("{ [[1, 2], [3, 4]] }")
            .getOrThrow().first() as JsxExpression
        val outer = expr.node as JsxExpressionNode.ArrayLiteral

        assertEquals(2, outer.items.size)

        val first = outer.items[0] as JsxExpressionNode.ArrayLiteral
        assertEquals(1.0, (first.items[0] as JsxExpressionNode.NumberLiteral).value)
        assertEquals(2.0, (first.items[1] as JsxExpressionNode.NumberLiteral).value)

        val second = outer.items[1] as JsxExpressionNode.ArrayLiteral
        assertEquals(3.0, (second.items[0] as JsxExpressionNode.NumberLiteral).value)
        assertEquals(4.0, (second.items[1] as JsxExpressionNode.NumberLiteral).value)
    }

    @Test
    fun parsesArrayAsAttributeValue() {
        val element = JsxParser.parse("""<Text fontVariant={["small-caps"]} />""")
            .getOrThrow().first() as JsxElement

        val attr = element.attributes.first()
        assertEquals("fontVariant", attr.key)

        val container = attr.value as JsxExpressionContainer
        val node = container.node as JsxExpressionNode.ArrayLiteral
        assertEquals(1, node.items.size)
        assertEquals("small-caps", (node.items[0] as JsxExpressionNode.StringLiteral).value)
    }

    @Test
    fun parsesArrayInsideObjectLiteral() {
        val expr = JsxParser.parse("{ { transform: [{rotate: '45deg'}] } }")
            .getOrThrow().first() as JsxExpression

        val obj = expr.node as JsxExpressionNode.ObjectLiteral
        val transformArray = obj.properties["transform"] as JsxExpressionNode.ArrayLiteral

        assertEquals(1, transformArray.items.size)
        val rotateObj = transformArray.items[0] as JsxExpressionNode.ObjectLiteral
        val rotate = rotateObj.properties["rotate"] as JsxExpressionNode.StringLiteral
        assertEquals("45deg", rotate.value)
    }

    @Test
    fun splitTopLevelHandlesCommasInsideNestedArray() {
        val expr = JsxParser.parse("{ { padding: [4, 8] } }")
            .getOrThrow().first() as JsxExpression

        val obj = expr.node as JsxExpressionNode.ObjectLiteral
        assertEquals(1, obj.properties.size)

        val padding = obj.properties["padding"] as JsxExpressionNode.ArrayLiteral
        assertEquals(2, padding.items.size)
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
