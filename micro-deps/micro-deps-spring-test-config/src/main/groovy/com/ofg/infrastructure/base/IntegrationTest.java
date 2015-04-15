package com.ofg.infrastructure.base;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.ofg.config.BasicProfiles.TEST;

/**
 * Base JUnit test class for Spring's web application
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles(TEST)
public abstract class IntegrationTest {
}
