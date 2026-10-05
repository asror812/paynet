package com.devops.paylite.domain;

import static com.devops.paylite.domain.AgentTestSamples.*;
import static com.devops.paylite.domain.PaymentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.devops.paylite.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Payment.class);
        Payment payment1 = getPaymentSample1();
        Payment payment2 = new Payment();
        assertThat(payment1).isNotEqualTo(payment2);

        payment2.setId(payment1.getId());
        assertThat(payment1).isEqualTo(payment2);

        payment2 = getPaymentSample2();
        assertThat(payment1).isNotEqualTo(payment2);
    }

    @Test
    void agentTest() {
        Payment payment = getPaymentRandomSampleGenerator();
        Agent agentBack = getAgentRandomSampleGenerator();

        payment.setAgent(agentBack);
        assertThat(payment.getAgent()).isEqualTo(agentBack);

        payment.setAgent(null);
        assertThat(payment.getAgent()).isNull();
    }
}
