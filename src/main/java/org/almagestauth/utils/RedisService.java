package org.almagestauth.utils;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.almagestauth.common.constants.RedisKeyConstants;
import org.almagestauth.exception.r400.AuthFailureException;
import org.almagestauth.exception.r406.AccessDeniedException;
import org.almagestauth.exception.r500.RedisSessionException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;
    private static final int LOCK_DURATION = 10; // 잠금 지속 시간
    private static final int MAX_FAILURE_COUNT = 5;
    private static final String LOCKED_STATUS = "locked";


    /**
     * Redis 키 생성
     */
    private String generateRedisKey(String prefix, String id) {
        return prefix + ":" + id;
    }

    /**
     * 실패 카운터 처리 통합 메서드
     * 1. Redis 조회
     * 2. locked 상태 확인
     * 3. 없으면 카운터 생성 (값=1)
     * 4. 카운터 증가 처리 (1-4 사이)
     * 5. 최대 실패시 잠금 처리 (count=5)
     */
    public void authFailureCountHandler(String id) {
        String key = generateRedisKey(RedisKeyConstants.FAILURE_PREFIX, id);
        int count = 0;

        // 1. Redis 조회
        String value = redisTemplate.opsForValue().get(key);


        // 2. locked 상태 확인
        if (LOCKED_STATUS.equals(value)) {
            log.warn("계정 잠금 상태: id={}", id);
            throw new AccessDeniedException("계정이 잠금 상태입니다. " + LOCK_DURATION + "분 후에 다시 시도해주세요.");
        }

        // 3. 값이 없으면 카운터 생성 (값=1)
        if (value == null) {
            count = 1;
            redisTemplate.opsForValue().set(key, String.valueOf(count), LOCK_DURATION, TimeUnit.MINUTES);
            log.debug("실패 카운터 생성: id={}, count=1", id);
            throw new AuthFailureException("인증 정보 불일치. Count : " + count, count);
        }

        // 4. 현재 카운트 확인 및 증가
        count = Integer.parseInt(value);
        count++;

        // 기존 TTL 유지
        Long remainingTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        long ttl = (remainingTtl != null && remainingTtl > 0) ? remainingTtl : LOCK_DURATION * 60L;

        // 5. 최대 실패 횟수 도달시 잠금 처리
        if (count >= MAX_FAILURE_COUNT) {
            redisTemplate.opsForValue().set(key, LOCKED_STATUS, LOCK_DURATION, TimeUnit.MINUTES);
            log.warn("계정 잠금 처리: id={}", id);
            throw new AccessDeniedException("인증 시도 횟수를 초과했습니다. " + LOCK_DURATION + "분 후에 다시 시도해주세요.");
        }

        // 증가된 카운트 저장 (기존 TTL 유지)
        redisTemplate.opsForValue().set(key, String.valueOf(count), ttl, TimeUnit.SECONDS);
        log.debug("실패 카운트 증가: id={}, count={}, remainingTtl={}s", id, count, ttl);
        throw new AuthFailureException("인증 정보 불일치. Count : " + count, count);
    }

    /**
     * 상태 삭제 (인증 성공, 로그아웃, 탈퇴)
     */
    public void resetStatus(String id) {
        String failKey = generateRedisKey(RedisKeyConstants.FAILURE_PREFIX, id);
        String refreshKey = generateRedisKey(RedisKeyConstants.REFRESH_TOKEN_PREFIX, id);
        try {
            redisTemplate.delete(failKey);
            redisTemplate.delete(refreshKey);
            log.debug("실패 카운트 초기화: id={}", id);
        } catch (Exception e) {
            throw new RedisSessionException("사용자 정보 처리 중 오류 발생");
        }
    }

    /**
     * 리프레시 토큰 검증 문자열 저장
     */
    public void setRefreshTokenVerification(String id, String verifyString, long expiration) {
        String key = generateRedisKey(RedisKeyConstants.REFRESH_TOKEN_PREFIX, id);
        try {
            redisTemplate.opsForValue().set(key, verifyString, expiration, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RedisSessionException("토큰 정보 저장 중 오류 발생");
        }
    }

    /**
     * 리프레시 토큰 검증 문자열 조회
     */
    public String getRefreshTokenVerification(String id) {
        String key = generateRedisKey(RedisKeyConstants.REFRESH_TOKEN_PREFIX, id);
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new RedisSessionException("토큰 정보 조회 중 오류 발생");
        }
    }
}
