# Deploy do Backend no Render

Este guia descreve como configurar e publicar o backend **solutions-clinic** na plataforma [Render](https://render.com).

## Pré-requisitos

- Conta no [Render](https://render.com)
- Repositório Git (GitHub ou GitLab) com o código do projeto
- (Opcional) Banco Postgres no Render ou externo

## Opção 1: Usar o Blueprint (render.yaml)

O repositório já inclui um `render.yaml` na **raiz do repo** que define:

- **Web Service** (Docker) para a API
- **PostgreSQL** (banco gerenciado)

### Passos

1. Acesse [dashboard.render.com](https://dashboard.render.com) e faça login.
2. Conecte seu repositório Git (New → Connect repository).
3. O Render deve detectar o `render.yaml`. Clique em **Apply** ou **Create Blueprint** e selecione o repositório.
4. Após o sync, serão criados:
   - **solutions-clinic-api** (Web Service)
   - **solutions-clinic-db** (PostgreSQL)
5. No serviço **solutions-clinic-api**, vá em **Environment** e configure as variáveis abaixo (principalmente as do banco).
6. **Conecte o banco ao serviço**: no serviço da API, em **Environment**, use **Add from Render** e escolha o banco **solutions-clinic-db**. O Render pode injetar `INTERNAL_DATABASE_URL`. Nesse caso, defina manualmente as variáveis que a aplicação espera (veja “Variáveis de ambiente”) usando os dados do banco.

## Opção 2: Criar o serviço manualmente

1. No Render: **New** → **Web Service**.
2. Conecte o repositório e escolha a branch.
3. Configure:
   - **Root Directory:** `API/solutions-clinic`
   - **Runtime:** **Docker**
   - **Dockerfile Path:** `Dockerfile.backend` (ou `API/solutions-clinic/Dockerfile.backend` se o Root Directory for a raiz do repo)
4. Crie um **PostgreSQL** em **New** → **PostgreSQL** (ou use um banco externo).
5. No Web Service, em **Environment**, adicione todas as variáveis listadas abaixo.

## Variáveis de ambiente

Defina estas variáveis no Render (Environment do Web Service). O Render define **PORT** automaticamente; a aplicação já usa `server.port=${PORT:8080}`.

### Banco de dados (obrigatório)

Se o banco for no Render, use as credenciais da aba **Info** do Postgres (Internal Connection String ou campos separados):

| Variável            | Descrição        | Exemplo (não commitar valores reais)   |
| ------------------- | ---------------- | -------------------------------------- |
| `POSTGRES_HOST`     | Host do Postgres | `dpg-xxx-a.oregon-postgres.render.com` |
| `POSTGRES_PORT`     | Porta            | `5432`                                 |
| `POSTGRES_DB`       | Nome do banco    | `solutions_clinic`                     |
| `POSTGRES_USER`     | Usuário          | `solutions_clinic`                     |
| `POSTGRES_PASSWORD` | Senha            | (valor da Render)                      |

### Segurança (obrigatório em produção)

| Variável                   | Descrição                                      |
| -------------------------- | ---------------------------------------------- |
| `SECURITY_TOKEN_SECRET`    | Chave secreta para tokens (mín. 32 caracteres) |
| `SECURITY_JWT_ISSUER`      | Emissor JWT (ex.: `solutions-clinic`)          |
| `SECURITY_JWT_AUDIENCE`    | Audience JWT (se usar)                         |
| `SECURITY_JWT_JWK_SET_URI` | URI do JWK Set (se usar Auth0/Okta)            |

### Frontend e Stripe (conforme necessidade)

| Variável                      | Descrição                                             |
| ----------------------------- | ----------------------------------------------------- |
| `FRONTEND_URL`                | URL do frontend (ex.: `https://seu-app.onrender.com`) |
| `STRIPE_API_KEY`              | Chave pública Stripe                                  |
| `STRIPE_API_SECRET`           | Chave secreta Stripe                                  |
| `STRIPE_WEBHOOK_SECRET`       | Secret do webhook Stripe                              |
| `STRIPE_WEBHOOK_SUCCESS_PATH` | Path de sucesso (default: `/plan-selection/success`)  |
| `STRIPE_WEBHOOK_CANCEL_PATH`  | Path de cancelamento (default: `/plan-selection`)     |

### Outros

| Variável              | Descrição     | Default |
| --------------------- | ------------- | ------- |
| `TRIAL_DURATION_DAYS` | Dias de trial | `14`    |

## Porta (PORT)

O Render define a variável **PORT** e a aplicação está configurada para usá-la em `application.yaml`:

```yaml
server:
  port: ${PORT:8080}
```

Não é necessário definir **PORT** manualmente no Environment.

## Health check (opcional)

Para deploys com zero downtime, você pode configurar um **Health Check Path** no Render. Se adicionar o Spring Boot Actuator e expor `/actuator/health`, use:

- **Health Check Path:** `/actuator/health`

No `render.yaml` isso pode ser definido com:

```yaml
healthCheckPath: /actuator/health
```

## Build e deploy

- **Build:** o Render usa o `Dockerfile.backend` (multi-stage: Gradle + JRE) dentro do contexto `API/solutions-clinic`.
- **Start:** o container executa `java -jar app.jar` e escuta na porta definida por **PORT**.

As migrações Flyway rodam na subida da aplicação (`validate-on-migrate`, `repair-on-migrate` e `baseline-on-migrate` já configurados).

## Domínio

Após o deploy, a API fica disponível em:

- `https://<nome-do-serviço>.onrender.com`

Você pode configurar um **custom domain** nas configurações do Web Service no Render.

## Resumo rápido

1. Conectar o repo no Render e aplicar o Blueprint **ou** criar Web Service (Docker) + Postgres manualmente.
2. Definir **Root Directory** = `API/solutions-clinic` e **Dockerfile** = `Dockerfile.backend` (ou caminho completo a partir da raiz).
3. Preencher no Environment: `POSTGRES_*`, `SECURITY_TOKEN_SECRET` (e demais segurança), `FRONTEND_URL` e Stripe se for usar.
4. Conectar o banco ao serviço (se usar Postgres do Render) e fazer o primeiro deploy.

Para mais detalhes do Blueprint, veja a [documentação do Render](https://render.com/docs/blueprint-spec).
