package com.devops.paylite.service.mapper;

import static com.devops.paylite.domain.AgentAsserts.*;
import static com.devops.paylite.domain.AgentTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentMapperTest {

    private AgentMapper agentMapper;

    @BeforeEach
    void setUp() {
        agentMapper = new AgentMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAgentSample1();
        var actual = agentMapper.toEntity(agentMapper.toDto(expected));
        assertAgentAllPropertiesEquals(expected, actual);
    }
}
