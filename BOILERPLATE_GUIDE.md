# Guia de Referência — Solutions Clinic API

Este documento descreve a estrutura, padrões e fluxos do projeto para servir como referência ao criar ou manter funcionalidades.

---

## 1. Árvore de Diretórios

Estrutura principal do módulo **API** (Spring Boot), sob `src/main/java/com/jettech/api/solutions_clinic/`:

```
com.jettech.api.solutions_clinic/
├── config/                    # Configurações (Jackson, OpenAPI, Flyway, Stripe, R2/S3)
├── exception/                 # Exceções de domínio e ApiError
├── job/                       # Jobs agendados (ex: TrialExpirationJob)
├── model/
│   ├── converter/            # AttributeConverter JPA (ex: JSONB ↔ JsonNode)
│   ├── entity/               # Entidades JPA (User, Tenant, Procedure, Room, etc.)
│   ├── repository/           # Interfaces JpaRepository (acesso a dados)
│   ├── service/              # Serviços de infra (R2Storage, MedicalRecordPdf, FinancialSync)
│   └── usecase/              # Casos de uso (regras de negócio)
│       ├── appointment/
│       ├── exam/
│       ├── financial/
│       ├── medicalrecord/
│       ├── medicalrecordtemplate/
│       ├── patient/
│       ├── procedure/
│       ├── professional/
│       ├── professionalschedule/
│       ├── room/
│       ├── signup/
│       ├── subscription/
│       ├── tenant/
│       └── user/
├── security/                 # TenantContext, filtros e integração com JWT
├── service/                  # Serviços de aplicação (ex: MedicalRecordPdfService)
└── web/                      # Controllers e interfaces de API (UserAPI, ProcedureAPI, etc.)
```

**Resumo por camada:**

| Camada        | Pacote principal      | Responsabilidade                                      |
|---------------|------------------------|--------------------------------------------------------|
| **Web**       | `web/`                 | Controllers que implementam interfaces de API (REST)  |
| **Use cases** | `model/usecase/*/`     | Orquestração, validação de negócio, transações        |
| **Repositórios** | `model/repository/` | Acesso a dados (Spring Data JPA)                      |
| **Entidades** | `model/entity/`        | Modelo de domínio persistido (JPA)                     |
| **Config**    | `config/`              | Beans e configuração da aplicação                     |
| **Exceções**  | `exception/`           | Tipos de erro e tratamento global (ControllerAdvice)  |

---

## 2. Padrões de Design

### 2.1 Dependency Injection (DI)

- **Spring gerencia** todos os beans; não há configuração manual de DI.
- **Injeção por construtor** com `@RequiredArgsConstructor(access = AccessLevel.PACKAGE)` (Lombok).
- Controllers recebem **interfaces de use case**; implementações são `@Service` e injetadas automaticamente.

### 2.2 Use Case (Application Service)

- **Interface por operação**: cada ação tem uma interface (ex: `CreateProcedureUseCase`, `GetProcedureByIdUseCase`, `DeleteProcedureUseCase`).
- **Contrato genérico**: `UseCase<IN, OUT>` para casos com retorno; `UnitUseCase<IN>` para `void`.
- **Implementação**: classe `Default*UseCase` com `@Service`, que usa repositórios, `TenantContext` e demais dependências.

```text
UseCase<IN, OUT>  →  OUT execute(IN in)
UnitUseCase<IN>   →  void execute(IN in)
```

### 2.3 Interface de API + Controller (Port/Adapter)

- **Interface** (ex: `ProcedureAPI`, `UserAPI`): define contratos REST com anotações do Spring (`@GetMapping`, `@PostMapping`, etc.) e **SpringDoc/OpenAPI**.
- **Controller**: classe `*Controller` que **implementa** a interface e delega para os use cases. Não contém lógica de negócio.

### 2.4 Repository (Data Access)

- **Spring Data JPA**: interfaces estendem `JpaRepository<Entity, ID>`.
- **Queries**: métodos de convenção (ex: `findByTenantId`) ou `@Query` (JPQL) para buscas mais complexas.
- Repositórios são **injetados nos use cases**; nunca nos controllers.

### 2.5 DTOs com Records

- **Request**: records com validação Bean Validation (`@NotBlank`, `@NotNull`, `@Min`, etc.), no pacote do use case (ex: `CreateProcedureRequest`, `UpdateProcedureBodyRequest`).
- **Response**: records imutáveis (ex: `ProcedureResponse`, `RoomResponse`) também no pacote do use case.
- Controllers e use cases trabalham com esses tipos; entidades JPA ficam restritas à camada de persistência e use cases.

### 2.6 Multi-tenancy (TenantContext)

- **TenantContext** (`@Component`): lê do JWT o `clinicId` (e opcionalmente o usuário) e expõe `getRequiredClinicId()`, `getUserIdOrNull()`, `requireSameTenant(tenantId)`.
- Use cases que precisam isolar por clínica injetam `TenantContext` e checam tenant antes de ler/escrever.

### 2.7 Tratamento global de exceções

- **GlobalExceptionHandler** (`@ControllerAdvice`): mapeia exceções de domínio (ex: `EntityNotFoundException`, `ForbiddenException`, `DuplicateEntityException`) para respostas HTTP padronizadas (status + corpo com `timestamp`, `status`, `error`, `message`).
- Exceções podem implementar `HasApiError` e usar `ApiError` para mensagens e códigos padronizados.

### 2.8 AttributeConverter (persistência customizada)

- Para tipos que não são nativos no banco (ex: JSONB no PostgreSQL), usa-se `AttributeConverter` (ex: `JsonNodeAttributeConverter`).
- Anotado com `@Converter(autoApply = false)` e referenciado na entidade com `@Convert(converter = ...)` onde necessário.

---

## 3. Fluxo de Dados

Fluxo típico de uma requisição até o banco e volta:

```text
Cliente HTTP
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│  Web (web/)                                                      │
│  • Controller implementa *API                                    │
│  • Valida @Valid no body (Bean Validation)                       │
│  • Monta objeto de request (record) e chama use case             │
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│  Use Case (model/usecase/<contexto>/)                            │
│  • Default*UseCase recebe request (ou ID)                        │
│  • Usa TenantContext para tenant/usuário                        │
│  • Valida regras de negócio e permissões                         │
│  • Chama repository.save(), findById(), etc.                     │
│  • Converte entidade → Response (record) e retorna              │
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│  Repository (model/repository/)                                  │
│  • Interface JpaRepository<Entity, UUID>                         │
│  • Spring Data implementa em runtime                             │
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
  Banco de dados (PostgreSQL, migrações Flyway)
```

**Exceções:** lançadas nos use cases (ou em serviços chamados por eles); o `GlobalExceptionHandler` traduz para resposta HTTP (404, 403, 409, etc.).

---

## 4. Persistência

### 4.1 Entidades

- **Pacote**: `model.entity`.
- **Anotações**: `@Entity`, `@Table` (quando o nome da tabela difere do nome da entidade), `@Id`, `@GeneratedValue(strategy = GenerationType.UUID)`.
- **Campos de auditoria**: `@CreationTimestamp` e `@UpdateTimestamp` (Hibernate) para `createdAt` e `updatedAt`.
- **Relacionamentos**: `@ManyToOne`, `@OneToMany`, etc., com `FetchType.LAZY` quando fizer sentido.
- **Identidade**: `@EqualsAndHashCode(of = "id")` (Lombok) para evitar uso de todos os campos.

Exemplo (trecho):

```java
@Entity(name = "procedures")
public class Procedure {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;
    // ...
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 4.2 Repositórios

- **Pacote**: `model.repository`.
- **Contrato**: `extends JpaRepository<Entity, UUID>`.
- **Métodos**: nomes por convenção (ex: `findByTenantId`, `findByIdAndTenantId`) ou `@Query` com JPQL.
- **Paginação**: métodos que recebem `Pageable` e retornam `Page<Entity>`.

Exemplo:

```java
public interface ProcedureRepository extends JpaRepository<Procedure, UUID> {
    List<Procedure> findByTenantId(UUID tenantId);
    Optional<Procedure> findByIdAndTenantId(UUID id, UUID tenantId);
    Page<Procedure> findByTenantId(UUID tenantId, Pageable pageable);

    @Query("""
        SELECT p FROM procedures p
        WHERE p.tenant.id = :tenantId
        AND (:professionalId IS NULL OR p.professional.id = :professionalId)
        AND (:search IS NULL OR :search = '' OR ...)
        """)
    Page<Procedure> findByTenantIdWithFilters(...);
}
```

### 4.3 Migrações

- **Flyway**: scripts em `src/main/resources/db/migration/` (não listados na árvore acima, mas presentes no projeto).
- Schema e alterações de tabela são versionados via Flyway; JPA/Hibernate não gera o DDL em produção.

---

## 5. Exemplo Prático: CRUD de Procedimento

Segue um CRUD simples seguindo exatamente os padrões do projeto (Procedure já existe; aqui serve como modelo para outro recurso).

### 5.1 Entidade

```java
// model/entity/Procedure.java (resumido)
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity(name = "procedures")
public class Procedure {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;
    // ... outros campos e createdAt/updatedAt
}
```

### 5.2 Repositório

```java
// model/repository/ProcedureRepository.java
public interface ProcedureRepository extends JpaRepository<Procedure, UUID> {
    Optional<Procedure> findByIdAndTenantId(UUID id, UUID tenantId);
    Page<Procedure> findByTenantId(UUID tenantId, Pageable pageable);
}
```

### 5.3 Request/Response (records no pacote do use case)

```java
// model/usecase/procedure/CreateProcedureRequest.java
public record CreateProcedureRequest(
    @NotNull UUID tenantId,
    @NotBlank @Size(min = 2, max = 200) String name,
    String description,
    @NotNull @Min(1) int estimatedDurationMinutes,
    @NotNull @Min(0) BigDecimal basePrice,
    @Min(0) BigDecimal professionalCommissionPercent,
    UUID professionalId
) {}
```

```java
// model/usecase/procedure/ProcedureResponse.java
public record ProcedureResponse(
    UUID id,
    UUID tenantId,
    String name,
    String description,
    int estimatedDurationMinutes,
    BigDecimal basePrice,
    BigDecimal professionalCommissionPercent,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### 5.4 Interfaces de Use Case

```java
// model/usecase/procedure/CreateProcedureUseCase.java
public interface CreateProcedureUseCase extends UseCase<CreateProcedureRequest, ProcedureResponse> {}
```

```java
// model/usecase/procedure/GetProcedureByIdUseCase.java
public interface GetProcedureByIdUseCase extends UseCase<UUID, ProcedureResponse> {}
```

```java
// model/usecase/procedure/UpdateProcedureUseCase.java
public interface UpdateProcedureUseCase extends UseCase<UpdateProcedureRequest, ProcedureResponse> {}
```

```java
// model/usecase/procedure/DeleteProcedureUseCase.java
public interface DeleteProcedureUseCase extends UnitUseCase<UUID> {}
```

### 5.5 Implementação: Create

```java
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateProcedureUseCase implements CreateProcedureUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProcedureResponse execute(CreateProcedureRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        Procedure procedure = new Procedure();
        procedure.setTenant(tenant);
        procedure.setName(request.name());
        procedure.setDescription(request.description());
        procedure.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        procedure.setBasePrice(request.basePrice());
        procedure.setProfessionalCommissionPercent(request.professionalCommissionPercent());
        procedure.setActive(true);

        procedure = procedureRepository.save(procedure);

        return new ProcedureResponse(
                procedure.getId(),
                procedure.getTenant().getId(),
                procedure.getName(),
                procedure.getDescription(),
                procedure.getEstimatedDurationMinutes(),
                procedure.getBasePrice(),
                procedure.getProfessionalCommissionPercent(),
                procedure.isActive(),
                procedure.getCreatedAt(),
                procedure.getUpdatedAt()
        );
    }
}
```

### 5.6 Implementação: Get by ID

```java
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetProcedureByIdUseCase implements GetProcedureByIdUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public ProcedureResponse execute(UUID id) throws AuthenticationFailedException {
        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", id));
        if (!procedure.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        return new ProcedureResponse(
                procedure.getId(),
                procedure.getTenant().getId(),
                procedure.getName(),
                procedure.getDescription(),
                procedure.getEstimatedDurationMinutes(),
                procedure.getBasePrice(),
                procedure.getProfessionalCommissionPercent(),
                procedure.isActive(),
                procedure.getCreatedAt(),
                procedure.getUpdatedAt()
        );
    }
}
```

### 5.7 Implementação: Update

```java
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateProcedureUseCase implements UpdateProcedureUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProcedureResponse execute(UpdateProcedureRequest request) throws AuthenticationFailedException {
        Procedure procedure = procedureRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", request.id()));
        if (!procedure.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        if (request.name() != null && !request.name().trim().isEmpty()) {
            procedure.setName(request.name());
        }
        // ... demais campos
        Procedure saved = procedureRepository.save(procedure);
        return new ProcedureResponse(/* mapear saved */);
    }
}
```

### 5.8 Implementação: Delete

```java
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteProcedureUseCase implements DeleteProcedureUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public void execute(UUID id) throws AuthenticationFailedException {
        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", id));
        if (!procedure.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        procedureRepository.delete(procedure);
    }
}
```

### 5.9 Interface de API

```java
// web/ProcedureAPI.java (sem @RequestMapping na interface; paths nos métodos)
@Tag(name = "Procedimentos", description = "Endpoints para gerenciamento de procedimentos")
public interface ProcedureAPI {

    @PostMapping("/procedures")
    ProcedureResponse createProcedure(@Valid @RequestBody CreateProcedureRequest request) throws AuthenticationFailedException;

    @GetMapping("/procedures/{id}")
    ProcedureResponse getProcedureById(@PathVariable UUID id) throws AuthenticationFailedException;

    @PutMapping("/procedures/{id}")
    ProcedureResponse updateProcedure(@PathVariable UUID id, @Valid @RequestBody UpdateProcedureBodyRequest request) throws AuthenticationFailedException;

    @DeleteMapping("/procedures/{id}")
    void deleteProcedure(@PathVariable UUID id) throws AuthenticationFailedException;
}
```

### 5.10 Controller

```java
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ProcedureController implements ProcedureAPI {

    private final CreateProcedureUseCase createProcedureUseCase;
    private final GetProcedureByIdUseCase getProcedureByIdUseCase;
    private final UpdateProcedureUseCase updateProcedureUseCase;
    private final DeleteProcedureUseCase deleteProcedureUseCase;

    @Override
    public ProcedureResponse createProcedure(@Valid @RequestBody CreateProcedureRequest request) throws AuthenticationFailedException {
        return createProcedureUseCase.execute(request);
    }

    @Override
    public ProcedureResponse getProcedureById(@PathVariable UUID id) throws AuthenticationFailedException {
        return getProcedureByIdUseCase.execute(id);
    }

    @Override
    public ProcedureResponse updateProcedure(@PathVariable UUID id, @Valid @RequestBody UpdateProcedureBodyRequest request) throws AuthenticationFailedException {
        return updateProcedureUseCase.execute(new UpdateProcedureRequest(id, request.name(), request.description(), ...));
    }

    @Override
    public void deleteProcedure(@PathVariable UUID id) throws AuthenticationFailedException {
        deleteProcedureUseCase.execute(id);
    }
}
```

---

## Checklist para novo recurso CRUD

1. **Entidade** em `model/entity/` com JPA e auditoria.
2. **Repositório** em `model/repository/` estendendo `JpaRepository<Entity, UUID>`.
3. **Records** de request/response no pacote `model/usecase/<recurso>/`.
4. **Interfaces** de use case (`Create*`, `Get*ById`, `Update*`, `Delete*`) estendendo `UseCase` ou `UnitUseCase`.
5. **Implementações** `Default*UseCase` com `@Service`, `@Transactional`, `TenantContext` quando for multi-tenant.
6. **Interface de API** em `web/*API` com anotações HTTP e SpringDoc.
7. **Controller** em `web/*Controller` implementando a API e delegando para os use cases.
8. **Migração Flyway** se houver nova tabela ou alteração de schema.

Com isso, o novo recurso fica alinhado ao boilerplate e ao fluxo de dados descrito neste guia.
