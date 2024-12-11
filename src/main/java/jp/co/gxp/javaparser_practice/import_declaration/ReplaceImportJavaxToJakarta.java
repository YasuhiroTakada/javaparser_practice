package jp.co.gxp.javaparser_practice.import_declaration;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class ReplaceImportJavaxToJakarta extends GenericVisitorAdapter<Boolean, Void> {

    @Override
    public Boolean visit(ImportDeclaration n, Void arg) {
        Optional<Name> qualifier = n.getName().getQualifier();
        if(qualifier.isEmpty()) {
            return Boolean.FALSE;
        }
        Name importName = qualifier.get();
        boolean isJavax = importName.asString().startsWith("javax");
        if(!isJavax) {
           return Boolean.FALSE;
        }
        Name newQualifier = replaceImportName(importName);
        n.replace(n.getName(), new Name(newQualifier, n.getName().getIdentifier()));
        return Boolean.TRUE;
    }

    private static Name replaceImportName(Name importName) {
        LinkedList<String> newList = new LinkedList<>(List.of(importName.asString().split("\\.")));
        newList.removeFirst();
        newList.addFirst("jakarta");
        return newList.stream()
            .map(Name::new)
            .reduce((q, identifier) -> new Name(q, identifier.getIdentifier())).orElse(importName);
    }

}
