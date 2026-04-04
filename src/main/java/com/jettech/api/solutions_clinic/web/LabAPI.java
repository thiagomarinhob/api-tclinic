package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.laboratory.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface LabAPI {

    // =====================================================
    // Exam Types (Catalog)
    // =====================================================

    @PostMapping("/lab/exam-types")
    @ResponseStatus(HttpStatus.CREATED)
    LabExamTypeResponse createExamType(@Valid @RequestBody CreateLabExamTypeRequest request) throws AuthenticationFailedException;

    @GetMapping("/lab/exam-types")
    Page<LabExamTypeResponse> getExamTypes(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean active
    ) throws AuthenticationFailedException;

    @PutMapping("/lab/exam-types/{id}")
    LabExamTypeResponse updateExamType(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateLabExamTypeBodyRequest request
    ) throws AuthenticationFailedException;

    @DeleteMapping("/lab/exam-types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteExamType(@PathVariable UUID id) throws AuthenticationFailedException;

    // =====================================================
    // Lab Orders
    // =====================================================

    @PostMapping("/lab/orders")
    @ResponseStatus(HttpStatus.CREATED)
    LabOrderResponse createOrder(@Valid @RequestBody CreateLabOrderBodyRequest request) throws AuthenticationFailedException;

    @GetMapping("/lab/orders")
    Page<LabOrderResponse> getOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) UUID patientId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String search
    ) throws AuthenticationFailedException;

    @GetMapping("/lab/orders/{id}")
    LabOrderResponse getOrderById(@PathVariable UUID id) throws AuthenticationFailedException;

    @PatchMapping("/lab/orders/{id}/status")
    LabOrderResponse updateOrderStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateLabOrderStatusBodyRequest request
    ) throws AuthenticationFailedException;

    @GetMapping("/lab/patients/{patientId}/orders")
    List<LabOrderResponse> getOrdersByPatient(@PathVariable UUID patientId) throws AuthenticationFailedException;

    // =====================================================
    // Results
    // =====================================================

    @PostMapping("/lab/order-items/{itemId}/result")
    LabOrderItemResponse enterResult(
        @PathVariable UUID itemId,
        @Valid @RequestBody EnterLabResultBodyRequest request
    ) throws AuthenticationFailedException;

    @PatchMapping("/lab/order-items/{itemId}/validate")
    LabOrderItemResponse validateResult(
        @PathVariable UUID itemId,
        @Valid @RequestBody ValidateLabResultBodyRequest request
    ) throws AuthenticationFailedException;

    // =====================================================
    // Dashboard
    // =====================================================

    @GetMapping("/lab/dashboard")
    LabDashboardResponse getDashboard() throws AuthenticationFailedException;
}
