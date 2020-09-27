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
     * @return
     */
    @Select
    List<T> findAll(T entity);

    /**
     * 通过ID查询用户
     * @param id
     * @return
     */
    @Select()
    T findById(H id);

    /**
     * 通过ID删除数据
     * @param id
     * @return
     */
    @Delete
    int deleteById(H id);

    /**
     * 添加实体
     * @param entity
     * @return
     */
    @Insert
    int insert(@Param("entity") Object... entity);

    /**
     * 通过ID修改实体
     * @param entity
     * @return
     */
    @Update
    int update(T entity);
}
