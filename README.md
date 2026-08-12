# Auto Job Match

Aplicação full stack para automatizar a busca, análise e priorização de vagas de tecnologia com apoio de Inteligência Artificial local.

O sistema utiliza o perfil profissional do usuário como fonte de contexto para comparar vagas, calcular compatibilidade, identificar requisitos atendidos e ausentes e, para oportunidades com alta compatibilidade, gerar materiais personalizados de candidatura.

> **Status:** MVP técnico avançado. Os principais fluxos de backend já foram desenvolvidos, mas a integração frontend/backend e alguns recursos de produto ainda estão em evolução.

---

## Sumário

- [Visão geral](#visão-geral)
- [Principais funcionalidades](#principais-funcionalidades)
- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [Fluxo principal](#fluxo-principal)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Executando o projeto](#executando-o-projeto)
- [API](#api)
- [Inteligência Artificial](#inteligência-artificial)
- [Automação do LinkedIn](#automação-do-linkedin)
- [Regras de negócio](#regras-de-negócio)
- [Estado atual](#estado-atual)
- [Próximos passos](#próximos-passos)
- [Decisões técnicas](#decisões-técnicas)
- [Segurança](#segurança)

---

## Visão geral

O Auto Job Match foi criado para reduzir o trabalho repetitivo envolvido na procura por vagas:

- acessar diferentes plataformas;
- executar pesquisas;
- abrir e ler vagas;
- comparar requisitos com o currículo;
- identificar oportunidades compatíveis;
- adaptar o resumo profissional;
- reorganizar experiências relevantes;
- gerar carta de apresentação;
- guardar links e resultados;
- evitar reprocessamento de vagas.

A aplicação centraliza esse processo e transforma uma vaga encontrada em um resultado estruturado contendo dados da oportunidade, compatibilidade e materiais de candidatura.

---

## Principais funcionalidades

### Perfil profissional

O sistema possui estrutura para armazenar:

- nome;
- e-mail;
- senha;
- telefone;
- senioridade;
- resumo profissional;
- currículo completo;
- habilidades;
- LinkedIn;
- GitHub e outros links, conforme a versão do modelo;
- demais informações profissionais.

O perfil é utilizado como fonte oficial de contexto para a análise da IA.

### Análise de compatibilidade

Para cada vaga, a IA analisa informações como:

- tecnologias;
- requisitos técnicos;
- responsabilidades;
- senioridade;
- experiência exigida;
- conhecimentos desejáveis;
- localização;
- modalidade;
- idioma;
- formação;
- contexto da oportunidade.

O resultado contém:

- porcentagem de compatibilidade;
- justificativa;
- requisitos atendidos;
- habilidades faltantes;
- indicação se vale a pena aplicar.

### Geração de materiais

Quando o match ultrapassa o limite definido no projeto, são gerados:

- resumo profissional adaptado;
- experiências adaptadas;
- carta de apresentação;
- conteúdo personalizado para currículo;
- currículo em LaTeX.

A geração é feita em uma segunda etapa de IA para evitar processamento desnecessário em vagas com baixa compatibilidade.

### Histórico

As vagas analisadas são persistidas no PostgreSQL, permitindo recuperar:

- vaga;
- empresa;
- descrição;
- link;
- porcentagem;
- justificativa;
- requisitos atendidos;
- habilidades faltantes;
- decisão de candidatura;
- currículo gerado;
- carta gerada.

### Busca automatizada

O projeto possui integrações/desenvolvimentos para:

- LinkedIn via Playwright;
- Gupy por API/endpoints estruturados;
- Remotive como prova de conceito.

A arquitetura procura utilizar dados estruturados quando disponíveis e Playwright quando a navegação automatizada é necessária.

---

## Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- Auth0 Java JWT
- BCrypt
- Spring AI
- Playwright for Java
- Maven
- Lombok

### Frontend

- React
- TypeScript
- Vite
- Tailwind CSS v4
- Fetch API
- `localStorage` para armazenamento do JWT

### Banco e infraestrutura

- PostgreSQL 15
- Docker / Docker Compose
- DBeaver
- Ollama
- Chromium

### Integrações

- LinkedIn
- Gupy
- Remotive
- Spring AI
- Ollama
- LaTeX

---

## Arquitetura

A aplicação utiliza arquitetura modular em camadas.

```text
┌─────────────────────────────────────────────┐
│                    USUÁRIO                  │
│ Login • Perfil • Vagas • Resultados         │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│           FRONTEND REACT + TS               │
│ Login • Dashboard • Perfil • Vagas           │
└──────────────────────┬──────────────────────┘
                       │ HTTP / JSON + JWT
                       ▼
┌─────────────────────────────────────────────┐
│             BACKEND SPRING BOOT             │
│                                             │
│ Controllers                                 │
│ Services                                    │
│ Security / JWT                              │
│ MatchService                                │
│ Spring AI                                   │
│ Playwright                                  │
│ Integrações externas                        │
│ Geração de LaTeX                            │
└──────────────┬──────────────┬───────────────┘
               │              │
               ▼              ▼
        ┌─────────────┐  ┌─────────────┐
        │ PostgreSQL  │  │   Ollama    │
        │ Perfis      │  │ Análise IA  │
        │ Vagas       │  │ Geração     │
        └─────────────┘  └─────────────┘

               ┌─────────────────────────┐
               │ Fontes externas         │
               │ LinkedIn • Gupy • etc.  │
               └─────────────────────────┘
```

O fluxo interno principal segue:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
JPA / Hibernate
    ↓
PostgreSQL
```

Para recursos externos:

```text
Service
 ├── Spring AI → Ollama
 ├── Playwright → LinkedIn
 ├── HTTP → Gupy / Remotive
 └── Repository → PostgreSQL
```

---

## Fluxo principal

### 1. Login

```text
Frontend
   ↓
POST /auth/login
   ↓
AuthenticationManager
   ↓
AutenticacaoService
   ↓
PerfilRepository.findByEmail()
   ↓
BCrypt
   ↓
TokenService
   ↓
JWT
   ↓
Frontend
```

### 2. Análise de uma vaga

```text
VagaRequest
    ↓
Carregar perfil
    ↓
Montar prompt
    ↓
Ollama
    ↓
AnaliseMatch
    ↓
VagaAnalisada
    ↓
PostgreSQL
```

### 3. Geração de candidatura

```text
Análise
   ↓
Match > 70
   ↓
Segunda chamada à IA
   ↓
Resumo + experiências + carta
   ↓
Template LaTeX fixo
   ↓
Currículo personalizado
```

---

## Estrutura do projeto

A estrutura documentada é aproximadamente:

```text
auto_job_match/
├── backend/
│   ├── pom.xml
│   ├── linkedin-session.json
│   └── src/main/
│       ├── java/com/auto_job_match/
│       │   ├── Application.java
│       │   ├── controller/
│       │   │   ├── PerfilController.java
│       │   │   ├── MatchController.java
│       │   │   ├── BotController.java
│       │   │   └── AuthController.java
│       │   ├── dto/
│       │   │   ├── VagaRequest.java
│       │   │   ├── AnaliseMatch.java
│       │   │   ├── MateriaisCandidatura.java
│       │   │   ├── AuthenticationDto.java
│       │   │   └── LoginResponseDto.java
│       │   ├── model/
│       │   │   ├── Perfil.java
│       │   │   └── VagaAnalisada.java
│       │   ├── repository/
│       │   │   ├── PerfilRepository.java
│       │   │   └── VagaAnalisadaRepository.java
│       │   ├── service/
│       │   │   ├── MatchService.java
│       │   │   ├── AutomacaoNavegadorService.java
│       │   │   └── VagaBuscadorService.java
│       │   └── security/
│       │       ├── SecurityConfigurations.java
│       │       ├── SecurityFilter.java
│       │       ├── TokenService.java
│       │       └── AutenticacaoService.java
│       └── resources/
│           └── application.properties
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── index.html
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── index.css
│       ├── pages/
│       │   ├── Login.tsx
│       │   └── Dashboard.tsx
│       └── services/
│           └── api.ts
└── docker-compose.yml
```

Os nomes podem variar conforme a versão atual do código.

---

## Pré-requisitos

Antes de iniciar, é necessário ter:

- Java 21;
- Maven;
- Node.js e npm;
- Docker;
- PostgreSQL via Docker;
- Ollama;
- Chromium/Playwright configurado pelo backend.

Para automação do LinkedIn, também é necessária uma sessão autenticada criada pelo próprio fluxo do projeto.

---

## Configuração

### PostgreSQL

A configuração utilizada durante o desenvolvimento foi:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/job_match_db
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Docker Compose:

```yaml
services:
  postgres:
    image: postgres:15
    container_name: job_db
    environment:
      POSTGRES_USER: root
      POSTGRES_PASSWORD: root
      POSTGRES_DB: job_match_db
    ports:
      - "5432:5432"
```

### Ollama

A configuração documentada utiliza:

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3
spring.ai.ollama.chat.options.format=json
spring.ai.ollama.chat.options.temperature=0.2
```

O modelo exato deve ser confirmado no `application.properties` atual antes da execução.

---

## Executando o projeto

### 1. Subir o PostgreSQL

Na raiz:

```bash
docker compose up -d
```

Verifique:

```bash
docker ps
```

O PostgreSQL deve estar disponível na porta `5432`.

### 2. Iniciar o Ollama

O Ollama deve estar executando localmente na porta:

```text
11434
```

Confirme também se o modelo configurado pelo backend está instalado.

### 3. Iniciar o backend

Entre na pasta:

```bash
cd backend
```

Execute:

```bash
mvn spring-boot:run
```

O backend utiliza a porta:

```text
8080
```

### 4. Instalar dependências do frontend

```bash
cd frontend
npm install
```

### 5. Iniciar o frontend

```bash
npm run dev
```

O Vite utiliza, conforme a configuração documentada:

```text
http://localhost:5173
```

> Se `npm run dev` informar que o script não existe, confirme se o terminal está dentro de `frontend/` e se o `package.json` possui `"dev": "vite"`.

---

## API

### Autenticação

#### Login

```http
POST /auth/login
```

Entrada:

```json
{
  "email": "usuario@email.com",
  "senha": "senha"
}
```

Saída:

```json
{
  "token": "eyJ..."
}
```

O token deve ser enviado nas rotas protegidas:

```http
Authorization: Bearer <token>
```

---

### Perfis

#### Criar perfil

```http
POST /api/perfis
```

Exemplo:

```json
{
  "nome": "Usuário",
  "email": "usuario@email.com",
  "senha": "senha",
  "telefone": "...",
  "senioridade": "Júnior",
  "resumo": "...",
  "curriculo": "...",
  "habilidades": [
    "Java",
    "Spring Boot",
    "React"
  ]
}
```

#### Listar perfis

```http
GET /api/perfis
```

> A implementação atual precisa evitar que a senha, mesmo em BCrypt, seja exposta diretamente no JSON.

---

### Match

#### Analisar vaga

```http
POST /api/match
```

Entrada:

```json
{
  "titulo": "Desenvolvedor Java",
  "empresa": "Empresa X",
  "descricaoCompleta": "Descrição integral da vaga...",
  "linkVaga": "https://..."
}
```

Saída esperada:

```json
{
  "id": 1,
  "titulo": "Desenvolvedor Java",
  "empresa": "Empresa X",
  "descricaoCompleta": "...",
  "porcentagemMatch": 82,
  "justificativa": "...",
  "valeApenaAplicar": true,
  "requisitosAtendidos": [
    "Java",
    "Spring Boot"
  ],
  "habilidadesFaltantes": [
    "Kubernetes"
  ],
  "curriculoGerado": "...",
  "coverLetterGerada": "...",
  "linkVaga": "https://..."
}
```

#### Histórico

```http
GET /api/match
```

Retorna as vagas analisadas persistidas no banco.

#### Busca web — Remotive

```http
POST /api/match/buscar-web
```

É uma prova de conceito síncrona.

---

### Bot LinkedIn

Rotas documentadas:

```text
/api/bot/gerar-sessao
/api/bot/buscar
```

O método HTTP final deve ser confirmado no `BotController` atual.

---

## Inteligência Artificial

A IA é executada localmente por meio do Ollama e integrada ao backend usando Spring AI.

O `ChatClient` transforma a resposta da IA diretamente em objetos Java:

```java
chatClient
    .prompt()
    .user(prompt)
    .call()
    .entity(AnaliseMatch.class);
```

### Primeira etapa

A IA analisa a compatibilidade.

```text
Perfil + Currículo + Vaga
          ↓
        Ollama
          ↓
     AnaliseMatch
```

### Segunda etapa

Somente vagas acima do limite definido recebem geração de materiais.

Regra registrada:

```java
if (porcentagemMatch > 70) {
    // gerar materiais
}
```

O limite `> 70` deve ser mantido ou explicitamente alterado caso a regra de negócio passe a utilizar `>= 70`.

---

## Automação do LinkedIn

O Playwright controla o Chromium e realiza:

1. abertura do LinkedIn;
2. carregamento da sessão;
3. localização das vagas;
4. extração dos cards;
5. abertura das vagas;
6. extração dos detalhes;
7. envio ao `MatchService`;
8. processamento de múltiplas vagas;
9. controle de erros e retry;
10. deduplicação durante a execução.

### Sessão

O login é realizado manualmente na primeira execução.

Depois, o estado da sessão é salvo em:

```text
linkedin-session.json
```

Nas execuções seguintes, o Playwright reutiliza esse estado.

**Não versionar esse arquivo no Git.**

### Deduplicação

Durante uma execução, o bot utiliza:

```java
Set<String> vagasProcessadas
```

O identificador utilizado é o `jobId` da plataforma.

A deduplicação entre execuções ainda precisa ser implementada no banco.

---

## Regras de negócio

Estas regras são fundamentais para a continuidade do projeto:

1. A IA não pode inventar experiências ou tecnologias.
2. O perfil é a fonte oficial dos dados do candidato.
3. O currículo completo deve ser considerado.
4. A resposta da análise deve ser estruturada.
5. Currículo e carta só devem ser gerados para matches acima do limite definido.
6. A automação coleta a vaga; o `MatchService` decide, gera e persiste.
7. Uma vaga só deve ser marcada como processada depois de sucesso.
8. Uma falha em uma vaga não deve interromper o processamento das demais.
9. O LaTeX deve ser estruturado pelo backend.
10. Senhas devem utilizar BCrypt.
11. Rotas protegidas exigem JWT.
12. O link original da vaga deve ser preservado.
13. A sessão do LinkedIn é informação sensível.
14. APIs estruturadas devem ser preferidas a scraping quando disponíveis.

---

## Estado atual

### Concluído / funcional

- [x] Estrutura backend Spring Boot
- [x] PostgreSQL
- [x] Persistência do perfil
- [x] Repository de perfil
- [x] Análise de vagas com Ollama
- [x] Spring AI
- [x] Resposta estruturada da IA
- [x] Pontuação de compatibilidade
- [x] Justificativa
- [x] Requisitos atendidos
- [x] Habilidades faltantes
- [x] Recomendação de candidatura
- [x] Persistência das vagas analisadas
- [x] Histórico básico no backend
- [x] Geração condicional de materiais
- [x] Geração textual de carta
- [x] Geração de currículo em LaTeX
- [x] Template LaTeX controlado pelo backend
- [x] Playwright
- [x] Sessão reutilizável do LinkedIn
- [x] Processamento de múltiplas vagas
- [x] Deduplicação durante a execução
- [x] Retry da IA
- [x] Logging
- [x] JWT
- [x] BCrypt
- [x] CORS
- [x] Frontend React + TypeScript
- [x] Login
- [x] Logout
- [x] Dashboard inicial
- [x] Tailwind CSS

### Em desenvolvimento / pendente

- [ ] Alinhar completamente frontend e backend
- [ ] Atualizar frontend de `/api/vagas/analisar` para `/api/match`
- [ ] Atualizar frontend de `response.text()` para `response.json()`
- [ ] Tipar a resposta como `VagaAnalisada`
- [ ] Exibir análise completa no dashboard
- [ ] Histórico no frontend
- [ ] Tela de perfil
- [ ] Associar cada vaga ao usuário autenticado
- [ ] Remover uso de ID fixo no `MatchService`
- [ ] Ocultar senha dos retornos JSON
- [ ] Deduplicação persistente no banco
- [ ] Validação dos DTOs
- [ ] Tratamento de `401` no frontend
- [ ] Filtros e paginação
- [ ] Processamento assíncrono
- [ ] Estado do processamento do bot
- [ ] Tratamento centralizado de exceções
- [ ] Migrações com Flyway

### Planejado

- [ ] Agendamento automático
- [ ] Busca periódica
- [ ] Candidatura assistida/automática
- [ ] Preenchimento automático de perguntas
- [ ] Identificação de etapas que exigem ação manual
- [ ] Integração final com Gupy
- [ ] Novas fontes de vagas
- [ ] Geração de PDF compilado
- [ ] Dashboard com métricas
- [ ] Filas/jobs
- [ ] Testes automatizados
- [ ] Suporte completo a múltiplos usuários
- [ ] Docker Compose completo com backend, frontend, banco e Ollama

---

## Próximo passo

O próximo passo técnico recomendado é **corrigir o contrato entre frontend e backend antes de adicionar novas funcionalidades visuais**.

Ordem recomendada:

1. Confirmar a rota real do `MatchController`.
2. Confirmar o JSON retornado por `POST /api/match`.
3. Atualizar `src/services/api.ts`.
4. Substituir `/api/vagas/analisar` por `/api/match`.
5. Substituir `Promise<string>` por `Promise<VagaAnalisada>`.
6. Substituir `response.text()` por `response.json()`.
7. Garantir `Authorization: Bearer ${token}`.
8. Atualizar `Dashboard.tsx` para trabalhar com o objeto completo.
9. Exibir:
   - porcentagem;
   - justificativa;
   - requisitos atendidos;
   - habilidades faltantes;
   - recomendação;
   - carta;
   - currículo/LaTeX.
10. Testar o fluxo completo pelo navegador.

Depois:

11. Remover o ID fixo do perfil.
12. Utilizar o usuário autenticado.
13. Criar associação entre `VagaAnalisada` e `Perfil`.
14. Criar histórico no frontend.
15. Criar tela de perfil.
16. Implementar deduplicação persistente.
17. Transformar a busca automática em job assíncrono.
18. Implementar agendamento depois que o processamento assíncrono estiver estável.

---

## Decisões técnicas

### Spring Boot como orquestrador

O backend centraliza:

- regras de negócio;
- banco;
- IA;
- automação;
- segurança;
- integrações;
- geração de documentos.

### PostgreSQL

Foi escolhido como banco persistente principal.

O H2 foi utilizado apenas durante a prototipagem.

### Ollama local

A IA local foi escolhida por:

- privacidade;
- ausência de custo por chamada;
- possibilidade de execução offline;
- controle do modelo;
- valor técnico para o projeto.

### Spring AI

O Spring AI abstrai a comunicação com o Ollama e fornece o `ChatClient`, prompts e conversão estruturada das respostas.

### IA em duas etapas

A análise e a geração de materiais são separadas para:

- reduzir processamento;
- evitar gerar currículo para vagas fracas;
- diminuir tempo de execução;
- facilitar tratamento de erros.

### JSON estruturado

A IA deve retornar dados estruturados em vez de texto livre para facilitar:

- persistência;
- regras condicionais;
- integração frontend/backend;
- processamento automático.

### LaTeX controlado pelo backend

A IA gera somente o conteúdo variável.

O template fica no Java.

Isso reduz problemas com:

- escape;
- JSON inválido;
- comandos LaTeX;
- alterações involuntárias de layout.

### APIs antes de scraping

Quando uma fonte fornece dados estruturados, a preferência é consumir a API em vez de renderizar e extrair o HTML.

---

## Segurança

O projeto utiliza:

- Spring Security;
- JWT;
- HMAC256;
- BCrypt;
- API stateless;
- filtro JWT;
- CORS.

Configuração documentada do JWT:

```text
Issuer: AutoJobMatch
Subject: e-mail do usuário
Algoritmo: HMAC256
Expiração aproximada: 2 horas
```

O frontend deve enviar:

```http
Authorization: Bearer <token>
```

A origem documentada para desenvolvimento é:

```text
http://localhost:5173
```

O backend utiliza:

```text
http://localhost:8080
```

---

## Observações importantes para desenvolvimento

### Senhas

Nunca salvar senha em texto simples.

O cadastro deve aplicar:

```java
perfil.setSenha(
    passwordEncoder.encode(perfil.getSenha())
);
```

### Perfil autenticado

Não manter ID de perfil fixo no `MatchService`.

O perfil deve ser obtido a partir do usuário autenticado/JWT.

### Vagas por usuário

Para uma aplicação multiusuário real, `VagaAnalisada` deve possuir associação com `Perfil`.

### Sessão do LinkedIn

O arquivo:

```text
linkedin-session.json
```

contém estado de autenticação e deve permanecer fora do versionamento.

### Contrato frontend/backend

O backend consolidado utiliza:

```text
POST /api/match
```

e retorna um objeto `VagaAnalisada`.

O frontend precisa trabalhar com JSON tipado.

---

## Status resumido

```text
Backend                 ████████████████████  Avançado
Banco                   ████████████████████  Funcional
IA / Ollama             ████████████████████  Funcional
Análise de vagas        ████████████████████  Funcional
Persistência            ████████████████████  Funcional
Playwright / LinkedIn   ███████████████████░  Avançado
Autenticação            ███████████████████░  Implementada
Frontend                ███████████████░░░░░  Em evolução
Integração Front/Back   ███████████░░░░░░░░  Pendente
Multiusuário            ███████░░░░░░░░░░░░  Pendente
Jobs / Agendamento      ████░░░░░░░░░░░░░░░  Planejado
Candidatura automática  ██░░░░░░░░░░░░░░░░░  Planejado
```

---

## Licença

Projeto em desenvolvimento. A licença ainda não está definida na documentação atual.
