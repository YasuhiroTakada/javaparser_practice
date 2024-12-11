package jp.co.gxp.javaparser_practice.method_analyze;

import java.io.IOException;
import java.nio.file.Path;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

/**
 * ソースコードの呼び出しフローを収集します.
 */
public class MethodCollectCallback implements SourceRoot.Callback {

    private final String startPackage;
    private final ParseContext parseContext = new ParseContext();
    private final MethodCallVisitor methodCallVisitor = new MethodCallVisitor();

    public MethodCollectCallback(String startPackage) {
        this.startPackage = startPackage;
    }

    public void accept(SourceRoot root) {
        root.setPrinter(LexicalPreservingPrinter::print);
        try {
            root.parseParallelized(startPackage, this);
        } catch (IOException e) {
            Log.error(e);
        }
    }

    @Override
    public Result process(Path localPath,
                          Path absolutePath,
                          ParseResult<CompilationUnit> result) {
        result.ifSuccessful(cu -> methodCallVisitor.visit(cu, parseContext));
        return Result.DONT_SAVE;
    }

    public ParseContext getParseContext() {
        return parseContext;
    }

}
