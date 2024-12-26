create table auth.refresh_token (
    user_id varchar(255) primary key,
    refresh_token varchar(2048),
    created_who varchar(255),
    created_at TIMESTAMP
);