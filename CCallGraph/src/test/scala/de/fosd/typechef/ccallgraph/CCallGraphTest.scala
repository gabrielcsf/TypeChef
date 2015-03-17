package de.fosd.typechef.ccallgraph

import java.io.{FileNotFoundException, InputStream}

import de.fosd.typechef.conditional.ConditionalSet
import de.fosd.typechef.featureexpr.FeatureExprFactory
import de.fosd.typechef.featureexpr.FeatureExprFactory.{False, True}
import de.fosd.typechef.parser.c._
import org.junit.Test


/**
 * Created by gferreir on 11/16/14.
 */
class CCallGraphTest extends TestHelper {

    @Test def unscopedObjectNames() {
        var cset = ConditionalSet[String]()
        cset +=("GLOBAL$x", True)
        cset +=("bar$y", True)
        cset +=("stat_main$statfunc", True)
        cset +=("a$b|c", True)

        val eq = new EquivalenceClass(cset, ConditionalSet())
        assert(eq.unscopedObjectNames() equals Set("x", "y", "statfunc", "b|c"), "expected %s, but found %s".format(eq.unscopedObjectNames(), Set("x", "y", "statfunc", "b|c")))
    }

    @Test def testCallGraphNodes(): Unit = {
        val ast = loadAST("callGraph1.c")

        val c: CCallGraph = new CCallGraph()
        c.calculatePointerEquivalenceRelation(ast)
        c.extractCallGraph()

        c.callGraphNodes.toPlainSetWithConditionals().map(println)
        assert(c.callGraphNodes.keys.size equals 4, "expected %s, but found %s".format(4, c.callGraphNodes.keys.size))
        assert(c.callGraphNodes.keys.toList.contains(Node("foo", "declaration", 1)), "expected %s, but not found".format(Node("foo", "declaration", 1)))
        assert(c.callGraphNodes.keys.toList.contains(Node("bar", "function-inline", 2)), "expected %s, but not found".format(Node("bar", "function-inline", 2)))
        assert(c.callGraphNodes.keys.toList.contains(Node("baz", "function-static", 3)), "expected %s, but not found".format(Node("baz", "function-static", 3)))
        assert(c.callGraphNodes.keys.toList.contains(Node("main", "function", 5)), "expected %s, but not found".format(Node("main", "function", 5)))
    }

    @Test def testCallGraphNodesFuncDecl(): Unit = {
        val ast = loadAST("callGraphFuncDeclarations.c")

        val c: CCallGraph = new CCallGraph()
        c.calculatePointerEquivalenceRelation(ast)
        c.extractCallGraph()

        c.callGraphNodes.toPlainSetWithConditionals().map(println)
        assert(c.callGraphNodes.keys.size equals 1, "expected %s, but found %s".format(1, c.callGraphNodes.keys.size))
        assert(c.callGraphNodes.keys.toList.contains(Node("xstrtoull_range_sfx", "function-inline", 2)), "expected %s, but not found".format(Node("xstrtoull_range_sfx", "function-inline", 2)))
    }

    @Test def testCallGraphNodesFuncDeclAndDef(): Unit = {
        val ast = loadAST("callGraph2.c")

        val c: CCallGraph = new CCallGraph()

        c.calculatePointerEquivalenceRelation(ast)
        c.extractCallGraph()

        c.callGraphNodes.toPlainSetWithConditionals.map(println)
        assert(c.callGraphNodes.keys.size equals 2, "expected %s, but found %s".format(2, c.callGraphNodes.keys.size))
        assert(c.callGraphNodes.keys.toList.contains(Node("foo", "function", 4)), "expected %s, but not found".format(Node("foo", "function", 4)))
        assert(c.callGraphNodes.keys.toList.contains(Node("bar", "declaration", 2)), "expected %s, but not found".format(Node("bar", "declaration", 2)))
    }

    @Test def testCallGraphNodesFuncDeclAndConditionalDef(): Unit = {
        val ast = loadAST("callGraph2Conditional.c")

        val c: CCallGraph = new CCallGraph()

        c.calculatePointerEquivalenceRelation(ast)
        c.extractCallGraph()

        c.callGraphNodes.toPlainSetWithConditionals().map(println)
        assert(c.callGraphNodes.toPlainSetWithConditionals.size equals 4, "expected %s, but found %s".format(4, c.callGraphNodes.toPlainSetWithConditionals.size))
        assert(c.callGraphNodes.toPlainSetWithConditionals.contains(Node("foo", "declaration", 1), True), "expected %s, but not found".format(Node("foo", "declaration", 1)))
        assert(c.callGraphNodes.toPlainSetWithConditionals.contains(Node("bar", "declaration", 2), True), "expected %s, but not found".format(Node("bar", "declaration", 2)))
        assert(c.callGraphNodes.toPlainSetWithConditionals.contains(Node("baz", "function-inline", 3), True), "expected %s, but not found".format(Node("baz", "declaration", 3)))
        assert(c.callGraphNodes.toPlainSetWithConditionals.contains(Node("foo", "function", 6), FeatureExprFactory.createDefinedExternal("A")), "expected %s, but not found".format(Node("foo", "function", 6)))

    }



    @Test def test_paper_example_fig1() {
        val ast = loadAST("fig1_table_dispatch.c")

        val c: CCallGraph = new CCallGraph()

        c.calculatePointerEquivalenceRelation(ast)
        c.showPointerEquivalenceClasses()
        c.showFunctionCalls()

        c.extractCallGraph()
        c.showCallGraphStatistics()

    }

    @Test def test_paper_example_fig2() {
        val ast = loadAST("fig2_extensible_func.c")

        val c: CCallGraph = new CCallGraph()
        c.calculatePointerEquivalenceRelation(ast)

        c.showCallGraphStatistics()

    }

    @Test def test_paper_example_fig3() {
        val ast = loadAST("fig3_sample_prog.c")

        val c: CCallGraph = new CCallGraph()

        c.calculatePointerEquivalenceRelation(ast)
        c.showPointerEquivalenceClasses()
        c.showFunctionCalls()

        c.extractCallGraph()
        c.showCallGraphStatistics()

    }

    @Test def test_paper_example_fig4() {
        val ast = loadAST("fig4_simple_sets_statements.c")

        val c: CCallGraph = new CCallGraph()
        c.calculatePointerEquivalenceRelation(ast)

        c.showPointerEquivalenceClasses()

    }

    @Test def test_variational_code() {
        val ast = loadAST("test_variational_code.c")

        val c: CCallGraph = new CCallGraph()

        c.calculatePointerEquivalenceRelation(ast)
        c.showPointerEquivalenceClasses()
        c.showFunctionCalls()

        c.extractCallGraph()
        c.showCallGraphStatistics()

    }

    @Test def test_variational_function_calls() {
        val ast = loadAST("variational_function_calls.c")

        val c: CCallGraph = new CCallGraph()
        c.calculatePointerEquivalenceRelation(ast)
        c.extractCallGraph()
        c.callGraphNodes.toPlainSetWithConditionals.map(println)

        val expectedNodes = ConditionalSet(Map(Node("foo", "declaration", 1) -> True, Node("bar", "declaration", 2) -> True, Node("baz", "declaration", 3) -> True, Node("main", "function",
            5) -> True))
        assert(c.callGraphNodes equals expectedNodes, "expected %s, but found %s".format(expectedNodes, c.callGraphNodes))

        val expectedEdges = ConditionalSet(Map(Edge("main", "foo", "D") -> FeatureExprFactory.createDefinedExternal("B"),
            Edge("main", "bar", "I") -> FeatureExprFactory.createDefinedExternal("B").and(FeatureExprFactory.createDefinedExternal("A")),
            Edge("main", "baz", "I") -> FeatureExprFactory.createDefinedExternal("B").and(FeatureExprFactory.createDefinedExternal("A").not)))

        assert(c.callGraphEdges equals expectedEdges, "expected %s, but found %s".format(c.callGraphEdges, expectedEdges))

    }

    @Test def test_variational_function_calls2() {
        val ast = loadAST("variational_function_calls2.c")

        val c: CCallGraph = new CCallGraph()

        c.calculatePointerEquivalenceRelation(ast)
        c.extractCallGraph()
        c.callGraphNodes.toPlainSetWithConditionals.map(println)

        val expectedNodes = ConditionalSet(Map(Node("foo", "declaration", 1) -> True, Node("bar", "declaration", 2) -> True, Node("baz", "declaration", 3) -> True, Node("main", "function", 5) -> True))
        assert(c.callGraphNodes equals expectedNodes, "expected %s, but found %s".format(c.callGraphNodes, expectedNodes))

        val expectedEdges = ConditionalSet(Map(Edge("main", "foo", "D") -> FeatureExprFactory.createDefinedExternal("B"),
            Edge("main", "bar", "I") -> FeatureExprFactory.createDefinedExternal("B").and(FeatureExprFactory.createDefinedExternal("A")),
            Edge("main", "baz", "I") -> FeatureExprFactory.createDefinedExternal("B").and(FeatureExprFactory.createDefinedExternal("A").not)))

        assert(c.callGraphEdges equals expectedEdges, "expected %s, but found %s".format(c.callGraphEdges, expectedEdges))

    }

    @Test def test_variational_function_calls3() {
        val ast = loadAST("variational_function_calls3.c")

        val c: CCallGraph = new CCallGraph()

        c.calculatePointerEquivalenceRelation(ast)
        c.extractCallGraph()
        c.callGraphNodes.toPlainSetWithConditionals.map(println)

        val expectedNodes = ConditionalSet(Map(Node("foo", "declaration", 1) -> True, Node("bar", "declaration", 2) -> True, Node("baz", "declaration", 3) -> True, Node("main", "function",
            5) -> True))
        assert(c.callGraphNodes equals expectedNodes, "expected %s, but found %s".format(c.callGraphNodes, expectedNodes))

        val expectedEdges = ConditionalSet(Map(Edge("main", "foo", "D") -> FeatureExprFactory.createDefinedExternal("B"),
            Edge("main", "bar", "I") -> FeatureExprFactory.createDefinedExternal("B").and(FeatureExprFactory.createDefinedExternal("A")),
            Edge("main", "baz", "I") -> FeatureExprFactory.createDefinedExternal("B").and(FeatureExprFactory.createDefinedExternal("A").not)))

        assert(c.callGraphEdges equals expectedEdges, "expected %s, but found %s".format(c.callGraphEdges, expectedEdges))

    }


    val parser = new CParser()
    val emptyFM = FeatureExprFactory.dflt.featureModelFactory.empty

    private def loadAST(filename: String): TranslationUnit = {
        val folder = "testfiles/"
        val instream: InputStream = getClass.getResourceAsStream("/" + folder + filename)
        if (instream == null)
            throw new FileNotFoundException("Input file not found!")
        val ast = parseFile(instream, folder, filename)
        ast
    }

    private def loadASTFromCodeSnippet(codeSnippet: String): TranslationUnit = {
        val code = "void main() {\n  %s\n}\n".format(codeSnippet)
        val ast: TranslationUnit = new ParserMain(parser).parserMain(lex(code), SilentParserOptions, emptyFM)
        ast
    }
}
