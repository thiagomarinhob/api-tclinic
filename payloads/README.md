# Payloads de Teste

Esta pasta contém exemplos de payloads JSON para testar os endpoints de cadastro.

## Como usar

### Com cURL

#### Teste 1: Cadastro de Clínica (completo)
```bash
curl -X POST http://localhost:8080/v1/auth/signup/clinic-owner \
  -H "Content-Type: application/json" \
  -d @clinic-owner-exemplo1.json
```

#### Teste 2: Cadastro de Clínica (mínimo)
```bash
curl -X POST http://localhost:8080/v1/auth/signup/clinic-owner \
  -H "Content-Type: application/json" \
  -d @clinic-owner-exemplo2.json
```

#### Teste 3: Cadastro de Profissional Solo (completo)
```bash
curl -X POST http://localhost:8080/v1/auth/signup/solo \
  -H "Content-Type: application/json" \
  -d @solo-exemplo1.json
```

#### Teste 4: Cadastro de Profissional Solo (mínimo)
```bash
curl -X POST http://localhost:8080/v1/auth/signup/solo \
  -H "Content-Type: application/json" \
  -d @solo-exemplo2.json
```

#### Teste 5: Cadastro de Paciente (completo)
```bash
curl -X POST http://localhost:8080/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d @patient-exemplo1.json
```

#### Teste 6: Cadastro de Paciente (mínimo)
```bash
curl -X POST http://localhost:8080/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d @patient-exemplo2.json
```

### Com Postman/Insomnia

1. Importe os arquivos JSON como body da requisição
2. Configure o método como `POST`
3. Configure a URL:
   - Para clínica: `http://localhost:8080/v1/auth/signup/clinic-owner`
   - Para solo: `http://localhost:8080/v1/auth/signup/solo`
   - Para paciente: `http://localhost:8080/v1/patients`
4. Configure os headers:
   - `Content-Type: application/json`
   - `Authorization: Bearer SEU_TOKEN_AQUI` (obrigatório para pacientes)
5. Cole o conteúdo do arquivo JSON no body

### Com HTTPie

```bash
# Clínica
http POST localhost:8080/v1/auth/signup/clinic-owner < clinic-owner-exemplo1.json

# Solo
http POST localhost:8080/v1/auth/signup/solo < solo-exemplo1.json

# Paciente (completo)
http POST localhost:8080/v1/patients Authorization:"Bearer SEU_TOKEN_AQUI" < patient-exemplo1.json

# Paciente (mínimo)
http POST localhost:8080/v1/patients Authorization:"Bearer SEU_TOKEN_AQUI" < patient-exemplo2.json
```

## Importante

⚠️ **Altere os valores de email, CNPJ, CPF e subdomain antes de testar**, pois eles devem ser únicos no banco de dados.

Se você tentar usar os mesmos valores duas vezes, receberá um erro de validação.

### Campos do Payload de Paciente

**Campos obrigatórios:**
- `tenantId`: UUID da clínica (obrigatório)
- `firstName`: Nome completo do paciente (obrigatório, 2-100 caracteres)

**Campos opcionais:**
- `cpf`: CPF com exatamente 11 dígitos (sem formatação)
- `birthDate`: Data de nascimento no formato `DD/MM/YYYY`
- `gender`: `MASCULINO`, `FEMININO`, `OUTRO`, `NAO_INFORMADO`
- `email`: Email válido
- `phone`: Telefone
- `whatsapp`: WhatsApp
- `addressStreet`, `addressNumber`, `addressComplement`, `addressNeighborhood`, `addressCity`, `addressState`, `addressZipcode`: Dados de endereço
- `bloodType`: `A_POSITIVE`, `A_NEGATIVE`, `B_POSITIVE`, `B_NEGATIVE`, `AB_POSITIVE`, `AB_NEGATIVE`, `O_POSITIVE`, `O_NEGATIVE`
- `allergies`: Alergias do paciente
- `guardianName`, `guardianPhone`, `guardianRelationship`: Dados do responsável (útil para menores de idade)

⚠️ **Para cadastro de pacientes, é necessário estar autenticado** (token JWT no header Authorization).
