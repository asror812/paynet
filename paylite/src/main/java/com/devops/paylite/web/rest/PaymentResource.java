package com.devops.paylite.web.rest;

import com.devops.paylite.domain.enumeration.PaymentStatus;
import com.devops.paylite.service.PaymentService;
import com.devops.paylite.service.dto.PaymentCreateRequestDto;
import com.devops.paylite.service.dto.PaymentDTO;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentResource {

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private static final Logger LOG = LoggerFactory.getLogger(PaymentResource.class);
    private static final String ENTITY_NAME = "payment";
    private final PaymentService paymentService;

    /**
     * {@code POST  /payments} : Create a new payment.
     *
     * @param createDto the PaymentCreateRequestDto to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new paymentDTO, or with status {@code 400 (Bad Request)} if the payment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody PaymentCreateRequestDto createDto) throws URISyntaxException {
        LOG.debug("REST request to create Payment : {}", createDto);

        PaymentDTO paymentDTO = paymentService.create(createDto);

        if (paymentDTO.getStatus() == PaymentStatus.FAILED) {
            return ResponseEntity.status(HttpStatusCode.valueOf(422)).body(paymentDTO);
        }

        return ResponseEntity.created(new URI("/api/payments/" + paymentDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, paymentDTO.getId().toString()))
            .body(paymentDTO);
    }

    /**
     * {@code GET  /payments} : get all the payments.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of payments in body.
     */
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    @GetMapping
    public ResponseEntity<Page<PaymentDTO>> getAllPayments(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Own Payments");

        Page<PaymentDTO> page = paymentService.getOwn(pageable);
        return ResponseEntity.ok().body(page);
    }
}
