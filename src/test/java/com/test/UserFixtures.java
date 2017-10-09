package com.test;

import com.chat.models.User;

/**
 * @author Ruslan Yaniuk
 * @date June 2017
 */
public class UserFixtures {

    public static final String NAME_PREFIX = "user";

    public static User getUser(int i) {
        User u = new User(NAME_PREFIX + i, "password");
        u.setId((long) i);
        return u;
    }

    public static User[] getArrayOfUsers(int... n) {
        User[] users = new User[n.length];

        for (int i = 0; i < n.length; i++) {
            users[i] = getUser(n[i]);
        }
        return users;
    }

    public static User getNonExistingUser() {
        return new User("nonExistingName", null);
    }
}
