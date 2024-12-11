package jp.co.gxp.javaparser_practice.adaptor.javaparser.dto;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * 呼ぶ - 呼ばれるメソッドの関係を持つ
 * @param caller 呼ぶメソッド
 * @param receiver 呼ばれるメソッド
 */
public record RelatedMethodDefinition(MethodDeclaration caller, MethodDeclaration receiver) {

}
