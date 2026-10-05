package com.devops.paylite.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AgentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Agent getAgentSample1() {
        return Agent.builder().id(1L).login("login1").balance(1L).build();
    }

    public static Agent getAgentSample2() {
        return Agent.builder().id(2L).login("login2").balance(2L).build();
    }

    public static Agent getAgentRandomSampleGenerator() {
        return Agent.builder()
            .id(longCount.incrementAndGet())
            .login(UUID.randomUUID().toString())
            .balance(longCount.incrementAndGet())
            .build();
    }
}
