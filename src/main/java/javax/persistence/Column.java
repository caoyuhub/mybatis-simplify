package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 数据库字段注释
 * @author caoyu
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Column {

    /**
     * 数据库中的字段
     * @return
     */
    String name() default "";

    /**
     * 自定义查询 #key 数据库字段名  #value 查询时的值
     * @return
     */
    String  custom() default "";

    /**
     * 在findAll的时候是否使用自定义查询
     * @return
     */
    boolean findAllCustom() default true;
}
