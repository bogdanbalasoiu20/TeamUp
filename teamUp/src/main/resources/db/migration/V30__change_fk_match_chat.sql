alter table match_chat_messages
drop constraint if exists fk_mcm_sender_is_participant;

alter table match_chat_messages
add constraint fk_mcm_sender_user foreign key (sender_id) references users(id) on delete restrict;