package com.jettech.api.solutions_clinic.model.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantTest {

    @Test
    void whenTenantIsCreated_thenConfirmationWindowMinutesDefaultsTo120() {
        Tenant tenant = new Tenant();

        assertThat(tenant.getConfirmationWindowMinutes()).isEqualTo(120);
    }
}
