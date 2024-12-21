package com.bridge.auth.service;

import com.bridge.auth.repository.rdb.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void save(RefreshTokenDto refreshTokenDto, String userId) {
        refreshTokenDto.setCreatedWho(userId);
        refreshTokenRepository.save(refreshTokenDto.toEntity());
    }

    @Transactional(readOnly = true)
    public RefreshTokenDto getByToken(String token) {
        return refreshTokenRepository.findByRefreshToken(token).map(RefreshTokenDto::new).orElse(null);
    }

    @Transactional(readOnly = true)
    public RefreshTokenDto getById(String userId){
        return refreshTokenRepository.findById(userId).map(RefreshTokenDto::new).orElse(null);
    }

    @Transactional
    public void deleteById(String userId) {
        refreshTokenRepository.deleteById(userId);
    }
}
