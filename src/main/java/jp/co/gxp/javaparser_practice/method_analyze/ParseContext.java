package jp.co.gxp.javaparser_practice.method_analyze;

import jp.co.gxp.javaparser_practice.adaptor.javaparser.repository.MethodRepository;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

/**
 * this class holds parse result.
 * Contains Method call hierarchy, Interface implemented classes.
 */
public class ParseContext {

    private final MethodRepository methodRepository = new MethodRepository();

    public void add(MethodDeclaration method, ResolvedMethodDeclaration caller) {
        methodRepository.add(method, caller);
    }

    public void put(MethodDeclaration md) {
         methodRepository.put(md);
    }

    public void add(ResolvedMethodDeclaration caller, ResolvedMethodDeclaration receiver) {
        methodRepository.add(caller, receiver);
    }

    public MethodRepository getMethodRepository() {
        return methodRepository;
    }

}
