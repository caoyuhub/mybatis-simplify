package org.apache.ibatis.plugin.sqlUtil.entity;

/**
 * 字段信息
 *
 * @author 曹渝
 * @date 2020/7/15 11:21
 **/
public class FieldInfo {

    private String databaseName;
    private String entityName;
    private String custom;
    private Boolean findAllCustom;

    public Boolean getFindAllCustom() {
        return findAllCustom;
    }

    public void setFindAllCustom(Boolean findAllCustom) {
        this.findAllCustom = findAllCustom;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }


    public String getCustomOut() {
        return custom = custom
                .replaceAll("#key",getDatabaseName())
                .replaceAll("#value","#{"+getEntityName()+"}");
    }
}
