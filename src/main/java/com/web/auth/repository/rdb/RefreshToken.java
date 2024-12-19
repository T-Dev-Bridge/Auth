package com.web.auth.repository.rdb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.base.base.repository.rdb.CrdEntity;

@NoArgsConstructor
@Getter
@ToString
@Entity(name = "refresh_token")
@Table
public class RefreshToken extends CrdEntity {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "refresh_token", length = 2048)
    private String refreshToken;


    @Builder
    public RefreshToken(String refreshToken,
                        String userId,
                        String createdWho) {

        super(createdWho);

        this.refreshToken = refreshToken;
        this.userId = userId;
    }
}
