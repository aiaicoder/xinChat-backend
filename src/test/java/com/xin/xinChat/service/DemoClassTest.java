package com.xin.xinChat.service;

import org.junit.jupiter.api.Test;

import javax.annotation.Resource;


public class DemoClassTest {

    @Resource
    private UserContactService userContactService;



    @Test
    public void testSearch() {
        // Arrange
        String userId = "123";
        String contactId = "456";
        // Mocking the behavior of the service
       userContactService.search(userId, contactId);

    }
}
