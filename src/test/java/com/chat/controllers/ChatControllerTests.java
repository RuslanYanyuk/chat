package com.chat.controllers;

import com.chat.common.AbstractKafkaTest;
import com.chat.config.WebSecurityConfig;
import com.chat.config.WebSocketConfig;
import com.chat.models.Message;
import com.chat.models.User;
import com.chat.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

import static com.chat.config.WebSocketConfig.APP_PREFIX;
import static com.chat.config.WebSocketConfig.BROKER_PREFIX;
import static com.jayway.jsonassert.JsonAssert.with;
import static com.test.MessageFixtures.getTestMessage;
import static com.test.UserFixtures.getArrayOfUsers;
import static com.test.UserFixtures.getUser;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ChatControllerTests extends AbstractKafkaTest {

    @LocalServerPort
    private int randomServerPort;

    private DefaultStompFrameHandler messageHandler;

    private StompSession session;

    @Before
    public void setup() {
        dbUnitHelper.deleteAllFixtures().
                insertUsers()
                .insertUserContacts()
                .insertChatRooms()
                .insertChatRoomsToUsers();

        messageHandler = new DefaultStompFrameHandler();

        session = createStompSession(loginUser(getUser(1)));
    }

    @Test
    public void sendMessage_messageToExistingChatRoom_messageDelivered() {
        Message message = getTestMessage(1, null);

        subscribe("/" + TEST_TOPIC);
        send("/message/" + TEST_TOPIC, message);

        with(response()).assertThat("$.data", is(message.getData()));
    }

    @Test
    public void sendMessage_notParticipantOfChatRoom_accessDenied() {
        //TODO
    }

    @Test
    public void subscribe_chatRoom_subscribed() {
        StompSession.Subscription subscription = subscribe("/" + TEST_TOPIC);
        assertNotNull(subscription.getSubscriptionId());
    }

    @Test
    public void subscribe_notParticipantOfChatRoom_accessDenied() {
        //TODO
    }

    @Test
    public void getContacts_loggedInUser_listOfContactsReturned() {
        subscribeUserTo("/contacts");
        send("/get-contacts", null);

        with(response()).assertThat("$.[*].name", hasSize(2));
    }

    @Test
    public void getChatRooms_listReturned() {
        subscribeUserTo("/chat-rooms");
        send("/chat-rooms", null);

        with(response()).assertThat("$.[*].topic", hasSize(1));
    }

    @Test
    public void createChatRoom_participants_roomCreated() {
        User[] participants = getArrayOfUsers(2, 3);

        subscribeUserTo("/chat-rooms");
        send("/create-chat-room", participants);

        with(response()).assertThat("$.[*].topic", hasSize(2));
    }

    @Test
    public void createChatRoom_participantsNotFromContacts_errorReturned() {
        User[] participants = getArrayOfUsers(2, 4);

        subscribeUserTo("/errors");
        send("/create-chat-room", participants);

        with(response()).assertThat("$.message", is(UserService.USER_IS_NOT_CONTACT));
    }

    class TestStompSessionHandlerAdapter extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        }
    }

    class DefaultStompFrameHandler implements StompFrameHandler {

        private BlockingQueue<byte[]> blockingQueue;

        public DefaultStompFrameHandler() {
            blockingQueue = new LinkedBlockingDeque<>();
        }

        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return byte[].class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            blockingQueue.offer((byte[]) o);
        }

        public String getMessage() {
            byte[] data;
            try {
                data = blockingQueue.poll(4, SECONDS);
                return new String(data);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private WebSocketHttpHeaders loginUser(User user) {
        WebSocketHttpHeaders wsHeaders = new WebSocketHttpHeaders();
        HttpHeaders httpHeaders = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("username", user.getName());
        map.add("password", "password");
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        httpHeaders = restTemplate
                .postForEntity("http://localhost:" + randomServerPort + WebSecurityConfig.SIGN_IN_PAGE,
                        new HttpEntity<>(map, httpHeaders), Object.class)
                .getHeaders();
        wsHeaders.add(HttpHeaders.COOKIE, httpHeaders.getFirst(HttpHeaders.SET_COOKIE));
        return wsHeaders;
    }

    private StompSession createStompSession(WebSocketHttpHeaders httpHeaders) {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                singletonList(new WebSocketTransport(new StandardWebSocketClient()))));

        try {
            return stompClient
                    .connect("ws://localhost:" + randomServerPort + WebSocketConfig.ENDPOINT, httpHeaders,
                            new TestStompSessionHandlerAdapter())
                    .get(1, SECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void send(String route, T t) {
        try {
            session.send(APP_PREFIX + route, jacksonObjectMapper.writeValueAsString(t).getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void subscribeUserTo(String path) {
        session.subscribe("/user/topic" + path, messageHandler);
    }

    private StompSession.Subscription subscribe(String path) {
        return session.subscribe(BROKER_PREFIX + path, messageHandler);
    }

    public String response() {
        return messageHandler.getMessage();
    }
}
