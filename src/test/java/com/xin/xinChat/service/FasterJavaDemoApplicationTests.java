package com.xin.xinChat.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xin.xinChat.MainApplication;
import com.xin.xinChat.mapper.UserMapper;
import com.xin.xinChat.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest(classes = MainApplication.class)
@RunWith(SpringRunner.class)
class FasterJavaDemoApplicationTests {


    @Resource
    private UserMapper userMapper;

    @Test
    void selectTest() {
       List<User> list = userMapper.selectList(new QueryWrapper<>());
    }

}
