package jp.co.gxp.javaparser_practice.adaptor.javaparser;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

public interface MethodSupport {

    default boolean hasAnnotationClassOrMethod(MethodDeclaration method, String annotation) {
        String annotationName = Stream.of(annotation.split("\\.")).toList().getLast();
        return
            method.getAnnotationByName(annotationName).isPresent()
            || method.getAnnotations().stream().anyMatch(annotationExpr -> annotationExpr.resolve().getQualifiedName().equals(annotation))
            || method.resolve().declaringType().hasAnnotation(annotation);
    }

    default Optional<ResolvedMethodDeclaration> implementedInterfaceMethod(ResolvedMethodDeclaration method) {
        return method.declaringType().getAllAncestors()
            .stream()
            .map(i -> i.getDeclaredMethods().stream().filter(m -> m.getSignature().equals(method.getSignature()))
                .findFirst()
                .orElse(null))
            .filter(Objects::nonNull)
            .findFirst()
            .map(MethodUsage::getDeclaration);
    }

    default String toKey(MethodDeclaration method) {
        return toKey(method.resolve());
    }

    default String toKey(ResolvedMethodDeclaration method) {
        return method.getQualifiedSignature();
    }
}
