package com.chat.common;

import com.chat.config.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.utils.DBUnitHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * @author Ruslan Yaniuk
 * @date June 2017
 */
@Import(TestConfig.class)
public abstract class AbstractTest {

    @Autowired
    protected DBUnitHelper dbUnitHelper;

    @Autowired
    protected ObjectMapper jacksonObjectMapper;
}
