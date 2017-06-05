package com.chat.config;

import com.test.utils.DBUnitHelper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public DBUnitHelper dbUnitHelper() {
        return new DBUnitHelper();
    }
}
