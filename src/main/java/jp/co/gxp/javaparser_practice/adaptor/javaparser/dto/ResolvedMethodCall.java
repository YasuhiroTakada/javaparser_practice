package jp.co.gxp.javaparser_practice.adaptor.javaparser.dto;

import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

/**
 * メソッド呼び出しを表すDTO
 * @param caller 呼ぶメソッド
 * @param receiver 呼ばれるメソッド
 */
public record ResolvedMethodCall(ResolvedMethodDeclaration caller, ResolvedMethodDeclaration receiver) {

}
