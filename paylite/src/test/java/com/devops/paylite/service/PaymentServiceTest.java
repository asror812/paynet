package com.devops.paylite.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devops.paylite.config.PaymentProperties;
import com.devops.paylite.domain.Agent;
import com.devops.paylite.domain.Payment;
import com.devops.paylite.domain.enumeration.PaymentStatus;
import com.devops.paylite.repository.AgentRepository;
import com.devops.paylite.repository.PaymentRepository;
import com.devops.paylite.service.dto.PaymentCreateRequestDto;
import com.devops.paylite.service.dto.PaymentDTO;
import com.devops.paylite.service.mapper.PaymentMapper;
import com.devops.paylite.service.mapper.PaymentMapperImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProperties paymentProperties;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        PaymentMapper paymentMapper = new PaymentMapperImpl();
        paymentService = new PaymentService(paymentRepository, agentRepository, paymentMapper, paymentProperties);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("agent1", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCalculateCommissionAndDeductTotalAmount() {
        Agent agent = Agent.builder().login("agent1").balance(100L).build();

        when(agentRepository.findByLoginForUpdate("agent1")).thenReturn(Optional.of(agent));
        when(paymentProperties.getCommissionPercent()).thenReturn(BigDecimal.TEN);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentDTO result = paymentService.create(new PaymentCreateRequestDto("8600123412341234", 50L));

        assertEquals(PaymentStatus.PAID, result.getStatus());
        assertEquals("8600123412341234", result.getAccount());
        assertEquals(50L, result.getAmount());
        assertEquals(5L, result.getCommissionAmount());
        assertEquals(55L, result.getTotalAmount());
        assertEquals(45L, agent.getBalance());

        verify(agentRepository).save(agent);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldCreateFailedPaymentWithoutChangingBalanceWhenBalanceIsInsufficient() {
        Agent agent = Agent.builder().login("agent1").balance(54L).build();
        when(agentRepository.findByLoginForUpdate("agent1")).thenReturn(Optional.of(agent));
        when(paymentProperties.getCommissionPercent()).thenReturn(BigDecimal.TEN);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentDTO result = paymentService.create(new PaymentCreateRequestDto("8600123412341234", 50L));

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        assertEquals(55L, result.getTotalAmount());
        assertEquals(54L, agent.getBalance());

        verify(agentRepository, never()).save(any(Agent.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldRejectPaymentWhenTotalAmountOverflows() {
        Agent agent = Agent.builder().login("agent1").balance(Long.MAX_VALUE).build();
        when(agentRepository.findByLoginForUpdate("agent1")).thenReturn(Optional.of(agent));
        when(paymentProperties.getCommissionPercent()).thenReturn(BigDecimal.TEN);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            paymentService.create(new PaymentCreateRequestDto("8600123412341234", Long.MAX_VALUE))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
