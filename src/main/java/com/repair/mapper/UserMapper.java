package com.repair.mapper;

import com.repair.entity.User;
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

    //修改个人信息（手机号、真实姓名）
    @Update("UPDATE user SET real_name=#{realName}, phone=#{phone} WHERE id=#{id}")
    int updateProfile(@Param("id") Integer id, @Param("realName") String realName, @Param("phone") String phone);

    //按id查询
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Integer id);
    //按名字查询
    @Select("SELECT * FROM user WHERE username = #{username}")
    User seletcByUsername(String username);
    //按角色查询所有用户
    @Select("SELECT * FROM user WHERE role = #{role}")
    List<User> selectByRole(Integer role);
    //查询所有用户
    @Select("SELECT *FROM user")
    List<User> selectAll();

    /**
     * 统计总用户数
     */
    @Select("SELECT COUNT(*) FROM user")
    Long countAll();

    // ==================== 用户管理（管理员） ====================

    //分页查询用户列表（角色/关键字过滤）
    @Select("<script>SELECT * FROM user WHERE 1=1 " +
            "<if test='role != null'>AND role = #{role}</if> " +
            "<if test='keyword != null and keyword != \"\"'>AND (username LIKE CONCAT('%',#{keyword},'%') OR real_name LIKE CONCAT('%',#{keyword},'%') OR phone LIKE CONCAT('%',#{keyword},'%'))</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{pageSize}</script>")
    List<User> selectPage(@Param("role") Integer role, @Param("keyword") String keyword,
                          @Param("offset") int offset, @Param("pageSize") int pageSize);

    //分页统计总数
    @Select("<script>SELECT COUNT(*) FROM user WHERE 1=1 " +
            "<if test='role != null'>AND role = #{role}</if> " +
            "<if test='keyword != null and keyword != \"\"'>AND (username LIKE CONCAT('%',#{keyword},'%') OR real_name LIKE CONCAT('%',#{keyword},'%') OR phone LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    Long countPage(@Param("role") Integer role, @Param("keyword") String keyword);

    //禁用/启用账号
    @Update("UPDATE user SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    //重置密码
    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    // ==================== 维修工档案 ====================

    //维修工列表（技能/在岗状态过滤）
    @Select("<script>SELECT * FROM user WHERE role = 3 " +
            "<if test='onDuty != null'>AND on_duty = #{onDuty}</if> " +
            "<if test='skill != null and skill != \"\"'>AND skill LIKE CONCAT('%',#{skill},'%')</if> " +
            "ORDER BY on_duty DESC, id ASC</script>")
    List<User> selectWorkers(@Param("skill") String skill, @Param("onDuty") Integer onDuty);

    //更新维修工档案（技能标签/在岗状态/服务区域/最大接单量）
    @Update("UPDATE user SET skill=#{skill}, on_duty=#{onDuty}, service_area=#{serviceArea}, max_workload=#{maxWorkload} WHERE id=#{id}")
    int updateWorkerProfile(User user);
}
