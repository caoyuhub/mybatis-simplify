package org.apache.ibatis.plugin.sqlUtil;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.plugin.consts.SqlAnalysisConst;
import org.apache.ibatis.plugin.mapper.BaseMapper;
import org.apache.ibatis.plugin.sqlUtil.entity.FieldInfo;
import org.apache.ibatis.plugin.sqlUtil.entity.SqlAutoInfo;

import javax.persistence.Column;
import javax.persistence.Custom;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成SQL的工具类
 *
 * @author 曹渝
 * @date 2020/7/15 10:36
 **/
public class SqlUtil {

    public static Map<String ,SqlAutoInfo> sqlAutoInfoCache = new HashMap<>();

    /**
     * 自动生成SQL
     * @param annotation
     * @param method
     * @param typeClass
     * @return
     */
    public static String[] autoSql(Annotation annotation, Method method,Class typeClass) throws Exception {
       SqlAutoInfo sqlAutoInfo = null;
        if(typeClass != null){
            Type[] types = typeClass.getGenericInterfaces();
            if(types != null && types.length > 0){
                for (Type type:types) {
                    ParameterizedType parameterizedType = ((ParameterizedType)type);
                    if(parameterizedType.getRawType()== BaseMapper.class){
                        Class entityClass = (Class) parameterizedType.getActualTypeArguments()[0];
                        sqlAutoInfo = getSqlAutoInfo(entityClass);
                    }
                }
            }
        }


        if (annotation instanceof Select) {
            if(typeClass == null || !"".equals(((Select)annotation).value()[0])){
                return ((Select)annotation).value();
            }
            return getSelectSql(sqlAutoInfo,method);
        } else if (annotation instanceof Update) {
            if(typeClass == null || !"".equals(((Update)annotation).value()[0])){
                return ((Update)annotation).value();
            }
            return getUpdateSql(sqlAutoInfo,method);
        } else if (annotation instanceof Insert) {
            if(typeClass == null || !"".equals(((Insert)annotation).value()[0])){
                return ((Insert)annotation).value();
            }
            return getInsertSql(sqlAutoInfo,method);
        } else if (annotation instanceof Delete) {
            if(typeClass == null || !"".equals(((Delete)annotation).value()[0])){
                return ((Delete)annotation).value();
            }
            return getDeleteSql(sqlAutoInfo,method);
        }
        return null;
    }

    private static String [] getUpdateSql(SqlAutoInfo sqlAutoInfo,Method method) throws Exception {
        String methodName =  method.getName();
        if(!methodName.equals(SqlAnalysisConst.UPDATE)){
            throw new Exception(methodName+"：在@Update中需要写入 mybatis支持的SQL语句");
        }
        StringBuffer updateSql = new StringBuffer("<script> UPDATE ").append(sqlAutoInfo.getTableName()).append(" SET ");
        List<FieldInfo> allField = sqlAutoInfo.getAllField();
        for (int i = 0; i < allField.size(); i++) {
            if(i!=0){
                updateSql.append(",");
            }
            updateSql.append(" ").append(allField.get(i).getDatabaseName()).append(" = #{").append(allField.get(i).getEntityName()).append("} ");
        }
        FieldInfo idFieldInfo =  sqlAutoInfo.getIdField();
        if(idFieldInfo == null){
            throw new Exception(sqlAutoInfo.getEntityName()+" 没用用@Id标识出主键。");
        }else{
            updateSql.append(" WHERE ").append(idFieldInfo.getDatabaseName()).append(" = #{").append(idFieldInfo.getEntityName()).append("} ");
        }

        updateSql.append("</script>");
        return new String[]{updateSql.toString()};
    }

    /**
     * 获取新增的sql
     * @param sqlAutoInfo
     * @param method
     * @return
     */
    private static String [] getInsertSql(SqlAutoInfo sqlAutoInfo,Method method) throws Exception {
        String methodName =  method.getName();
        if(!methodName.equals(SqlAnalysisConst.INSERT)){
            throw new Exception(methodName+"：在@Insert中需要写入 mybatis支持的SQL语句");
        }

        List<FieldInfo> allField = sqlAutoInfo.getAllField();
        StringBuffer insertSql = new StringBuffer("<script> INSERT INTO ").append(sqlAutoInfo.getTableName()).append("(");
        for (int i = 0; i < allField.size(); i++) {
            if(i != 0){
                insertSql.append(",");
            }
            insertSql.append(allField.get(i).getDatabaseName());
        }
        insertSql.append(") VALUES <foreach collection=\"entity\"  separator=\",\"  item=\"i\">(");
        for (int i = 0; i < allField.size(); i++) {
            if(i != 0){
                insertSql.append(",");
            }
            insertSql.append(" #{i.").append(allField.get(i).getEntityName()).append("} ");
        }
        insertSql.append(")</foreach></script> ");
        return new String[]{insertSql.toString()};
    }

    /**
     * 获取删除的SQL
     * @param sqlAutoInfo
     * @param method
     * @return
     * @throws Exception
     */
    private static String [] getDeleteSql(SqlAutoInfo sqlAutoInfo,Method method) throws Exception {

        String methodName = method.getName();
        if(!methodName.startsWith(SqlAnalysisConst.DELETE_BY)){
            throw new Exception(methodName+",删除语句只能是deleteBy开始");
        }

        StringBuffer deleteSql = new StringBuffer("<script> DELETE FROM ");
        deleteSql.append(sqlAutoInfo.getTableName()).append(" ");

        //添加delete中的where
        deleteSql.append(getSqlWhere(methodName.split(SqlAnalysisConst.DELETE_BY)[1],sqlAutoInfo,method));

        deleteSql.append(" </script> ");
        return new String[]{deleteSql.toString()};
    }

    /**
     * 获取查询语句的sql
     * @param sqlAutoInfo
     * @param method
     * @return
     * @throws Exception
     */
    private static String[] getSelectSql(SqlAutoInfo sqlAutoInfo,Method method) throws Exception {
        //是否在最后加limit 1
        boolean isLimitOnt = false;
        //需要解析的方法名字
        String methodName = method.getName();
        //判断是不是find开头
        if(methodName.indexOf(SqlAnalysisConst.FIND) != 0){
            return null;
        }

        //名称中的条件部分
        String whereName = null;
        //判断是否只返回第一条
        if(methodName.indexOf(SqlAnalysisConst.FIND_FIRST_BY) == 0){
            isLimitOnt = true;
            whereName = methodName.split(SqlAnalysisConst.FIND_FIRST_BY)[1];
        }else if(methodName.indexOf(SqlAnalysisConst.FIND_BY) == 0){
            whereName = methodName.split(SqlAnalysisConst.FIND_BY)[1];
        }else if(methodName.equals(SqlAnalysisConst.FIND_ALL)){
            whereName = SqlAnalysisConst.ENTITY_ALL;
        }else if(methodName.startsWith(SqlAnalysisConst.FIND_ALL)){
            whereName = SqlAnalysisConst.ENTITY_ALL+methodName;

        }else{
            return null;
        }

        List<FieldInfo> allField = sqlAutoInfo.getAllField();

        StringBuilder sql = new StringBuilder("<script> SELECT ");
        for (int i = 0; i < allField.size(); i++) {
            if(i!=0){
                sql.append(",");
            }
            sql.append(allField.get(i).getDatabaseName()).append(" AS ").append(allField.get(i).getEntityName());
        }
        sql.append(" FROM ").append(sqlAutoInfo.getTableName()).append(" ");


        //添加where语句
        sql.append(getSqlWhere(whereName,sqlAutoInfo,method));
        //添加order by语句
        sql.append(getSqlOrderBy(whereName,sqlAutoInfo,method));
        if(isLimitOnt){
            sql.append(" LIMIT 1");
        }
        sql.append("</script>");
        return new String[]{sql.toString()};
    }


    /**
     * 获取order by语句
     * @param orderByString
     * @param sqlAutoInfo
     * @param method
     * @return
     */
    private static String getSqlOrderBy(String orderByString,SqlAutoInfo sqlAutoInfo,Method method){

        StringBuffer sql = new StringBuffer(" ORDER BY ");

        String []tmpOrderByString = orderByString.split(SqlAnalysisConst.ORDER_BY);


        if(tmpOrderByString.length != 2){
            return "";
        }

        orderByString = tmpOrderByString[1];

//        String []orderBys = stringUpTpArray(orderByString);

        String []orderBys = division(orderByString,new String[]{SqlAnalysisConst.DESC,SqlAnalysisConst.ASC});

        Map<String ,FieldInfo> entityNameToFieldInfo = new HashMap<>();
        for (FieldInfo fieldInfo:sqlAutoInfo.getAllField()) {
            entityNameToFieldInfo.put(fieldInfo.getEntityName(),fieldInfo);
        }

        for (int i = 0; i < orderBys.length; i++) {

            String orderString  = orderBys[i];
            if(SqlAnalysisConst.DESC.equals(orderString) || SqlAnalysisConst.ASC.equals(orderString) ){
                sql.append(" ").append(orderString.toUpperCase()).append(" ");
                continue;
            }

            FieldInfo fieldInfo= entityNameToFieldInfo.get(StringUtil.firstStringLow(orderString));

            if(i!=0){
                sql.append(" ").append(",").append(" ");
            }
            try{
                sql.append(" ").append(fieldInfo.getDatabaseName()).append(" ");;
            }catch (Exception e){
                System.out.println();
            }
        }

        return sql.toString();
    }

    /**
     * 生成where语句
     * @param whereName
     * @param sqlAutoInfo
     * @param method
     * @return
     * @throws Exception
     */
    private static String getSqlWhere(String whereName,SqlAutoInfo sqlAutoInfo,Method method) throws Exception {
        if(whereName == null || "".equals(whereName)){
            return "";
        }

        StringBuffer sql = null;
        List<FieldInfo> allField = sqlAutoInfo.getAllField();
        if(whereName.startsWith(SqlAnalysisConst.ENTITY_ALL)){
            sql = new StringBuffer(" WHERE 1=1 ");
            for (FieldInfo fieldInfo:allField) {
                sql.append("<if test=\"");
                sql.append(fieldInfo.getEntityName());
                sql.append(" != null\"> AND ");

                //注意不要让deleteBy和updateBy进来 不然会出事
                if(fieldInfo.getFindAllCustom() && fieldInfo.getCustom() != null && !"".equals(fieldInfo.getCustom())){
                   sql.append(fieldInfo.getCustomOut());
                }else{
                    sql.append(fieldInfo.getDatabaseName()).append(" = #{");
                    sql.append(fieldInfo.getEntityName()).append("}");
                }

                sql.append("</if> ");
            }
        }else{

            //查看这个方法有没有自定义sql
            Custom custom = method.getAnnotation(Custom.class);

            sql = new StringBuffer(" WHERE ");
            whereName = whereName.split(SqlAnalysisConst.ORDER_BY)[0];
            if(whereName == null || "".equals(whereName)){
                return "";
            }

//            String []fieldNames = stringUpTpArray(whereName);
            String []fieldNames = division(whereName,new String[]{SqlAnalysisConst.AND,SqlAnalysisConst.OR,SqlAnalysisConst.IN,SqlAnalysisConst.NOT_IN});


            Map<String ,FieldInfo> entityNameToFieldInfo = new HashMap<>();
            for (FieldInfo fieldInfo:allField) {
                entityNameToFieldInfo.put(fieldInfo.getEntityName(),fieldInfo);
            }

            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                if(SqlAnalysisConst.IN.equals(fieldName) || SqlAnalysisConst.NOT_IN.equals(fieldName)){
                    String sign = fieldName;
                    i++;
                    fieldName = fieldNames[i];
                    fieldName = StringUtil.firstStringLow(fieldName);
                    FieldInfo fieldInfo = entityNameToFieldInfo.get(fieldName);

                    sql.append(" ").append(fieldInfo.getDatabaseName()).append(" ");
                    if(SqlAnalysisConst.IN.equals(sign)){
                        sql.append(SqlAnalysisConst.IN.toUpperCase());
                    }else{
                        sql.append(SqlAnalysisConst.NOT_IN_SQL.toUpperCase());
                    }

                    sql.append(" ").append("<foreach item=\"item\" collection=\"");
                    sql.append(fieldInfo.getEntityName());
                    sql.append("\" separator=\",\" open=\"(\" close=\")\"> #{item}</foreach>");
                }else{
                    if(SqlAnalysisConst.AND.equals(fieldName) || SqlAnalysisConst.OR.equals(fieldName)){
                        sql.append(" ").append(fieldName.toUpperCase()).append(" ");
                        continue;
                    }
                    fieldName = StringUtil.firstStringLow(fieldName);
                    FieldInfo fieldInfo = entityNameToFieldInfo.get(fieldName);
                    if(fieldInfo == null){
                        throw new Exception(method.getName()+"中的"+fieldName+"没有找到");
                    }

                    if(custom != null && !"".equals(fieldInfo.getCustom())){
                        sql.append(" ").append(fieldInfo.getCustomOut());
                    }else{
                        sql.append(" ").append(fieldInfo.getDatabaseName()).append(" = #{").append(fieldInfo.getEntityName());
                        sql.append("} ");
                    }

                }


            }
        }
        return sql.toString();
    }


    /**
     * 通过特定的字符串吧字符串分割为数组
     * @param string
     * @param divisionKey
     * @return
     */
    public static String [] division(String string ,String []divisionKey){

       for(String key:divisionKey){
           if(string.contains(key)){
               string = string.replaceAll(key,SqlAnalysisConst.SPACE+key+SqlAnalysisConst.SPACE);
           }
       }

        string =string.trim();
        while (string.contains(SqlAnalysisConst.DOUBLE_SPACE)){
            string = string.replaceAll(SqlAnalysisConst.DOUBLE_SPACE,SqlAnalysisConst.SPACE);
        }

        return string.split(SqlAnalysisConst.SPACE);
    }

//    /**
//     * 吧字符串通过大写分割成数组
//     * @param string
//     * @return
//     */
//    private static String [] stringUpTpArray(String string){
//        char [] ss = string.toCharArray();
//        List<String > valueArray = new ArrayList<>();
//        List<Character> value = null;
//        for (char c:ss) {
//            if(Character.isUpperCase(c)){
//                if(value == null){
//                    value = new ArrayList<>();
//                }
//                else{
//                    Character[] vs = new Character[value.size()];
//                    value.toArray(vs);
//                    valueArray.add(charToString(vs));
//                    value = new ArrayList<>();
//                }
//            }
//            value.add(c);
//        }
//
//        Character[] vs = new Character[value.size()];
//        value.toArray(vs);
//        valueArray.add(charToString(vs));
//
//        String []values = new String[valueArray.size()];
//        valueArray.toArray(values);
//        return values;
//    }


//    private static String charToString(Character[] characters){
//        char []c = new char[characters.length];
//        for (int i = 0; i < characters.length; i++) {
//            c[i] = characters[i].charValue();
//        }
//        return String.valueOf(c);
//    }

    /**
     * 获取实体类中的字段信息
     * @param entityClass
     * @return
     * @throws Exception
     */
    private static SqlAutoInfo getSqlAutoInfo(Class entityClass) throws Exception {

        if(sqlAutoInfoCache == null){
            sqlAutoInfoCache = new HashMap<>();
        }

        SqlAutoInfo sqlAutoInfo = sqlAutoInfoCache.get(entityClass.getName());
        if(sqlAutoInfo != null){
            return sqlAutoInfo;
        }


        Table table = null;
        //获取类上面所有的注解
        Annotation[] classAnnotations =  entityClass.getAnnotations();
        //循环拿到需要的注解
        for (Annotation classAnnotation:classAnnotations) {
            if(classAnnotation instanceof Table){
                table = (Table) classAnnotation;
            }
        }
        if(table == null){
            throw new Exception(entityClass.getName()+"中没有@Table注解");
        }
        sqlAutoInfo = new SqlAutoInfo();
        sqlAutoInfo.setEntityName(entityClass.getName());
        FieldInfo fieldInfo;
        List<FieldInfo> allFieldInfo = new ArrayList<>();
        //循环拿到所有的方法
        for (Method method:entityClass.getMethods()) {
            Column column = method.getAnnotation(Column.class);
            if(column!= null){
                fieldInfo = new FieldInfo();
                //获取ID
                Id id = method.getAnnotation(Id.class);

                fieldInfo.setDatabaseName(column.name());
                String methodName = method.getName();
                String fieldName = methodName.substring(3);
                fieldName = StringUtil.firstStringLow(fieldName);
                fieldInfo.setEntityName(fieldName);
                if(!"".equals(column.custom())){
                    fieldInfo.setCustom(column.custom());
                }
                fieldInfo.setFindAllCustom(column.findAllCustom());
                if(id != null){
                    sqlAutoInfo.setIdField(fieldInfo);
                }
                allFieldInfo.add(fieldInfo);
            }
        }
        sqlAutoInfo.setAllField(allFieldInfo);
        sqlAutoInfo.setTableName(table.name());

        sqlAutoInfoCache.put(entityClass.getName(),sqlAutoInfo);
        return sqlAutoInfo;
    }
}
