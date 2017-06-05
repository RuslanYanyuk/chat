package com.chat.repositories;

import com.chat.models.ChatRoom;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
public interface ChatRoomRepository extends CrudRepository<ChatRoom, String> {
}
