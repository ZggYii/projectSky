package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);


    /**
     * 根据id查找用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);


    /**
     * 查询每天的新增用户
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("select count(id) from user where create_time > #{startTime} and create_time < #{endTime}")
    Integer getBetweenCreateTime(LocalDateTime startTime, LocalDateTime endTime);


    /**
     * 查询这天之前的用户总数
     * @param endTime
     * @return
     */
    @Select("select count(id) from user where create_time < #{endTime}")
    Integer getBeforeCreateTime(LocalDateTime endTime);
}
