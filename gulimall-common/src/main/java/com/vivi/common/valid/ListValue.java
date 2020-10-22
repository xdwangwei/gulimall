package com.vivi.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author wangwei
 * 2020/10/10 17:22
 *
 * 自定义校验注解(如@NotBlank,@Email)，用于校验有限值类型的属性（如只能取1,2）
 */
@Documented
// 指定用于处理注解的校验器
@Constraint(validatedBy = {ListValueValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListValue {
    // 校验失败时的显示信息,
    // 默认去resources下ValidationMessages.properties中找。这里指定的是key
    String message() default "{com.vivi.common.valid.ListValue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // 设置取值范围
    int[] value() default {};
}
