<div align="center">

# Lisory 👑

**Backend do E-commerce de Joias**

Plataforma moderna para gestão de loja virtual de joias, com autenticação JWT, painel administrativo e integrações com gateways de pagamento e transporte.

![Java](https://img.shields.io/badge/Java-21-%23ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-%236DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?logo=jsonwebtokens&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.16-C71A36?logo=apachemaven&logoColor=white)
![Spring Modulith](https://img.shields.io/badge/Spring_Modulith-2.1.0-%236DB33F)
![License](https://img.shields.io/badge/license-MIT-green)

</div>

---

## Sumário

- [1. Sobre o Projeto](#1-sobre-o-projeto)
- [2. Tecnologias](#2-tecnologias)
- [3. Arquitetura](#3-arquitetura)
- [4. Estrutura do Projeto](#4-estrutura-do-projeto)
- [5. Funcionalidades](#5-funcionalidades)
- [6. Banco de Dados](#6-banco-de-dados)
- [7. API](#7-api)
- [8. Fluxo da Aplicação](#8-fluxo-da-aplicação)
- [9. Variáveis de Ambiente](#9-variáveis-de-ambiente)
- [10. Instalação](#10-instalação)
- [11. Como Executar Localmente](#11-como-executar-localmente)
- [12. Como Executar utilizando Docker](#12-como-executar-utilizando-docker)
- [13. Scripts](#13-scripts)
- [14. Estrutura dos Módulos](#14-estrutura-dos-módulos)
- [15. Segurança](#15-segurança)
- [16. Tratamento de Erros](#16-tratamento-de-erros)
- [17. Logs](#17-logs)
- [18. Deploy](#18-deploy)
- [19. Integrações (Planejadas)](#19-integrações-planejadas)
- [20. Roadmap](#20-roadmap)
- [21. Contribuição](#21-contribuição)
- [22. Licença](#22-licença)
- [23. Autor](#23-autor)

---

## 1. Sobre o Projeto

### Objetivo

O **Lisory** é um backend para e-commerce especializado em joias, desenvolvido para oferecer uma plataforma robusta, segura e escalável para gestão de loja virtual. O sistema utiliza uma abordagem de monolito modular com **Spring Modulith**, combinando a simplicidade de um monolito com a organização de módulos bem delimitados.

### Problema que Resolve

Lojas de joias enfrentam desafios específicos: garantia diferenciada por tipo de material (prata, dourado), variações sazonais de coleções, necessidade de checkout simplificado sem obrigatoriedade de cadastro, e cálculos de frete complexos. O Lisory endereça esses problemas com uma arquitetura preparada para essas necessidades.

### Público-alvo

- Lojas virtuais de joias e semijoias
- Administradores que precisam de um painel de gestão completo
- Clientes que buscam uma experiência de compra simplificada

### Principais Funcionalidades

| Funcionalidade | Status |
|---|---|
| Autenticação JWT (Register / Login) | ✅ Implementado |
| Refresh Token | ✅ Implementado (entidade criada) |
| Painel Administrativo | 📄 Documentado |
| Gestão de Produtos | 📄 Documentado |
| Gestão de Categorias | 📄 Documentado |
| Gestão de Coleções | 📄 Documentado |
| Gestão de Pedidos | 📄 Documentado |
| Gestão de Clientes | 📄 Documentado |
| Gestão de Cupons | 📄 Documentado |
| Configurações da Loja | 📄 Documentado |
| Perfil do Administrador | 📄 Documentado |
| Dashboard com Estatísticas | 📄 Documentado |
| Integração Asaas | ✅ Implementado |
| Integração Melhor Envio | 📄 Planejado |

> **Legenda:** ✅ Implementado | 📄 Documentado (pendente de implementação) | 🚧 Em desenvolvimento

---

## 2. Tecnologias

### Backend

| Tecnologia | Versão | Finalidade |
|---|---|---|
| [Java](https://openjdk.org/) | 21 | Linguagem de programação principal |
| [Spring Boot](https://spring.io/projects/spring-boot) | 4.1.0 | Framework de aplicação |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | — | ORM e persistência de dados |
| [Spring Security](https://spring.io/projects/spring-security) | — | Autenticação e autorização |
| [Spring Security OAuth2 Client](https://spring.io/projects/spring-security) | — | Autenticação via provedores OAuth2 |
| [Spring Validation](https://spring.io/projects/spring-framework) | — | Validação de beans Jakarta |
| [Spring WebMVC](https://spring.io/projects/spring-framework) | — | API REST |
| [Spring Modulith](https://spring.io/projects/spring-modulith) | 2.1.0 | Modularidade monolítica |
| [jjwt](https://github.com/jwtk/jjwt) | 0.13.0 | Geração e validação de tokens JWT |
| [Hibernate](https://hibernate.org/) | — | Implementação JPA / ORM |
| [BCrypt](https://en.wikipedia.org/wiki/Bcrypt) | — | Hash de senhas |

### Banco de Dados

| Tecnologia | Finalidade |
|---|---|
| [PostgreSQL](https://www.postgresql.org/) | Banco de dados relacional principal |

### Ferramentas e Build

| Ferramenta | Versão | Finalidade |
|---|---|---|
| [Maven Wrapper](https://maven.apache.org/wrapper/) | 3.9.16 | Automação de build |
| [JUnit 5](https://junit.org/junit5/) | — | Testes unitários |
| [Mockito](https://site.mockito.org/) | — | Mock objects para testes |
| [IntelliJ IDEA](https://www.jetbrains.com/idea/) | — | IDE de desenvolvimento |
| [FastRequest](https://github.com/dromara/fast-request) | — | Cliente HTTP integrado à IDE |

---

## 3. Arquitetura

O projeto adota uma **Arquitetura Modular Monolítica** com **Spring Modulith**, combinando os benefícios de um monolito (simplicidade operacional, deploy único, performance) com a organização de módulos bem definidos.

### Padrões de Projeto Utilizados

| Padrão | Aplicação |
|---|---|
| **DTO (Data Transfer Object)** | `AuthRequest`, `AuthResponse` — separação entre modelo de domínio e camada de apresentação |
| **Service Layer** | `AuthenticationService` — centralização da lógica de negócio |
| **Repository Pattern** | `AuthRepository` — abstração de acesso a dados via Spring Data JPA |
| **Filter Pattern** | `JwtAuthenticationFilter` — interceptação de requisições HTTP para validação JWT |
| **Strategy Pattern** | Interfaces do Spring Security (`UserDetailsService`, `PasswordEncoder`) |
| **Template Method** | `OncePerRequestFilter` — estrutura fixa com gancho para implementação customizada |
| **Injeção de Dependência** | Construtor com DI do Spring — todos os beans utilizam injeção via construtor |
| **Exception Handler Global** | `@RestControllerAdvice` em `GlobalExceptionHandler` — tratamento centralizado de exceções |

### Camadas da Arquitetura

```
┌─────────────────────────────────────────────────┐
│                 Controller Layer                 │
│              (AuthController)                    │
│           Endpoints REST + Validação             │
├─────────────────────────────────────────────────┤
│                   DTO Layer                      │
│         (AuthRequest / AuthResponse)             │
│        Objetos de transferência de dados         │
├─────────────────────────────────────────────────┤
│                 Service Layer                    │
│         (AuthenticationService)                  │
│            Lógica de negócio principal           │
├─────────────────────────────────────────────────┤
│               Security Layer                     │
│  (JwtAuthenticationFilter / TokenProvider)       │
│   Autenticação JWT, geração e validação de token │
├─────────────────────────────────────────────────┤
│              Repository Layer                    │
│          (AuthRepository - JPA)                  │
│              Acesso a dados                      │
├─────────────────────────────────────────────────┤
│                 Entity Layer                     │
│     (AuthEntity / RefreshTokenEntity / ROLES)    │
│           Mapeamento JPA + UserDetails           │
├─────────────────────────────────────────────────┤
│               Database (PostgreSQL)              │
│              auth_users / tb_refresh_tokens      │
└─────────────────────────────────────────────────┘
```

### Fluxo de Requisição com JWT

```
                    ┌───────────────┐
                    │   Cliente     │
                    └───────┬───────┘
                            │
              POST /auth/register ou /auth/login
                            │
                            ▼
              ┌─────────────────────────┐
              │   AuthController        │
              │   (valida @Valid)       │
              └────────────┬────────────┘
                           │
                           ▼
              ┌──────────────────────────┐
              │   AuthenticationService  │
              │   - Verifica email       │
              │   - BCrypt hash/check    │
              │   - Gera JWT             │
              └────────────┬────────────┘
                           │
                           ▼
              ┌──────────────────────────┐
              │   TokenProvider          │
              │   (jjwt - HMAC-SHA)      │
              │   .subject(username)     │
              │   .issuedAt(now)         │
              │   .expiration(...)       │
              │   .signWith(secretKey)   │
              └──────────────────────────┘

         Requisições autenticadas subsequentes:

              ┌───────────────┐
              │  Cliente      │
              │  Authorization│
              │  Bearer <JWT> │
              └───────┬───────┘
                      │
                      ▼
        ┌─────────────────────────────────┐
        │  JwtAuthenticationFilter        │
        │  (OncePerRequestFilter)         │
        │  1. Extrai header "Bearer "     │
        │  2. Extrai username do token    │
        │  3. Carrega UserDetails (DB)    │
        │  4. Valida token + expiração    │
        │  5. Seta SecurityContextHolder  │
        └─────────────────────────────────┘
                      │
                      ▼
              ┌──────────────────┐
              │  Endpoint alvo  │
              │  (autenticado)   │
              └──────────────────┘
```

---

## 4. Estrutura do Projeto

```
Lisory/
├── .fastRequest/                    # Configurações FastRequest (IDE)
│   └── config/
├── .mvn/wrapper/                    # Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/lisory/backend/
│   │   │   ├── LisoryApplication.java
│   │   │   └── auth/                # Módulo de Autenticação
│   │   │       ├── config/          # Configurações Spring Security
│   │   │       │   ├── AuthConfig.java
│   │   │       │   ├── SecurityBeansConfig.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── controller/      # Endpoints REST
│   │   │       │   └── AuthController.java
│   │   │       ├── dto/             # Data Transfer Objects
│   │   │       │   ├── AuthRequest.java
│   │   │       │   └── AuthResponse.java
│   │   │       ├── entity/          # Entidades JPA
│   │   │       │   ├── AuthEntity.java
│   │   │       │   ├── ROLES.java
│   │   │       │   └── RefreshTokenEntity.java
│   │   │       ├── repository/      # Repositórios JPA
│   │   │       │   └── AuthRepository.java
│   │   │       └── services/        # Lógica de negócio
│   │   │           ├── AuthenticationService.java
│   │   │           ├── JpaUserDetailsService.java
│   │   │           ├── JwtAuthenticationFilter.java
│   │   │           └── TokenProvider.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db.migration/        # Migrations SQL
│   │           └── V1__create_table_auth.sql
│   └── test/java/com/lisory/backend/
│       ├── LisoryApplicationTests.java
│       └── auth/
│           ├── controller/
│           │   └── AuthControllerTest.java
│           └── services/
│               └── AuthenticationServiceTest.java
├── target/                          # Build output
├── .gitattributes
├── .gitignore
├── API_REFERENCE.md                 # Documentação dos endpoints
├── Entidades.md                     # Modelo de dados completo
├── HELP.md                          # Documentação de referência Spring
├── mvnw                             # Maven Wrapper (Unix)
├── mvnw.cmd                         # Maven Wrapper (Windows)
├── pom.xml                          # Configuração Maven
└── README.md                        # Este arquivo
```

### Propósito das Pastas e Arquivos

| Caminho | Propósito |
|---|---|
| `auth/config/` | Configurações de segurança: `SecurityFilterChain`, `PasswordEncoder`, `AuthenticationManager` e tratamento global de exceções |
| `auth/controller/` | Endpoints REST expostos para o módulo de autenticação |
| `auth/dto/` | Objetos de transferência de dados entre controller e service |
| `auth/entity/` | Entidades JPA mapeadas para o banco de dados |
| `auth/repository/` | Interfaces Spring Data JPA para acesso a dados |
| `auth/services/` | Lógica de negócio, filtros JWT e provedor de tokens |
| `resources/db.migration/` | Scripts de migração de banco de dados |

---

## 5. Funcionalidades

### Autenticação

- **Registro de usuário** — Cadastro com email e senha, hash BCrypt, validação de unicidade de email
- **Autenticação (Login)** — Verificação de credenciais, geração de token JWT
- **Refresh Token** — Entidade preparada para renovação de tokens (pendente de implementação no serviço)
- **Roles** — Suporte a papéis `ADMIN` e `USER` com authorities do Spring Security

### Segurança

- **JWT Token** — Geração e validação de tokens JWT com algoritmo HMAC-SHA
- **Filtro JWT** — Interceptação de requisições para validação automática de token
- **Stateless Sessions** — Sessões sem estado (stateless) via JWT
- **CORS** — Configuração de Cross-Origin Resource Sharing
- **BCrypt** — Hash seguro de senhas

### Infraestrutura

- **Tratamento de erros global** — `GlobalExceptionHandler` com respostas padronizadas
- **Modularidade** — Organizado com Spring Modulith para evolução independente dos módulos
- **Validação de entrada** — Jakarta Validation com mensagens de erro detalhadas

---

## 6. Banco de Dados

### ORM utilizado

O projeto utiliza **Hibernate** via **Spring Data JPA** com estratégia `ddl-auto=update`, ou seja, as tabelas são geradas automaticamente a partir das entidades JPA.

### Entidades Implementadas

#### `auth_users`

Mapeada por: `AuthEntity.java`

| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | UUID (PK) | Gerado automaticamente | Identificador único do usuário |
| `email` | VARCHAR | `unique`, `not null` | Email utilizado para login |
| `password` | VARCHAR | `not null` | Senha criptografada com BCrypt |
| `role` | ENUM (`ADMIN`/`USER`) | — | Papel do usuário no sistema |
| `is_active` | BOOLEAN | — | Indica se a conta está ativa |

Implementa `UserDetails` do Spring Security, fornecendo:
- `getAuthorities()` — Retorna `ROLE_ADMIN` ou `ROLE_USER`
- `getUsername()` — Retorna o email
- Controle de conta ativa/inativa via campo `is_active`

#### `tb_refresh_tokens`

Mapeada por: `RefreshTokenEntity.java`

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID (PK) | Identificador único |
| `users_id` | UUID (FK) | ID do usuário proprietário |
| `tokens` | VARCHAR | Token JWT de refresh |
| `expired` | BOOLEAN | Indica se o token expirou |
| `blacklists` | BOOLEAN | Indica se o token foi revogado |

> **Nota:** A funcionalidade completa de refresh token (emissão, renovação, revogação) está documentada mas ainda não implementada nos serviços.

### Migrations

O diretório `src/main/resources/db.migration/` contém o arquivo:

| Arquivo | Status | Descrição |
|---|---|---|
| `V1__create_table_auth.sql` | ⚠️ Vazio | Script de migração inicial (vazio — DDL gerenciado pelo Hibernate) |

### Modelo de Dados Completo (Documentado)

O arquivo [`Entidades.md`](./Entidades.md) descreve **12 entidades** planejadas para o sistema completo:

| Entidade | Tabela | Status |
|---|---|---|
| Users | `auth_users` | ✅ Implementado |
| Refresh Tokens | `tb_refresh_tokens` | ✅ Implementado |
| Addresses | — | 📄 Documentado |
| Products | — | 📄 Documentado |
| Product Images | — | 📄 Documentado |
| Carts | — | 📄 Documentado |
| Cart Items | — | 📄 Documentado |
| Coupons | — | 📄 Documentado |
| Orders | — | 📄 Documentado |
| Order Items | — | 📄 Documentado |
| Payments | — | 📄 Documentado |
| Shipments | — | 📄 Documentado |

### Relacionamentos (Diagrama Simplificado)

```text
Users
 ├───────────────< Addresses
 ├───────────────< Refresh Tokens
 ├───────────────< Orders >────────────── Coupons
 │                     │
 │                     ├──────────────< Order Items >──────── Products
 │                     ├────────────── Payments
 │                     └────────────── Shipments
 │
 └────────────── Cart ───────────────< Cart Items >────────── Products

Products
 └──────────────< Product Images
```

---

## 7. API

### Base URL

```
http://localhost:8080
```

### Endpoints Implementados

#### Autenticação — `/auth`

---

##### `POST /auth/register`

Registra um novo usuário no sistema.

**Body da Requisição:**

```json
{
  "email": "usuario@exemplo.com",
  "password": "senha123"
}
```

**Regras de Validação:**

| Campo | Regra |
|---|---|
| `email` | Obrigatório, formato de email válido |
| `password` | Obrigatório, mínimo 8 caracteres, máximo 20 caracteres |

**Respostas:**

| Status | Descrição |
|---|---|
| `200 OK` | Usuário registrado com sucesso (body vazio) |
| `400 Bad Request` | Erro de validação ou email já cadastrado |

**Exemplo de Erro:**

```json
{
  "error": "Email already registered"
}
```

---

##### `POST /auth/login`

Autentica um usuário e retorna um token JWT.

**Body da Requisição:**

```json
{
  "email": "usuario@exemplo.com",
  "password": "senha123"
}
```

**Resposta de Sucesso (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQGV4ZW1wbG8uY29tIiwiaWF0IjoxNzE4MDAwMDAwLCJleHAiOjE3MTgwODY0MDB9.exemplo_token"
}
```

**Respostas de Erro:**

| Status | Descrição |
|---|---|
| `400 Bad Request` | Credenciais inválidas |

```json
{
  "error": "Invalid email or password"
}
```

### Endpoints Documentados (Planejados)

O arquivo [`API_REFERENCE.md`](./API_REFERENCE.md) contém a documentação completa de todos os endpoints planejados:

| Módulo | Endpoints | Status |
|---|---|---|
| **Autenticação** | `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me` | 🔶 Parcial |
| **Dashboard** | `GET /api/admin/dashboard/stats`, `GET /api/admin/dashboard/sales`, `GET /api/admin/dashboard/recent-orders`, `GET /api/admin/dashboard/top-products`, `GET /api/admin/dashboard/recent-customers` | 📄 Planejado |
| **Produtos** | CRUD `/api/admin/products` | 📄 Planejado |
| **Categorias** | CRUD `/api/admin/categories` | 📄 Planejado |
| **Coleções** | `GET /api/admin/collections` | 📄 Planejado |
| **Pedidos** | `GET /api/admin/orders`, `GET /api/admin/orders/:id` | 📄 Planejado |
| **Clientes** | `GET /api/admin/customers`, `GET /api/admin/customers/:id`, `GET /api/admin/customers/top` | 📄 Planejado |
| **Cupons** | CRUD + toggle + duplicate + status `/api/admin/coupons` | 📄 Planejado |
| **Config. Loja** | `GET /api/admin/settings`, `PUT /api/admin/settings` | 📄 Planejado |
| **Perfil Admin** | `GET /api/admin/profile`, `PUT /api/admin/profile`, `PUT /api/admin/profile/avatar`, `PUT /api/admin/profile/password` | 📄 Planejado |

---

## 8. Fluxo da Aplicação

### Fluxo de Autenticação

```text
Cadastro (POST /auth/register)
    │
    ├── Valida email + senha (@Valid)
    ├── Verifica se email já existe
    ├── Hash da senha (BCrypt)
    └── Salva usuário no banco
    │
    ▼
Login (POST /auth/login)
    │
    ├── Busca usuário por email
    ├── Verifica senha (BCrypt.matches)
    ├── Gera token JWT (jjwt)
    └── Retorna { "token": "eyJ..." }
    │
    ▼
Requisições autenticadas
    │
    ├── Header: Authorization: Bearer <token>
    ├── JwtAuthenticationFilter:
    │   ├── Extrai token
    │   ├── Extrai username do token
    │   ├── Carrega UserDetails do banco
    │   └── Valida token + expiração
    └── Acesso ao recurso protegido
```

### Fluxo Geral do Sistema (Planejado)

Conforme documentado em [`Entidades.md`](./Entidades.md):

```text
Usuário
    │
    ▼
Cadastro / Login
    │
    ▼
Carrinho
    │
    ▼
Adicionar Produtos
    │
    ▼
Aplicar Cupom (Opcional)
    │
    ▼
Checkout
    │
    ▼
Pedido
   ├──────────────┐
   │              │
Pagamento      Envio
   │              │
   └──────► Pedido Finalizado
```

---

## 9. Variáveis de Ambiente

Todas as variáveis possuem valores padrão definidos em [`application.properties`](src/main/resources/application.properties). Para ambiente de produção, recomenda-se configurar valores específicos via variáveis de ambiente ou arquivo `.env`.

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `DB_URL` | Não | `jdbc:postgresql://localhost:5432/Lisory` | URL de conexão com o banco PostgreSQL |
| `DB_USERNAME` | Não | `Lisory` | Usuário do banco de dados |
| `DB_PASSWORD` | Não | `LisoryTest` | Senha do banco de dados |
| `JWT_SECRET` | Não | *(valor base64 fixo)* | Chave secreta para assinatura HMAC-SHA dos tokens JWT |
| `JWT_EXPIRATION` | Não | `86400000` | Tempo de expiração do token JWT em milissegundos (24 horas) |
| `SHOW_SQL` | Não | `false` | Exibe queries SQL no console (útil para debugging) |
| `FORMAT_SQL` | Não | `false` | Formata as queries SQL exibidas |

> ⚠️ **Importante:** A chave `JWT_SECRET` padrão inclusa no código **não deve ser utilizada em produção**. Sempre configure uma chave secreta forte e única via variável de ambiente.

---

## 10. Instalação

### Pré-requisitos

- **Java 21** (JDK 21+)
- **Maven 3.9+** (ou utilizar o Maven Wrapper incluso)
- **PostgreSQL 16+**
- **Git**

### Passo a Passo

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/lisory.git
cd lisory

# 2. Configure o banco PostgreSQL
# Crie um banco de dados chamado "Lisory"
psql -U postgres -c "CREATE DATABASE Lisory;"

# 3. (Opcional) Configure variáveis de ambiente
# Exporte ou crie um arquivo .env com as configurações desejadas
export DB_URL=jdbc:postgresql://localhost:5432/Lisory
export DB_USERNAME=seu_usuario
export DB_PASSWORD=sua_senha
export JWT_SECRET=sua_chave_secreta_base64
export JWT_EXPIRATION=86400000

# 4. Execute a aplicação com Maven Wrapper
./mvnw spring-boot:run

# Alternativa: compile e execute o JAR
./mvnw clean package -DskipTests
java -jar target/Lisory-0.0.1-SNAPSHOT.jar
```

---

## 11. Como Executar Localmente

### Usando Maven Wrapper (recomendado)

```bash
# Terminal 1: Executar a aplicação
./mvnw spring-boot:run

# A aplicação iniciará em http://localhost:8080
```

### Usando JAR compilado

```bash
# Compilar o projeto
./mvnw clean package -DskipTests

# Executar o JAR
java -jar target/Lisory-0.0.1-SNAPSHOT.jar
```

### Executar Testes

```bash
# Executar todos os testes
./mvnw test

# Executar testes de um módulo específico
./mvnw test -Dtest="com.lisory.backend.auth.services.AuthenticationServiceTest"

# Executar testes com relatório detalhado
./mvnw test -Dtest="com.lisory.backend.auth.*"
```

---

## 12. Como Executar utilizando Docker

No momento, o projeto **não possui** configuração Docker (`Dockerfile`, `docker-compose.yml`). Esta seção será atualizada quando a conteinerização for implementada.

Para executar localmente sem Docker, siga as instruções da [seção anterior](#11-como-executar-localmente).

---

## 13. Scripts

| Comando | Descrição |
|---|---|
| `./mvnw spring-boot:run` | Executa a aplicação Spring Boot em modo desenvolvimento |
| `./mvnw compile` | Compila o código fonte |
| `./mvnw test` | Executa todos os testes unitários |
| `./mvnw clean` | Limpa o diretório `target/` |
| `./mvnw clean package -DskipTests` | Compila e empacota o JAR sem executar testes |
| `./mvnw clean package` | Compila, testa e empacota o JAR |
| `./mvnw dependency:tree` | Exibe a árvore de dependências do projeto |
| `./mvnw validate` | Valida o projeto (estrutura, dependências) |

> No Windows, utilize `mvnw.cmd` no lugar de `./mvnw`.

---

## 14. Estrutura dos Módulos

### Módulo `auth` (Autenticação)

Responsável por todo o ciclo de autenticação e autorização da aplicação.

#### `config/`

| Arquivo | Finalidade |
|---|---|
| `AuthConfig.java` | Configura o `SecurityFilterChain` do Spring Security: desabilita CSRF, define política de sessão stateless, configura rotas públicas (`/auth/**`) e protegidas, adiciona o filtro JWT |
| `SecurityBeansConfig.java` | Declara os beans de `PasswordEncoder` (BCrypt) e `AuthenticationManager` |
| `GlobalExceptionHandler.java` | Intercepta exceções lançadas pelos controllers e retorna respostas padronizadas em JSON |

#### `controller/`

| Arquivo | Finalidade |
|---|---|
| `AuthController.java` | Expõe os endpoints `POST /auth/register` e `POST /auth/login`. Valida os dados de entrada com `@Valid` |

#### `dto/`

| Arquivo | Finalidade |
|---|---|
| `AuthRequest.java` | Record que define o payload de requisição com validações: `@Email`, `@NotBlank`, `@Size(min=8, max=20)` |
| `AuthResponse.java` | Record que encapsula o token JWT retornado no login |

#### `entity/`

| Arquivo | Finalidade |
|---|---|
| `AuthEntity.java` | Entidade JPA `auth_users` que implementa `UserDetails` do Spring Security. Gerencia id (UUID), email, password (hash), role, is_active |
| `RefreshTokenEntity.java` | Entidade JPA `tb_refresh_tokens` para armazenar tokens de refresh com controle de expiração e blacklist |
| `ROLES.java` | Enumeração com os papéis `ADMIN` e `USER` |

#### `repository/`

| Arquivo | Finalidade |
|---|---|
| `AuthRepository.java` | Interface Spring Data JPA com métodos `findByEmail` e `existsByEmail` |

#### `services/`

| Arquivo | Finalidade |
|---|---|
| `AuthenticationService.java` | Lógica de negócio: registro (verifica duplicidade, hash da senha, salva) e autenticação (busca por email, verifica senha, gera token) |
| `TokenProvider.java` | Geração e validação de tokens JWT com jjwt: `generateToken`, `extractUsername`, `validateToken` |
| `JwtAuthenticationFilter.java` | Filtro `OncePerRequestFilter` que extrai o token do header `Authorization`, valida e configura o `SecurityContextHolder` |
| `JpaUserDetailsService.java` | Implementação customizada de `UserDetailsService` que carrega usuários do banco via `AuthRepository` |

---

## 15. Segurança

### JWT (JSON Web Token)

- **Algoritmo:** HMAC-SHA via `Keys.hmacShaKeyFor()`
- **Claims:** `subject` (email do usuário), `issuedAt`, `expiration`
- **Header:** Enviado como `Authorization: Bearer <token>`
- **Validação:** Verificação de assinatura + expiração em cada requisição protegida

### Spring Security

- **CSRF:** Desabilitado (API REST)
- **Session Management:** Stateless (sem sessões HTTP)
- **Rotas Públicas:** `/auth/**` (register, login)
- **Rotas Protegidas:** Demais endpoints exigem token JWT válido
- **Autenticação:** Via `JwtAuthenticationFilter` executado antes do `UsernamePasswordAuthenticationFilter`

### CORS

Configuração pronta para liberação de origens específicas (personalizável via `CorsConfigurationSource` em `AuthConfig.java`).

### Roles e Autoridades

| Role | Authority | Descrição |
|---|---|---|
| `USER` | `ROLE_USER` | Usuário comum |
| `ADMIN` | `ROLE_ADMIN` | Administrador |

As authorities são derivadas do campo `role` da entidade `AuthEntity`.

### Validações

- **Jakarta Validation:** `@Email`, `@NotBlank`, `@Size(min=8, max=20)` no `AuthRequest`
- **Validação de senha:** BCrypt `matches()` no login
- **Unicidade de email:** Consulta `existsByEmail` antes do registro

### Tratamento de Tokens

- `generateToken(String username)` — Cria novo token JWT com expiração configurável
- `extractUsername(String token)` — Extrai o subject (email) do token
- `validateToken(String token, UserDetails)` — Valida assinatura + username + expiração

---

## 16. Tratamento de Erros

A aplicação utiliza um `@RestControllerAdvice` global (`GlobalExceptionHandler.java`) para centralizar o tratamento de exceções, garantindo respostas padronizadas em JSON.

| Exceção | Status HTTP | Formato da Resposta |
|---|---|---|
| `IllegalArgumentException` | `400 Bad Request` | `{ "error": "mensagem" }` |
| `MethodArgumentNotValidException` | `400 Bad Request` | `{ "error": "campo: mensagem; campo2: mensagem2" }` |
| `Exception` (genérica) | `500 Internal Server Error` | `{ "error": "Internal server error" }` |

### Exemplos

**Erro de validação de email:**

```json
{
  "error": "email: must be a well-formed email address"
}
```

**Erro de senha curta:**

```json
{
  "error": "password: size must be between 8 and 20"
}
```

**Credenciais inválidas:**

```json
{
  "error": "Invalid email or password"
}
```

---

## 17. Logs

A aplicação utiliza o sistema de logging padrão do Spring Boot (SLF4J + Logback).

### Configuração Atual

- **Nível padrão:** INFO (definido pelo Spring Boot)
- **JPA SQL:** Controlado pelas propriedades `SHOW_SQL` e `FORMAT_SQL` (desabilitado por padrão)
- **JWT Filter:** Loga avisos em caso de falha de autenticação via `logger.warn("JWT authentication failed: " + e.getMessage())`

### Ativar Logs de SQL

```properties
# application.properties ou variável de ambiente
SHOW_SQL=true
FORMAT_SQL=true
```

---

## 18. Deploy

No momento, o projeto **não possui** configuração de deploy automatizado (CI/CD, Docker, Railway, Vercel, Nginx). Esta seção será atualizada quando o pipeline de deploy for implementado.

Para executar a aplicação em produção manualmente:

```bash
# Compilar o JAR
./mvnw clean package -DskipTests

# Executar (configurar variáveis de ambiente primeiro)
java -jar target/Lisory-0.0.1-SNAPSHOT.jar
```

---

## 19. Integrações (Planejadas)

As seguintes integrações estão documentadas no modelo de dados ([`Entidades.md`](./Entidades.md)) e na referência de API ([`API_REFERENCE.md`](./API_REFERENCE.md)), mas ainda não foram implementadas.

| Integração | Finalidade | Documentado em |
|---|---|---|
| **Asaas** | Gateway de pagamento (PIX e Cartão de crédito) | `Entidades.md` — Entidade `Payments` |
| **Melhor Envio** | Cálculo de frete, geração de etiquetas e rastreamento | `Entidades.md` — Entidade `Shipments` |

### Canais de Contato (Planejados)

Conforme documentado na API de configurações da loja:
- WhatsApp
- Instagram
- E-mail

---

## 20. Roadmap

As seguintes funcionalidades estão documentadas e planejadas para implementação futura:

### Curto Prazo

- [ ] Implementar refresh token (emissão, renovação e revogação)
- [ ] Endpoint `GET /api/auth/me` (dados do usuário logado)
- [ ] Endpoint `POST /api/auth/logout` (invalidação de token)
- [ ] Gestão de Produtos (CRUD)
- [ ] Gestão de Categorias (CRUD)

### Médio Prazo

- [ ] Gestão de Pedidos
- [ ] Gestão de Clientes
- [ ] Dashboard com estatísticas
- [ ] Upload de imagens de produtos
- [ ] Gestão de Cupons

### Longo Prazo

- [x] Integração com Asaas
- [ ] Integração com Melhor Envio
- [ ] Configurações da loja
- [ ] Carrinho de compras
- [ ] Checkout

> **Nota:** Este roadmap reflete exclusivamente os módulos e funcionalidades documentados nos arquivos [`API_REFERENCE.md`](./API_REFERENCE.md) e [`Entidades.md`](./Entidades.md) do projeto. Nenhuma funcionalidade foi inventada.

---

## 21. Contribuição

Contribuições são bem-vindas! Siga os passos abaixo:

1. Faça um **fork** do repositório
2. Crie uma **branch** para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. **Commit** suas alterações (`git commit -m 'feat: adiciona nova funcionalidade'`)
4. Faça **push** para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um **Pull Request**

### Diretrizes

- Mantenha o padrão de código existente
- Escreva testes para novas funcionalidades
- Documente endpoints e entidades no padrão já estabelecido
- Utilize commits semânticos (conventional commits)

---

## 22. Licença

Este projeto está licenciado sob a licença **MIT**. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 23. Autor

As informações do autor não estão especificadas no projeto atualmente. O arquivo [`pom.xml`](pom.xml) contém campos de `<developers>` vazios (herdados do parent Spring Boot).

Este espaço será preenchido quando as informações do autor ou da equipe de desenvolvimento forem disponibilizadas.

---

<div align="center">

Desenvolvido com 💛 para o universo das joias

</div>
