package com.jettech.api.solutions_clinic.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Painel de Chamadas")
@RequestMapping("/v1")
public interface PainelAPI {

    @GetMapping("/painel/hoje")
    @Operation(summary = "Últimas chamadas do dia para exibição no painel")
    ResponseEntity<List<ChamadaPainelResponse>> chamadasHoje();

    @PostMapping("/painel/chamar")
    @Operation(summary = "Chamar paciente no painel")
    ResponseEntity<ChamadaPainelResponse> chamar(
            @RequestParam UUID appointmentId,
            @RequestParam UUID roomId
    );
}
