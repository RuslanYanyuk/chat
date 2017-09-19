/* Create schema for MySql 5 */
CREATE TABLE chat_rooms_to_users (
  chat_room_topic VARCHAR(191) NOT NULL,
  user_id         BIGINT       NOT NULL
);
CREATE TABLE chat_room (
  topic VARCHAR(191) NOT NULL,
  PRIMARY KEY (topic)
);
CREATE TABLE user (
  id   BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  PRIMARY KEY (id)
);
CREATE TABLE user_contacts (
  user_id    BIGINT NOT NULL,
  contact_id BIGINT NOT NULL
);

ALTER TABLE user ADD CONSTRAINT UK_name UNIQUE (name);
ALTER TABLE chat_rooms_to_users ADD CONSTRAINT FK__chat_rooms_to_users_to_user FOREIGN KEY (user_id) REFERENCES user (id);
ALTER TABLE chat_rooms_to_users ADD CONSTRAINT FK__chat_rooms_to_users_to_chat_room FOREIGN KEY (chat_room_topic) REFERENCES chat_room (topic);
ALTER TABLE user_contacts ADD CONSTRAINT FK__user_contacts_to_user_contacts FOREIGN KEY (contact_id) REFERENCES user (id);
ALTER TABLE user_contacts ADD CONSTRAINT FK__user_contacts_to_user FOREIGN KEY (user_id) REFERENCES user (id);