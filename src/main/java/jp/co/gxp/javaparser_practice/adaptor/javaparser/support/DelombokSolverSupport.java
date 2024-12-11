package jp.co.gxp.javaparser_practice.adaptor.javaparser.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

class DelombokSolverSupport {

    private DelombokSolverSupport() {
        // hidden constructor
    }

    /** 対象プロジェクト直下にあるdelombokディレクトリを対象とする */
    private static final String DEFAULT_DELOMBOK_DIR = "/delombok";

    /**
     * delombokしたソースファイルをSolverに登録する
     * https://projectlombok.org/features/delombok
     *
     * @param targetPath 解析対象プロジェクトのパス
     * @return delombokディレクトリが存在すれば、 JavaParserTypeSolverとして返す. ディレクトリがなければemptyを返す.
     */
    static Optional<JavaParserTypeSolver> delombokSolver(String targetPath) {
        Path delombok = Paths.get(targetPath, DEFAULT_DELOMBOK_DIR);
        return Optional.ofNullable(Files.isDirectory(delombok) ? new JavaParserTypeSolver(delombok) : null);
    }
}
