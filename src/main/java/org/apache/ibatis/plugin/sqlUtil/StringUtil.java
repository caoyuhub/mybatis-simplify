package org.apache.ibatis.plugin.sqlUtil;

/**
 * 字符串处理工具类
 * @author caoyu
 */
public class StringUtil {

    /**
     * 返回首字母小写
     * @param string
     * @return
     */
    public static String firstStringLow(String string){
        return string.substring(0,1).toLowerCase()+string.substring(1);
    }
}
