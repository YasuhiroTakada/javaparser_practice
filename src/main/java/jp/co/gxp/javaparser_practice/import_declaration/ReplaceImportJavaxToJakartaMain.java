package jp.co.gxp.javaparser_practice.import_declaration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.co.gxp.javaparser_practice.adaptor.javaparser.LogConfig;
import jp.co.gxp.javaparser_practice.adaptor.javaparser.support.CollectionStrategySupport;

import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

public class ReplaceImportJavaxToJakartaMain {

    static {
        LogConfig.setErrorOnly();
    }

    /**
     * 起動引数リスト
     * <ol>
     *    <li>1：解析対象プロジェクトへのパス.</li>
     *    <li>２：解析対象パッケージ.</li>
     * </ol>
     * @param args 起動引数
     */
    public static void main(String[] args) {
        String targetPath = args[0];
        String startPackage = args.length > 1 ? args[1] : "";

        SymbolSolverCollectionStrategy strategy = CollectionStrategySupport.symbolSolverCollectionStrategy(targetPath);

        ProjectRoot projectRoot = strategy.collect(Paths.get(targetPath, "/src/main/java"));
        ReplaceImportJavaxToJakarta visitor = new ReplaceImportJavaxToJakarta();

        // Do Parse
        projectRoot.getSourceRoots()
            .forEach(sourceRoot -> {
                sourceRoot.setPrinter(LexicalPreservingPrinter::print);
                try {
                    sourceRoot.parseParallelized(startPackage, (localPath, absolutePath, result) -> {
                        AtomicBoolean visitResult = new AtomicBoolean(false);
                        result.ifSuccessful(compilationUnit -> {
                            Boolean success = visitor.visit(compilationUnit, null);
                            if (success != null && success) {
                                visitResult.set(true);
                            }
                        });
                        return visitResult.get() ? SourceRoot.Callback.Result.SAVE : SourceRoot.Callback.Result.DONT_SAVE;
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

    }


}
