package de.fosd.typechef.parser.c
import org.anarres.cpp.Main

import de.fosd.typechef.featureexpr._
import de.fosd.typechef.parser._
import java.io.FileWriter
import java.io.File
import junit.framework._
import junit.framework.Assert._

object ParserMain {
    def parserMain(filePath: String, parentPath: String, initialContext: CTypeContext):AST = {
        val result = new CParser().translationUnit(
            CLexer.lexFile(filePath, parentPath).setContext(initialContext), FeatureExpr.base)
        val resultStr: String = result.toString
        println("FeatureSolverCache.statistics: " + FeatureSolverCache.statistics)
        val writer = new FileWriter(filePath + ".ast")
        writer.write(resultStr);
        writer.close
        println("done.")

        printParseResult(result, FeatureExpr.base)
        checkParseResult(result, FeatureExpr.base)
        result match {
            case Success(ast, _) => ast
            case _=>null
        }
    }

    def printParseResult(result: MultiParseResult[Any, TokenWrapper, CTypeContext], feature: FeatureExpr) {
        result match {
            case Success(ast, unparsed) => {
                if (unparsed.atEnd)
                    println(feature.toString + "\tsucceeded\n")
                else
                    println(feature.toString + "\tstopped before end\n")
            }
            case NoSuccess(msg, context, unparsed, inner) =>
                println(feature.toString + "\tfailed " + msg + "\n")
            case SplittedParseResult(f, left, right) => {
                printParseResult(left, feature.and(f))
                printParseResult(right, feature.and(f.not))
            }
        }
    }

    def checkParseResult(result: MultiParseResult[Any, TokenWrapper, CTypeContext], feature: FeatureExpr) {
        result match {
            case Success(ast, unparsed) => {
                if (!unparsed.atEnd)
                    fail("parser did not reach end of token stream with feature " + feature + " (" + unparsed.first.getPosition + "): " + unparsed)
                //succeed
            }
            case NoSuccess(msg, context, unparsed, inner) =>
                fail(msg + " at " +  unparsed + " with feature " + feature + " and context " + context + " " + inner)
            case SplittedParseResult(f, left, right) => {
                checkParseResult(left, feature.and(f))
                checkParseResult(right, feature.and(f.not))
            }
        }
    }

    def main(args: Array[String]) = {
        for (filename <- args) {
            println("**************************************************************************")
            println("** Processing file: "+filename)
            println("**************************************************************************")
            val parentPath = new File(filename).getParent()
            parserMain(filename, parentPath, new CTypeContext())
            println("**************************************************************************")
	    println("** End of processing for: " + filename)
            println("**************************************************************************")
        }
    }
}
