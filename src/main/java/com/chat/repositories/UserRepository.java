package com.chat.repositories;

import com.chat.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findByName(String name);

    List<User> findByNameStartingWithOrderByNameAsc(String name);
}
