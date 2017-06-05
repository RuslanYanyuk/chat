package com.chat.repositories;

import com.chat.models.User;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
public interface UserRepository extends CrudRepository<User, Long> {
    User findByName(String name);
}
