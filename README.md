# Incident Management API – Checkpoint 6

API ReST desenvolvida em **Java 17 + Spring Boot 3.5** para cadastrar e gerenciar **ocorrências escolares**. É o lado da publicação (provedor) dos serviços do Projeto Integrador, na disciplina **Arquitetura SOA e Web Services** (Prof. Carlos Eduardo Machado de Oliveira).

## Integrantes

| Nome | RM |
|------|----|
| João Victor Oliveira dos Santos | RM557948 |
| Matheus Alcântara Estevão | RM558193 |
| Nicolle Pellegrino Jelinski | RM558610 |
| Pedro Pereira dos Santos | RM552047 |
| Eric Segawa Montagner | RM558224 |

## Tecnologias

- Java 17, Spring Boot 3.5 (Web, Data JPA, Validation, Security)
- Autenticação **JWT** (stateless), senhas com BCrypt
- Banco **H2** em arquivo (`./data`), criado automaticamente
- Documentação **OpenAPI / Swagger UI** (springdoc)
- Testes de integração com JUnit 5 + MockMvc

## Como executar

Pré-requisitos: JDK 17+ e Maven 3.9+.

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8090`:

| Recurso | URL |
|---------|-----|
| Frontend (telas do projeto) | http://localhost:8090/index.html |
| Swagger UI | http://localhost:8090/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8090/v3/api-docs |
| Console H2 | http://localhost:8090/h2-console (JDBC URL `jdbc:h2:file:./data/incidentdb`, usuário `sa`, sem senha) |

Para rodar os testes:

```bash
mvn test
```

### Usuários de exemplo

Na primeira execução o banco é populado com usuários, categorias, tipos, turmas, alunos e duas ocorrências. A senha de todos é `123456`.

| Login | Perfil |
|-------|--------|
| `professor` / `professor2` | PROFESSOR |
| `analista` | ANALISTA |
| `coordenador` | COORDENADOR |
| `admin` | ADMINISTRATIVO |

Para recomeçar do zero, pare a aplicação e apague a pasta `data/`.

## Arquitetura

```
controller  → recebe as requisições HTTP e devolve DTOs (records)
service     → regras de negócio e transações
repository  → Spring Data JPA (inclui Specifications para filtros dinâmicos)
model       → entidades JPA do MER
dto         → contratos de entrada e saída da API
security    → filtro JWT, emissão de tokens e regras de acesso por perfil
exception   → tratamento global de erros (400, 401, 403, 404, 409)
```

### Modelo de dados (MER)

| Entidade | Tabela | Observações |
|----------|--------|-------------|
| Usuário + Perfil | `usuario` | perfil: PROFESSOR, ANALISTA, COORDENADOR, ADMINISTRATIVO |
| Aluno | `aluno` | foto armazenada no banco (`foto`) |
| Turma | `turma` | turno (MANHA/TARDE/NOITE), ano e semestre (PRIMEIRO/SEGUNDO) |
| Aluno ↔ Turma (N:M) | `aluno_turma` | um aluno pode estar em várias turmas |
| Categoria da ocorrência | `categoria_ocorrencia` | 1:N com tipo |
| Tipo de ocorrência | `tipo_ocorrencia` | pertence a uma categoria |
| Ocorrência | `ocorrencia` | turma, aluno, usuário (professor), data/hora, categoria, tipo, descrição, status |

### Regras de negócio

- Na ocorrência, **o aluno precisa pertencer à turma selecionada**. A turma não pode estar cancelada e o aluno precisa estar ativo.
- O **tipo de ocorrência precisa pertencer à categoria** escolhida.
- O professor **só altera/exclui as próprias ocorrências**, mas consulta todas.
- Exclusões são **lógicas**: aluno e usuário ficam inativos, a turma fica cancelada e a ocorrência fica `deleted`. Assim, o histórico é preservado.
- Categoria com tipos vinculados e tipo já usado em ocorrências não podem ser excluídos.

### Permissões por perfil

| Recurso | PROFESSOR | ANALISTA | COORDENADOR | ADMINISTRATIVO |
|---------|:---------:|:--------:|:-----------:|:--------------:|
| Registrar / alterar / excluir ocorrência (própria) | ✅ | | | |
| Consultar ocorrências | ✅ | ✅ | ✅ | ✅ |
| Alterar status da ocorrência | | ✅ | ✅ | |
| Dashboard (`/incidents/summary`) | | ✅ | ✅ | |
| Cadastrar alunos, turmas, vínculos, categorias e tipos | | | | ✅ |
| Gerenciar usuários | | | | ✅ |
| Consultar alunos, turmas, categorias e tipos | ✅ | ✅ | ✅ | ✅ |
| Alterar a própria senha | ✅ | ✅ | ✅ | ✅ |

## Endpoints

Todas as rotas, exceto `POST /login`, exigem o header `Authorization: Bearer <tokenJWT>`. As listagens são paginadas: `?page=0&size=10&sort=campo,asc`.

### Autenticação
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/login` | `{ "login", "password" }` → `{ tokenJWT, id, login, name, role }` |

### Usuários
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/users` | Cadastra `{ name, login, password, role }` |
| GET | `/users` | Lista usuários ativos |
| GET | `/users/{id}` | Consulta usuário |
| GET | `/users/roles` | Lista os perfis |
| PUT | `/users` | Atualiza `{ id, name, login }` |
| PATCH | `/users` | Altera o perfil `{ id, role }` |
| PATCH | `/users/password` | Altera a própria senha `{ id, oldPassword, newPassword }` |
| DELETE | `/users/{id}` | Inativa usuário |

### Alunos
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/students` | Cadastra `{ name, birthDate: "dd/MM/yyyy" }` |
| GET | `/students?name=` | Lista alunos ativos (filtro por nome) |
| GET | `/students/{id}` | Consulta aluno |
| GET | `/students/{id}/schoolclasses` | Turmas do aluno |
| PUT | `/students` | Atualiza `{ id, name, birthDate }` |
| PATCH | `/students/{id}/image` | Envia foto (multipart, campo `image`) |
| GET | `/students/{id}/image` | Devolve a foto (público, para uso em `<img>`) |
| DELETE | `/students/{id}` | Inativa aluno |

### Turmas
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/schoolclasses` | Cadastra `{ name, shift, year, semester }` |
| GET | `/schoolclasses` | Lista turmas ativas |
| GET | `/schoolclasses/canceled` | Lista turmas canceladas |
| GET | `/schoolclasses/{id}` | Consulta turma |
| GET | `/schoolclasses/shifts` / `/schoolclasses/semesters` | Valores de turno e semestre |
| PUT | `/schoolclasses` | Atualiza `{ id, name, shift, year, semester }` |
| DELETE | `/schoolclasses/{id}` | Cancela turma |
| GET | `/schoolclasses/{id}/students` | Alunos da turma |
| POST | `/schoolclasses/{id}/students/{studentId}` | Vincula aluno à turma |
| DELETE | `/schoolclasses/{id}/students/{studentId}` | Desvincula aluno |

### Categorias e tipos de ocorrência
| Método | Rota | Descrição |
|--------|------|-----------|
| POST / GET | `/categories` | Cadastra `{ name }` / lista |
| GET / PUT / DELETE | `/categories/{id}` | Consulta / renomeia / exclui |
| POST / GET | `/types` (`?categoryId=`) | Cadastra `{ name, categoryId }` / lista |
| GET / PUT / DELETE | `/types/{id}` | Consulta / atualiza / exclui |

### Ocorrências
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/incidents` | Registra ocorrência (ver exemplo abaixo) |
| GET | `/incidents` | Consulta com filtros `studentId`, `schoolClassId`, `category`, `type`, `startDate`, `endDate` (yyyy-MM-dd), `includeDeleted` |
| GET | `/incidents/{id}` | Detalha ocorrência |
| GET | `/incidents/students/{studentId}/history` | Histórico do aluno em todas as turmas |
| PUT | `/incidents` | Altera ocorrência própria |
| DELETE | `/incidents/{id}` | Exclui (logicamente) ocorrência própria |
| GET | `/incidents/categories` | Nomes das categorias |
| GET | `/incidents/types/{categoria}` | Nomes dos tipos da categoria |
| GET | `/incidents/status` | Status possíveis `[{ name, description }]` |
| GET | `/incidents/status/{status}` | Lista por situação: `active`, `waiting`, `progressing`, `solved`, `unsolved`, `closed` |
| PATCH | `/incidents/status` | Altera status `{ id, status, updateDate }` |
| GET | `/incidents/summary` | Dashboard: totais por status, categoria, tipo, turma, período e ranking de alunos reincidentes |

Exemplo de registro de ocorrência:

```http
POST /incidents
Authorization: Bearer <token>
Content-Type: application/json

{
  "idSchoolClass": 1,
  "idStudent": 1,
  "registerDate": "01/11/2026 - 10:30",
  "category": "Disciplinar",
  "type": "Indisciplina",
  "description": "Conversou durante a avaliação mesmo após advertência."
}
```

Exemplo rápido com cURL:

```bash
TOKEN=$(curl -s -X POST localhost:8090/login -H "Content-Type: application/json" \
  -d '{"login":"coordenador","password":"123456"}' | sed 's/.*"tokenJWT":"\([^"]*\)".*/\1/')
curl -s localhost:8090/incidents/summary -H "Authorization: Bearer $TOKEN"
```
