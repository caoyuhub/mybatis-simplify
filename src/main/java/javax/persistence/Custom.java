package javax.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 是否启用自定义查询
 * @author caoyu
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface Custom {
}
