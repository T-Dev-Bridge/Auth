package com.web.auth.service;

import com.web.auth.repository.rdb.RefreshToken;
import lombok.Data;
import org.base.base.service.CrdDto;

@Data
public class RefreshTokenDto extends CrdDto {

    private String userId;
    private String refreshToken;

    public RefreshTokenDto() {}

    public RefreshTokenDto(RefreshToken entity) {
        super(entity);
        this.userId = entity.getUserId();
        this.refreshToken = entity.getRefreshToken();
    }

    public RefreshToken toEntity() {
        return RefreshToken.builder()
                .userId(this.userId)
                .refreshToken(this.refreshToken)
                .createdWho(this.createdWho)
                .build();
    }
}
