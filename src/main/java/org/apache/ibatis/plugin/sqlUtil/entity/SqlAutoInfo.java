package org.apache.ibatis.plugin.sqlUtil.entity;

import java.util.List;

/**
 * 保存生成SQL的信息
 *
 * @author 曹渝
 * @date 2020/7/15 11:16
 **/
public class SqlAutoInfo {

    private String tableName;
    private FieldInfo idField;
    private List<FieldInfo> allField;
    private String entityName;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public FieldInfo getIdField() {
        return idField;
    }

    public void setIdField(FieldInfo idField) {
        this.idField = idField;
    }

    public List<FieldInfo> getAllField() {
        return allField;
    }

    public void setAllField(List<FieldInfo> allField) {
        this.allField = allField;
    }
}
