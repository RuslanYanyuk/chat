package com.chat.config;

import com.chat.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static com.chat.config.WebSocketConfig.*;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String ALL_SUBDIR_MATCHER = "/**";
    public static final String SIGN_IN_PAGE = "/sign-in";
    public static final String SIGN_UP_PAGE = "/sign-up";

    @Autowired
    UserService userService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/assets/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(SIGN_UP_PAGE)
                .permitAll()
                .antMatchers(
                        "/",
                        "/user" + ALL_SUBDIR_MATCHER,
                        ENDPOINT + ALL_SUBDIR_MATCHER,
                        BROKER_PREFIX + ALL_SUBDIR_MATCHER,
                        APP_PREFIX + ALL_SUBDIR_MATCHER).hasRole("USER")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage(SIGN_IN_PAGE)
                .permitAll()
                .and()
                .logout()
                .permitAll().and()
                .csrf()
                .disable();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }
}
