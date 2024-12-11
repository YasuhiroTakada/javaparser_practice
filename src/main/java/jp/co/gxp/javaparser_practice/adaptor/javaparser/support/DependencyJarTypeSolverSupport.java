package jp.co.gxp.javaparser_practice.adaptor.javaparser.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;

class DependencyJarTypeSolverSupport {

    private DependencyJarTypeSolverSupport() {
        // hidden constructor
    }

    private static final String DEFAULT_DEPENDENCY_LIST_FILE = ".dependencies";

    /**
     *
     * @param targetPath 解析対象のプロジェクトルート
     * @return 依存関係のJarTypeSolver
     */
    static CombinedTypeSolver dependencySolver(String targetPath) {
        Path dependencyListPath = Paths.get(targetPath, DEFAULT_DEPENDENCY_LIST_FILE);

        JarTypeSolver[] solvers = readDependencies(dependencyListPath).stream()
            .map(Paths::get)
            .map(DependencyJarTypeSolverSupport::mapToSolver)
            .filter(Objects::nonNull)
            .toArray(JarTypeSolver[]::new);

        return new CombinedTypeSolver(solvers);
    }

    private static List<String> readDependencies(Path dependenciesListClassPath) {
        try (BufferedReader br = Files.newBufferedReader(dependenciesListClassPath)) {
            return br.lines()
                .flatMap(line -> Stream.of(line.split(File.pathSeparator)))
                .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private static JarTypeSolver mapToSolver(Path path) {
        try {
            return new JarTypeSolver(path);
        } catch (IOException e) {
            return null;
        }
    }
}
