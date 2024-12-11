package jp.co.gxp.javaparser_practice.adaptor.javaparser.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import jp.co.gxp.javaparser_practice.adaptor.javaparser.MethodSupport;
import jp.co.gxp.javaparser_practice.adaptor.javaparser.dto.RelatedMethodDefinition;
import jp.co.gxp.javaparser_practice.adaptor.javaparser.dto.ResolvedMethodCall;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

public class MethodRepository implements MethodSupport {

    /** メソッド呼び出しをとりあえずためる */
    private final Map<String, MethodDeclaration> methodDeclarationMap = new ConcurrentHashMap<>();

    /** メソッド呼び出しをとりあえずためる */
    private final List<ResolvedMethodCall> methodCalls = new CopyOnWriteArrayList<>();

    public void put(MethodDeclaration md) {
         methodDeclarationMap.put(md.resolve().getQualifiedSignature(), md);
     }

    public void add(MethodDeclaration caller, ResolvedMethodDeclaration receiver) {
        methodCalls.add(new ResolvedMethodCall(caller.resolve(), receiver));
        methodDeclarationMap.putIfAbsent(caller.resolve().getQualifiedSignature(), caller);
    }

    public void add(ResolvedMethodDeclaration caller, ResolvedMethodDeclaration receiver) {
        methodCalls.add(new ResolvedMethodCall(caller, receiver));
    }

    // まず MethodDeclaration -> ResolvedMethodDeclarationとなっている呼び出し関係を
    // MethodDeclaration -> MethodDeclarationの型に解決する
    private final Map<String, List<RelatedMethodDefinition>> relatedMethodDefinitionMap = new HashMap<>();
    // 呼ばれる側のシグネチャをキーに持つマップ. 呼ばれる側から参照をたどりたいときに使う
    private final Map<String, List<RelatedMethodDefinition>> inversedMethodDefinitionMap = new HashMap<>();

    public void tree() {
        for(var methodCall : methodCalls) {
            String callerSignature = methodCall.caller().getQualifiedSignature();
            MethodDeclaration callerDeclaration = methodDeclarationMap.get(callerSignature);

            // 呼ぶ側が不明であれば無視する（ありえない）
            if (callerDeclaration == null) {
                continue;
            }

            String receiverSignature = methodCall.receiver().getQualifiedSignature();
            MethodDeclaration receiverDeclaration = methodDeclarationMap.get(receiverSignature);
            RelatedMethodDefinition relatedMethodDefinition = new RelatedMethodDefinition(callerDeclaration, receiverDeclaration);
            relatedMethodDefinitionMap.computeIfAbsent(callerSignature, k -> new ArrayList<>()).add(relatedMethodDefinition);
            inversedMethodDefinitionMap.computeIfAbsent(receiverSignature, k -> new ArrayList<>()).add(relatedMethodDefinition);
        }
    }

    public List<ResolvedMethodCall> find(Predicate<ResolvedMethodCall> condition) {
        return methodCalls.stream().filter(condition).toList();
    }

    private List<LinkedList<MethodDeclaration>> downWiseRecursive(LinkedList<MethodDeclaration> stack, MethodDeclaration method) {
        boolean isCyclicCall = stack.stream().anyMatch(m -> m.resolve().getQualifiedSignature().equals(method.resolve().getSignature()));
        if(isCyclicCall) {
            return List.of(stack);
        }
        stack.add(method);
        List<RelatedMethodDefinition> receivers = relatedMethodDefinitionMap.get(method.resolve().getQualifiedSignature());
        if(receivers == null || receivers.isEmpty()) {
            return List.of(stack);
        }

        List<LinkedList<MethodDeclaration>> result = new ArrayList<>();
        for (var receiver : receivers) {
            List<LinkedList<MethodDeclaration>> subResult = downWiseRecursive(new LinkedList<>(stack), receiver.receiver());
            result.addAll(subResult);
        }
        return result;
    }

    public List<LinkedList<MethodDeclaration>> createMethodCallStackUpWise(MethodDeclaration leaf) {
        return upWiseRecursive(new LinkedList<>(), leaf);
    }


    public List<LinkedList<MethodDeclaration>> createMethodCallStackUpWise(ResolvedMethodDeclaration leaf) {
        MethodDeclaration declaration = methodDeclarationMap.get(leaf.getQualifiedSignature());
        if(declaration != null) {
            return createMethodCallStackUpWise(declaration);
        }

        return inversedMethodDefinitionMap.getOrDefault(leaf.getQualifiedSignature(), List.of())
            .stream()
            .flatMap(rmd -> createMethodCallStackUpWise(rmd.caller()).stream())
            .toList();
    }


    private List<LinkedList<MethodDeclaration>> upWiseRecursive(LinkedList<MethodDeclaration> stack, MethodDeclaration method) {
        boolean isCyclicCall = stack.stream().anyMatch(m -> m.resolve().getQualifiedSignature().equals(method.resolve().getSignature()));
        if(isCyclicCall) {
            return List.of(stack);
        }
        stack.addFirst(method);
        List<RelatedMethodDefinition> callers = inversedMethodDefinitionMap.getOrDefault(method.resolve().getQualifiedSignature(), new ArrayList<>());
        // callerが実装クラスの場合、interfaceでも解消する
        implementedInterfaceMethod(method.resolve())
            .ifPresent(interfaceMethod -> {
                List<RelatedMethodDefinition> interfaceCallers = inversedMethodDefinitionMap.getOrDefault(interfaceMethod.getQualifiedSignature(), List.of());
                callers.addAll(interfaceCallers);
        });

        if(callers == null || callers.isEmpty()) {
            return List.of(stack);
        }

        return callers.stream()
            .flatMap(caller -> upWiseRecursive(new LinkedList<>(stack), caller.caller()).stream())
            .toList();
    }

}
