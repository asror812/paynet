package com.devops.paylite.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PaymentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Payment getPaymentSample1() {
        return Payment.builder().id(1L).account("account1").amount(1L).commissionAmount(1L).totalAmount(1L).build();
    }

    public static Payment getPaymentSample2() {
        return Payment.builder().id(2L).account("account2").amount(2L).commissionAmount(2L).totalAmount(2L).build();
    }

    public static Payment getPaymentRandomSampleGenerator() {
        return Payment.builder()
            .id(longCount.incrementAndGet())
            .account(UUID.randomUUID().toString())
            .amount(longCount.incrementAndGet())
            .commissionAmount(longCount.incrementAndGet())
            .totalAmount(longCount.incrementAndGet())
            .build();
    }
}
