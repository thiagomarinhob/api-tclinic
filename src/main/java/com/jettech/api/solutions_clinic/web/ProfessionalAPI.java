package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.professional.AddProfessionalToClinicBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.CreateProfessionalRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.ProfessionalResponse;
import com.jettech.api.solutions_clinic.model.usecase.professional.ProfessionalTenantResponse;
import com.jettech.api.solutions_clinic.model.usecase.professional.UpdateProfessionalActiveBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.UpdateProfessionalBodyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.jettech.api.solutions_clinic.model.entity.Specialty;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.List;
import java.util.UUID;

@Tag(name = "Profissionais", description = "Endpoints para gerenciamento de profissionais")
public interface ProfessionalAPI {

    @GetMapping("/specialties")
    @Operation(summary = "Lista especialidades disponíveis", description = "Retorna a lista de todas as especialidades médicas disponíveis no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de especialidades retornada com sucesso")
    })
    List<Specialty> getSpecialties();

    @PostMapping("/professionals")
    @Operation(summary = "Cria um novo profissional", description = "Registra um novo profissional no sistema associado a um usuário e uma clínica. A role SPECIALIST é criada automaticamente para o usuário no tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profissional criado com sucesso", 
                    content = @Content(schema = @Schema(implementation = ProfessionalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário ou clínica não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Profissional já existe para este usuário e clínica", content = @Content)
    })
    ProfessionalResponse createProfessional(@Valid @RequestBody CreateProfessionalRequest request) throws AuthenticationFailedException;

    @PostMapping("/clinics/{clinicId}/professionals")
    @Operation(summary = "Adiciona profissional a uma clínica", description = "Registra um profissional associando-o a uma clínica específica. A role SPECIALIST é criada automaticamente para o usuário no tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profissional adicionado à clínica com sucesso", 
                    content = @Content(schema = @Schema(implementation = ProfessionalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário ou clínica não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Profissional já existe para este usuário e clínica", content = @Content)
    })
    ProfessionalResponse addProfessionalToClinic(
            @PathVariable UUID clinicId,
            @Valid @RequestBody AddProfessionalToClinicBodyRequest request
    ) throws AuthenticationFailedException;

    @GetMapping("/professionals/user/{userId}")
    @Operation(summary = "Busca profissional pelo userId", description = "Retorna o profissional vinculado ao userId informado dentro da clínica autenticada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profissional encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProfessionalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado para este usuário na clínica", content = @Content)
    })
    ProfessionalResponse getProfessionalByUserId(@PathVariable UUID userId) throws AuthenticationFailedException;

    @GetMapping("/professionals/{userId}/tenants")
    @Operation(summary = "Lista tenants vinculados a um profissional", description = "Retorna todos os tenants (clínicas) onde o usuário atua como profissional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tenants retornada com sucesso", 
                    content = @Content(schema = @Schema(implementation = ProfessionalTenantResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    ProfessionalTenantResponse getProfessionalTenants(@PathVariable UUID userId) throws AuthenticationFailedException;

    @GetMapping("/clinics/{clinicId}/professionals")
    @Operation(
        summary = "Lista profissionais de uma clínica com paginação e filtros",
        description = "Retorna uma lista paginada de profissionais de uma clínica específica com suporte a busca textual, filtros e ordenação."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de profissionais retornada com sucesso",
                content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Clínica não encontrada",
                content = @Content
            )
    })
    Page<ProfessionalResponse> getProfessionalsByClinic(
            @PathVariable UUID clinicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "user.fullName,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String documentType
    ) throws AuthenticationFailedException;

    @PutMapping("/professionals/{id}")
    @Operation(summary = "Atualiza dados de um profissional", description = "Atualiza especialidade, documento e bio de um profissional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profissional atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProfessionalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content)
    })
    ProfessionalResponse updateProfessional(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfessionalBodyRequest request
    ) throws AuthenticationFailedException;

    @PatchMapping("/professionals/{id}/active")
    @Operation(summary = "Atualiza o status ativo de um profissional", description = "Ativa ou desativa um profissional no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do profissional atualizado com sucesso", 
                    content = @Content(schema = @Schema(implementation = ProfessionalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado", content = @Content)
    })
    ProfessionalResponse updateProfessionalActive(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfessionalActiveBodyRequest request
    ) throws AuthenticationFailedException;

    @PostMapping("/professionals/with-user")
    @Operation(summary = "Cria um novo profissional com usuário", description = "Cria um novo usuário e associa-o como profissional à clínica autenticada. A role SPECIALIST é criada automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profissional criado com sucesso", 
                    content = @Content(schema = @Schema(implementation = ProfessionalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Clínica não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email ou CPF já cadastrado", content = @Content)
    })
    ProfessionalResponse createProfessionalWithUser(@Valid @RequestBody com.jettech.api.solutions_clinic.model.usecase.professional.CreateProfessionalWithUserRequest request) throws AuthenticationFailedException;
}

