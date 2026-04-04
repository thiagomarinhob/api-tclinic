package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.usecase.user.CreateUserRequest;
import com.jettech.api.solutions_clinic.model.usecase.user.UpdateUserBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.user.UpdateUserBlockedBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.user.UserDetailResponse;
import com.jettech.api.solutions_clinic.model.usecase.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public interface UserAPI {

    @PostMapping
    @Operation(summary = "Cria um novo usuário", description = "Registra um novo usuário no sistema. Se tenantId for fornecido, o usuário será automaticamente associado ao tenant com a role RECEPTION.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "409", description = "Email já existente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Clínica não encontrada (quando tenantId é fornecido)", content = @Content)
    })
    User createUser(@Valid @RequestBody CreateUserRequest user) throws AuthenticationFailedException;

    @GetMapping
    @Operation(
        summary = "Lista usuários de uma clínica com paginação e filtros",
        description = "Retorna uma lista paginada de usuários de uma clínica (tenant) com suporte a busca textual e filtros."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de usuários retornada com sucesso",
                content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Clínica não encontrada",
                content = @Content
            )
    })
    Page<UserResponse> getUsersByTenant(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "firstName,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean blocked,
            @RequestParam(required = false) com.jettech.api.solutions_clinic.model.entity.Role role
    ) throws AuthenticationFailedException;

    @GetMapping("/{id}")
    @Operation(
        summary = "Busca usuário por ID",
        description = "Retorna os dados do usuário incluindo seus tenants e roles associados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Usuário encontrado com sucesso",
                content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Usuário não encontrado",
                content = @Content
            )
    })
    UserDetailResponse getUserById(@PathVariable UUID id) throws AuthenticationFailedException;

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um usuário", description = "Atualiza os dados de um usuário existente.")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Usuário atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email já está em uso", content = @Content)
    })
    UserResponse updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserBodyRequest request
    ) throws AuthenticationFailedException;

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta um usuário", description = "Remove um usuário do sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    void deleteUser(@PathVariable UUID id) throws AuthenticationFailedException;

    @PatchMapping("/{id}/blocked")
    @Operation(summary = "Bloqueia ou desbloqueia um usuário", description = "Atualiza o status de bloqueio de um usuário no sistema.")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Status de bloqueio do usuário atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    UserResponse updateUserBlocked(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserBlockedBodyRequest request
    ) throws AuthenticationFailedException;

    @GetMapping("/check-cpf/{cpf}")
    @Operation(summary = "Verifica se um CPF já está cadastrado", description = "Verifica se um CPF já está cadastrado no sistema.")
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Verificação realizada com sucesso",
                content = @Content
            )
    })
    Map<String, Boolean> checkCpfExists(@PathVariable String cpf) throws AuthenticationFailedException;
}

