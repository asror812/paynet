package com.devops.paylite.web.rest;

import com.devops.paylite.repository.AgentRepository;
import com.devops.paylite.service.AgentService;
import com.devops.paylite.service.dto.AgentDTO;
import com.devops.paylite.service.dto.BalanceResponseDto;
import com.devops.paylite.service.dto.TopUpRequestDto;
import com.devops.paylite.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.devops.paylite.domain.Agent}.
 */
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentResource {

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private static final Logger LOG = LoggerFactory.getLogger(AgentResource.class);
    private static final String ENTITY_NAME = "agent";
    private final AgentService agentService;

    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    @GetMapping("/me/balance")
    public ResponseEntity<BalanceResponseDto> getOwnBalance() {
        LOG.debug("REST request to get Own Balance");

        BalanceResponseDto dto = agentService.getOwnBalance();
        return ResponseEntity.ok().body(dto);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/topup")
    public ResponseEntity<Void> topUp(@PathVariable Long id, @Valid @RequestBody TopUpRequestDto dto) {
        LOG.debug("REST request to top up Payment");

        agentService.topUp(id, dto);

        return ResponseEntity.ok().build();
    }
}
