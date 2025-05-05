package org.almagestauth.common.constants;

public class RedisKeyConstants { // Redis Key 상수

    //비밀번호,OTP 입력 실패 카운트 - 1~5, locked
    public static final String FAILURE_PREFIX = "auth:failure:";

    //리프레시 토큰 검증
    public static final String REFRESH_TOKEN_PREFIX = "token:refresh:";

    private RedisKeyConstants() {}
}
