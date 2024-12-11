package jp.co.gxp.javaparser_practice.method_analyze;

import java.util.Objects;

import jp.co.gxp.javaparser_practice.adaptor.javaparser.MethodSupport;

import com.github.javaparser.ast.body.MethodDeclaration;

public class MethodDeclarationWrapper implements MethodSupport {

    private final MethodDeclaration methodDeclaration;

    public MethodDeclarationWrapper(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MethodDeclarationWrapper that = (MethodDeclarationWrapper) o;
        return Objects.equals(this.getKey(), that.getKey());
    }

    public boolean hasAnnotationClassOrMethod(String annotation) {
        return hasAnnotationClassOrMethod(this.methodDeclaration, annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getKey());
    }

    public String getKey() {
        return toKey(this.methodDeclaration);
    }

}
