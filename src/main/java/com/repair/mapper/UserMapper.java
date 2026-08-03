package com.repair.mapper;

import com.repair.entity.User;
import org.apache.catalina.realm.UserDatabaseRealm;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    //插入操作
    @Insert("INSERT INTO user(username, password, real_name, phone, role, status, building, unit, room, skill, max_workload) " +
            "VALUES(#{username}, #{password}, #{realName}, #{phone}, #{role}, #{status}, #{building}, #{unit}, #{room}, #{skill}, #{maxWorkload})")

    //启动获取自动生成的主键，把生成的主键存入到User对象的id属性里
    @Options(useGeneratedKeys = true,keyProperty = "id")

    //影响的行数
    int insert(User user);

    @Delete("DELETE FROM user WHERE id = #{id}") //删除操作
    int deleteById(Integer id);

    //修改操作
    @Update("UPDATE user SET username=#{username}, real_name=#{realName}, phone=#{phone}, " +
            "role=#{role}, status=#{status}, building=#{building}, unit=#{unit}, room=#{room}, " +
            "skill=#{skill}, max_workload=#{maxWorkload} WHERE id=#{id}")

            int updateById(User user);

    //按id查询
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Integer id);
    //按名字查询
    @Select("SELECT * FROM user WHERE username = #{username}")
    User seletcByUsername(String username);
    //按角色查询所有用户
    @Select("SELECT * FROM user WHERE username = #{role}")
    List<User> selectByRole(Integer role);
    //查询所有用户
    @Select("SELECT *FROM user")
    List<User> selectAll();
}