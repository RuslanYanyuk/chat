ALTER TABLE chat_rooms_to_users DROP FOREIGN KEY FK__chat_rooms_to_users_to_chat_room;
DROP INDEX FK__chat_rooms_to_users_to_chat_room ON chat_rooms_to_users;

ALTER TABLE chat_room DROP PRIMARY KEY;

ALTER TABLE chat_room ADD COLUMN id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT;

ALTER TABLE chat_rooms_to_users ADD COLUMN chat_room_id BIGINT NOT NULL;

UPDATE chat_rooms_to_users cu
  INNER JOIN chat_room cr ON cu.chat_room_topic = cr.topic
SET cu.chat_room_id = cr.id;

ALTER TABLE chat_rooms_to_users ADD CONSTRAINT FK__chat_rooms_to_users_to_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id);

ALTER TABLE chat_rooms_to_users DROP COLUMN chat_room_topic;