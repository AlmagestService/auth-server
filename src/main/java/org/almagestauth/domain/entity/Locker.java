package org.almagestauth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Locker {
    private String account;
    private String count;
}
