package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.room.CreateRoomRequest;
import com.jettech.api.solutions_clinic.model.usecase.room.RoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.List;
import java.util.UUID;

@Tag(name = "Salas", description = "Endpoints para gerenciamento de salas")
public interface RoomAPI {

    @PostMapping("/rooms")
    @Operation(summary = "Cria uma nova sala", description = "Registra uma nova sala no sistema associada a uma clínica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala criada com sucesso",
                    content = @Content(schema = @Schema(implementation = RoomResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Clínica não encontrada", content = @Content)
    })
    RoomResponse createRoom(@Valid @RequestBody CreateRoomRequest request) throws AuthenticationFailedException;

    @GetMapping("/rooms/{id}")
    @Operation(
        summary = "Busca sala por ID",
        description = "Retorna os dados da sala incluindo todas as informações cadastradas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Sala encontrada com sucesso",
                content = @Content(schema = @Schema(implementation = RoomResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Sala não encontrada",
                content = @Content
            )
    })
    RoomResponse getRoomById(@PathVariable UUID id) throws AuthenticationFailedException;

    @GetMapping("/rooms")
    @Operation(
        summary = "Lista salas de uma clínica com paginação",
        description = "Retorna uma lista paginada de salas de uma clínica (tenant), com opção de filtrar apenas salas ativas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de salas retornada com sucesso",
                content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Clínica não encontrada",
                content = @Content
            )
    })
    Page<RoomResponse> getRoomsByTenantPaginated(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "name,asc") String sort
    ) throws AuthenticationFailedException;

    @GetMapping("/rooms/all")
    @Operation(
        summary = "Lista todas as salas de uma clínica (sem paginação)",
        description = "Retorna todas as salas de uma clínica. Use para dropdowns e seletores."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de salas retornada com sucesso",
                content = @Content(schema = @Schema(implementation = RoomResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Clínica não encontrada",
                content = @Content
            )
    })
    List<RoomResponse> getRoomsByTenant(
            @RequestParam UUID tenantId,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly
    ) throws AuthenticationFailedException;
}

