package com.chat.controllers;

import com.chat.common.AbstractKafkaTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static com.chat.config.WebSecurityConfig.SIGN_UP_PAGE;
import static com.chat.controllers.AuthController.REDIRECT_HOME;
import static com.chat.controllers.AuthController.REDIRECT_SIGN_UP_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * @author Ruslan Yaniuk
 * @date September 2017
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTests extends AbstractKafkaTest {

    private MockMvc mockMvc;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected WebApplicationContext wac;

    @Before
    public void setup() {
        dbUnitHelper.deleteAllFixtures().
                insertUsers()
                .insertUserContacts()
                .insertChatRooms()
                .insertChatRoomsToUsers();

        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Test
    public void getRegistrationPage() throws Exception {
        mockMvc.perform(get(SIGN_UP_PAGE))
                .andExpect(status().isOk())
                .andExpect(view().name("sign-up"));
    }

    @Test
    public void registerUser_correctDetails_userRegisteredAndLoggedIn() throws Exception {
        mockMvc.perform(post(SIGN_UP_PAGE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("name=user10&password=password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_HOME));
    }

    @Test
    public void registerUser_inCorrectDetails_registrationFails() throws Exception {
        mockMvc.perform(post(SIGN_UP_PAGE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("name=us&password=pa"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SIGN_UP_ERROR));

        mockMvc.perform(post(SIGN_UP_PAGE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("name=user1&password=password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SIGN_UP_ERROR));
    }
}