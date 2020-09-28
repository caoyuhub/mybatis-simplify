package org.apache.ibatis.plugin.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 基础mapper
 * @param <T>   实体类
 * @param <H>   ID类型
 * @author caoyu
 */
public interface BaseMapper<T,H> {


    /**
     * 查询所有
     * @param entity 实体类
     * @return 实体类的集合
     */
    @Select
    List<T> findAll(T entity);

    /**
     * 通过ID查询用户
     * @param id 查询的id
     * @return  查询的实体类
     */
    @Select()
    T findById(H id);

    /**
     * 通过ID删除数据
     * @param id    删除的id
     * @return  受影响的条数
     */
    @Delete
    int deleteById(H id);

    /**
     * 添加实体
     * @param entity    实体类的数组
     * @return  受影响的条数
     */
    @Insert
    int insert(@Param("entity") Object... entity);

    /**
     * 通过ID修改实体
     * @param entity    需要修改的实体类（必须要有id）
     * @return  受影响的条数
     */
    @Update
    int update(T entity);
}
