package jp.co.gxp.javaparser_practice.method_analyze;

import jp.co.gxp.javaparser_practice.adaptor.javaparser.MethodSupport;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.utils.Log;

public class MethodCallVisitor extends VoidVisitorAdapter<ParseContext> implements MethodSupport {

    /**
     * メソッド定義を参照して、そのメソッド実装内でメソッド呼び出しを収集します.
     * また、メソッド定義がインターフェースを継承している場合は継承したインターフェースのQualifierNameとしてもメソッド呼び出しを登録します。
     * メソッドを実装クラスではなく、インターフェース経由で呼んでいる場合に、あとから階層を作成するときにインターフェースと実装を解決する必要があるため。
     * 例)以下①～③が存在する場合に呼び出し階層をたどるとき、②のインターフェースと実装が解決できる必要があります.
     *  ①：Root#doSomething メソッドが Interface#doGoodThing を呼んでいる
     *  ②：Interface#doGoodThing を継承する Implementedクラス が実装している
     *  ③：Implemented#doGoodThing　が Leaf#doSomething を呼んでいる
     *
     * @param md メソッド定義
     * @param arg コンテキスト
     */
    @Override
    public void visit(MethodDeclaration md, ParseContext arg) {
        super.visit(md, arg);
        arg.put(md);

        md.findAll(MethodCallExpr.class)
            .forEach(methodCall -> {
                try {
                    ResolvedMethodDeclaration receiver = methodCall.resolve();
                    arg.add(md, receiver);

                    // メソッドがインターフェースを実装している場合、インターフェースとして呼び出しを登録する
//                    ResolvedMethodDeclaration caller = md.resolve();
//                    implementedInterfaceMethod(caller)
//                    .ifPresent(interfaceMethod -> arg.add(interfaceMethod, receiver));
                } catch (Exception e) {
                    Log.error(e);
                }
            });
    }
}
