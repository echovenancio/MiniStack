package com.echovenancio.ministack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest // This annotation loads the full Spring application context
class MiniStackApplicationTests {

    @Test
    void contextLoads() {
        // This test method will simply pass if the Spring application context
        // loads without any exceptions (e.g., NoSuchBeanDefinitionException)
        // You don't need to add any specific assertions here.
    }
}
