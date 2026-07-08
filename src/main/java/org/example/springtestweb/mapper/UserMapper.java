package org.example.springtestweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springtestweb.model.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
