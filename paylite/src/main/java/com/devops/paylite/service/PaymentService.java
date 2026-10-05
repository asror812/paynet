package com.devops.paylite.service;

import com.devops.paylite.config.PaymentProperties;
import com.devops.paylite.domain.Agent;
import com.devops.paylite.domain.Payment;
import com.devops.paylite.domain.enumeration.PaymentStatus;
import com.devops.paylite.repository.AgentRepository;
import com.devops.paylite.repository.PaymentRepository;
import com.devops.paylite.security.SecurityUtils;
import com.devops.paylite.service.dto.PaymentCreateRequestDto;
import com.devops.paylite.service.dto.PaymentDTO;
import com.devops.paylite.service.mapper.PaymentMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final AgentRepository agentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentProperties paymentProperties;

    /**
     * Save a payment.
     *
     * @param dto the entity to create.
     * @return the persisted entity.
     */
    @Transactional
    public PaymentDTO create(PaymentCreateRequestDto dto) {
        LOG.debug("Request to create Payment : {}", dto);

        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user login not found"));

        Agent agent = agentRepository
            .findByLoginForUpdate(login)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found"));

        long commissionAmount = calculateCommission(dto.getAmount());
        long totalAmount = calculateTotalAmount(dto.getAmount(), commissionAmount);

        PaymentStatus status = agent.getBalance() >= totalAmount ? PaymentStatus.PAID : PaymentStatus.FAILED;

        Payment payment = Payment.builder()
            .account(dto.getAccount())
            .amount(dto.getAmount())
            .totalAmount(totalAmount)
            .commissionAmount(commissionAmount)
            .status(status)
            .agent(agent)
            .createdDate(Instant.now())
            .build();

        if (status == PaymentStatus.PAID) {
            agent.setBalance(agent.getBalance() - totalAmount);
            agentRepository.save(agent);
        } else {
            LOG.info("Insufficient balance for agent: {}", agent.getLogin());
        }

        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    public Page<PaymentDTO> getOwn(Pageable pageable) {
        LOG.debug("Request to get all Own Payments");

        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user login not found"));

        return paymentRepository.findAllByAgent_Login(login, pageable).map(paymentMapper::toDto);
    }

    private long calculateCommission(long amount) {
        try {
            return BigDecimal.valueOf(amount)
                .multiply(paymentProperties.getCommissionPercent())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValueExact();
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount is too large", exception);
        }
    }

    private long calculateTotalAmount(long amount, long commissionAmount) {
        try {
            return Math.addExact(amount, commissionAmount);
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount is too large", exception);
        }
    }
}
