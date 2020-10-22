package com.vivi.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author wangwei
 * 2020/10/10 17:26
 *
 * 此类用于处理 @ListValue注解 标注在 Integer 类型字段
 * implements ConstraintValidator<ListValue, T> 用于处理 @ListValue注解 标注在 T 类型字段
 *
 * 这个 T 需要对应 ListValue注解 定义时 value 字段的类型
 */
public class ListValueValidator implements ConstraintValidator<ListValue, Integer> {

    // 可取的范围
    private Set<Integer> valueSet = new HashSet<>();;

    /**
     * 初始化
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        // 获取到使用注解时指定的允许的数值
        for (int val : constraintAnnotation.value()) {
            valueSet.add(val);
        }
    }

    /**
     * 是否校验成功
     * @param integer
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        // 提供的数值是否是允许的可取值
        return valueSet.contains(integer);
    }
}
