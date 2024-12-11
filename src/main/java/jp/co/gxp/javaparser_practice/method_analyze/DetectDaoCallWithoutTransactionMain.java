package jp.co.gxp.javaparser_practice.method_analyze;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jp.co.gxp.javaparser_practice.adaptor.javaparser.LogConfig;
import jp.co.gxp.javaparser_practice.adaptor.javaparser.dto.ResolvedMethodCall;
import jp.co.gxp.javaparser_practice.adaptor.javaparser.repository.MethodRepository;
import jp.co.gxp.javaparser_practice.adaptor.javaparser.support.CollectionStrategySupport;

import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;

public class DetectDaoCallWithoutTransactionMain {

    static {
        LogConfig.setErrorOnly();
    }

    /**
     * 起動引数リスト
     * <ol>
     *    <li>1：解析対象プロジェクトへのパス.</li>
     *    <li>２：解析対象パッケージ.</li>
     *    <li>３：データアクセスを行うコードが存在するパッケージ名.</li>
     * </ol>
     * @param args 起動引数
     */
    public static void main(String[] args) {
        final String targetPath = args[0];
        final String startPackage = args[1];
        final String retrieveBasePackage = args[2];

        SymbolSolverCollectionStrategy strategy = CollectionStrategySupport.symbolSolverCollectionStrategy(targetPath);

        // 解析対象はmaven or gradleで管理想定なので/src/main/javaへの参照はハードコード
        ProjectRoot projectRoot = strategy.collect(Paths.get(targetPath, "/src/main/java"));

        // 各ファイルにアクセスしてメソッド定義の情報を集めるコールバック関数
        MethodCollectCallback methodCollectCallback = new MethodCollectCallback(startPackage);

        // ここでパースしている
        projectRoot.getSourceRoots()
            .forEach(methodCollectCallback::accept);

        // ここから先はパースした結果を利用して
        // トランザクション外のデータアクセスを見つけるコード
        MethodRepository methodRepository = methodCollectCallback.getParseContext().getMethodRepository();
        methodRepository.tree();

        List<ResolvedMethodCall> daoMethods = methodRepository.find(call -> call.receiver()
            .getQualifiedName()
            .startsWith(retrieveBasePackage));

        daoMethods.stream().parallel()
            .map(ResolvedMethodCall::receiver)
            .collect(Collectors.toMap(ResolvedMethodDeclaration::getQualifiedSignature, Function.identity(), (r1, r2) -> r1))
            .values()
            .stream()
            .flatMap(md -> methodRepository.createMethodCallStackUpWise(md).stream())
        .filter(stack -> stack.stream().map(MethodDeclarationWrapper::new).noneMatch(mdw -> mdw.hasAnnotationClassOrMethod("org.springframework.transaction.annotation.Transactional")))
        .forEach(stack -> System.out.println("NoTransactional: " + stack.stream().map(md -> new MethodDeclarationWrapper(md).getKey()).toList()));
    }


}
