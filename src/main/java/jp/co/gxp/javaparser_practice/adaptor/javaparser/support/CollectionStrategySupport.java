package jp.co.gxp.javaparser_practice.adaptor.javaparser.support;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;

public class CollectionStrategySupport {

    private CollectionStrategySupport() {
        // hidden constructor
    }

    public static SymbolSolverCollectionStrategy symbolSolverCollectionStrategy(String target) {
        SymbolSolverCollectionStrategy strategy = new SymbolSolverCollectionStrategy();
        SymbolResolver resolver = javaSymbolSolverSetup(target);
        strategy.getParserConfiguration()
            .setCharacterEncoding(StandardCharsets.UTF_8)
            .setLexicalPreservationEnabled(true)
            .setSymbolResolver(resolver);
        return strategy;
    }

    private static JavaSymbolSolver javaSymbolSolverSetup(String targetPath) {
        // 依存関係先の型を解決するSolverをclasspathリストから作成する
        CombinedTypeSolver typeSolver = DependencyJarTypeSolverSupport.dependencySolver(targetPath);

        // lombokで生成するコードを解決できるようにdelombokしたソースファイルをSolverに登録する
        Optional<JavaParserTypeSolver> delombokSolverMaybe = DelombokSolverSupport.delombokSolver(targetPath);
        delombokSolverMaybe.ifPresent(typeSolver::add);

        typeSolver.add(new ReflectionTypeSolver(false));

        return new JavaSymbolSolver(typeSolver);
    }
}
