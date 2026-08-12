# CONTEXTO DO PROJETO — AUTO JOB MATCH

## 1. Finalidade deste arquivo

Este documento serve para transferir o contexto do projeto **Auto Job Match** para outro chat ou outra IA, permitindo continuar o desenvolvimento do ponto exato em que o histórico terminou.

Ele foi construído a partir do histórico integral do projeto. Quando o histórico apresenta versões diferentes da mesma classe, rota ou estrutura, este documento diferencia:

- o que foi implementado e testado;
- o que foi alterado durante o desenvolvimento;
- o que ainda está inconsistente;
- o que foi apenas planejado;
- qual deve ser o próximo passo.

---

## 2. Resumo executivo

O Auto Job Match é uma aplicação full stack local para automatizar a busca e análise de vagas de tecnologia.

O sistema possui ou passou a desenvolver:

- frontend React + TypeScript + Vite;
- backend Java + Spring Boot;
- PostgreSQL em Docker;
- autenticação JWT com Spring Security e BCrypt;
- inteligência artificial local com Ollama e Spring AI;
- análise de compatibilidade entre currículo e vaga;
- persistência de vagas analisadas;
- geração condicional de currículo adaptado e carta de apresentação;
- geração de currículo em LaTeX;
- busca de vagas por fontes externas;
- automação do LinkedIn com Playwright;
- reutilização de sessão por `linkedin-session.json`;
- frontend inicial com login e dashboard manual.

O ponto exato em que o histórico termina é a **estilização das telas `Login.tsx` e `Dashboard.tsx` com Tailwind CSS**. O frontend ainda precisa ser alinhado ao contrato real do backend.

---

## 3. Objetivo funcional

O sistema deve:

1. armazenar o perfil profissional do usuário;
2. armazenar currículo, resumo, senioridade, habilidades e links;
3. localizar vagas automaticamente;
4. extrair título, empresa, descrição e link;
5. comparar a vaga com o perfil usando Ollama;
6. gerar uma nota de compatibilidade entre 0 e 100;
7. justificar a nota;
8. listar requisitos atendidos;
9. listar habilidades faltantes;
10. indicar se vale a pena aplicar;
11. para matches altos, gerar currículo adaptado e carta;
12. guardar os resultados no PostgreSQL;
13. exibir os dados no frontend;
14. futuramente executar buscas de forma agendada.

---

## 4. Tecnologias

### Backend

- Java 21 no ambiente do usuário;
- Spring Boot;
- Spring Web;
- Spring Data JPA;
- Hibernate;
- Spring Security;
- Auth0 Java JWT;
- Spring AI;
- Playwright para Java;
- Maven;
- Lombok em partes do projeto.

### Frontend

- React;
- TypeScript;
- Vite;
- Tailwind CSS v4 com `@tailwindcss/vite`;
- Fetch API;
- `localStorage` para JWT.

### Banco e infraestrutura

- PostgreSQL 15;
- Docker Compose;
- DBeaver;
- Ollama em `localhost:11434`;
- Chromium controlado pelo Playwright.

### Fontes de vagas

- LinkedIn por automação com Playwright;
- Gupy por endpoint JSON descoberto no tráfego do portal;
- Remotive como prova de conceito.

---

## 5. Estrutura geral esperada

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

Os nomes podem variar ligeiramente no código real. Conferir os arquivos antes de aplicar alterações.

---

## 6. Banco de dados

### Configuração principal

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/job_match_db
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Docker Compose utilizado como base

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

### DBeaver

- Host: `localhost`
- Porta: `5432`
- Database: `job_match_db`
- Usuário: `root`
- Senha: `root`

### Observação sobre H2

O H2 foi usado em uma etapa de testes, mas causou conflitos porque configurações de H2 e PostgreSQL ficaram simultaneamente no `application.properties`. O banco definitivo é o PostgreSQL. Remover configurações H2 se ainda existirem.

---

## 7. Modelo `Perfil`

O perfil começou simples e foi expandido.

Campos existentes ou discutidos:

- `Long id`;
- `String nome`;
- `String email`;
- `String senha`;
- `String telefone`;
- `String senioridade`;
- `String resumo`;
- `String curriculo`;
- `List<String> habilidades`;
- `String perfilLinkedin`;
- possível link do GitHub, conforme a versão do arquivo.

Campos longos devem usar `TEXT`.

A entidade passou a implementar `UserDetails`.

Regras:

- e-mail é o username;
- senha deve estar em BCrypt;
- autoridade padrão: `ROLE_USER`;
- conta, credenciais e usuário retornam ativos nas funções de `UserDetails`.

### Risco atual

Se o controller retorna `Perfil` diretamente, o hash da senha pode aparecer no JSON. O ideal é:

- usar `@JsonIgnore` no campo/getter da senha; ou
- criar DTOs de entrada e saída que não exponham senha.

---

## 8. Modelo `VagaAnalisada`

Campos existentes ou adicionados:

- `Long id`;
- `String titulo`;
- `String empresa`;
- `String descricaoCompleta`;
- `String linkVaga`;
- `Integer porcentagemMatch`;
- `String justificativa`;
- `Boolean valeApenaAplicar`;
- `List<String> requisitosAtendidos`;
- `List<String> habilidadesFaltantes`;
- `String curriculoGerado`;
- `String coverLetterGerada`.

Listas usam `@ElementCollection`.

Textos longos usam `@Column(columnDefinition = "TEXT")`.

### Pendência arquitetural

Não há confirmação de uma associação JPA entre `VagaAnalisada` e `Perfil`. Para multiusuário, adicionar algo como:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "perfil_id", nullable = false)
private Perfil perfil;
```

---

## 9. DTOs principais

### `VagaRequest`

Versão evoluída:

```java
public record VagaRequest(
    String titulo,
    String empresa,
    String descricaoCompleta,
    String linkVaga
) {}
```

Alguns pontos antigos ainda podem instanciar o record com três argumentos. Atualizar todos os usos para a versão final.

### `AnaliseMatch`

```java
public record AnaliseMatch(
    int porcentagemMatch,
    String justificativa,
    List<String> requisitosAtendidos,
    List<String> habilidadesFaltantes,
    boolean valeApenaAplicar
) {}
```

### `MateriaisCandidatura`

Houve duas abordagens.

Versão acoplada:

```java
public record MateriaisCandidatura(
    String coverLetter,
    String resumoProfissional,
    List<String> itensExperienciaZenit,
    List<String> itensExperienciaEducAZ
) {}
```

Essa versão omitia ou dificultava a experiência da SECOM e não é genérica.

Direção recomendada:

```java
public record MateriaisCandidatura(
    String coverLetter,
    String resumoProfissional,
    List<ExperienciaAdaptada> experiencias
) {}

public record ExperienciaAdaptada(
    String empresa,
    String cargo,
    String periodo,
    String local,
    List<String> bulletPoints
) {}
```

Verificar qual versão está no projeto antes de continuar.

---

## 10. Repositories

### `PerfilRepository`

Deve estar tipado:

```java
public interface PerfilRepository
    extends JpaRepository<Perfil, Long> {

    UserDetails findByEmail(String email);
}
```

O repository sem `<Perfil, Long>` causou erros de `Object` e type safety.

### `VagaAnalisadaRepository`

```java
public interface VagaAnalisadaRepository
    extends JpaRepository<VagaAnalisada, Long> {
}
```

Ainda falta, possivelmente:

- consulta por `jobId` ou `linkVaga` para deduplicação persistente;
- consulta por perfil;
- ordenação por porcentagem;
- paginação;
- filtros por recomendação.

---

## 11. `MatchService`

É o serviço central.

### Dependências

- `ChatClient`;
- `PerfilRepository`;
- `VagaAnalisadaRepository`.

### Fluxo de `analisarCompatibilidade(VagaRequest vaga)`

1. buscar o perfil;
2. montar prompt com perfil e vaga;
3. chamar Ollama;
4. converter para `AnaliseMatch`;
5. preencher `VagaAnalisada`;
6. se `porcentagemMatch > 70`, gerar materiais;
7. montar LaTeX no backend;
8. salvar no PostgreSQL;
9. retornar a entidade.

### Problema atual importante

Durante o histórico, o perfil foi buscado por ID fixo (`1L` e depois `2L`). Isso deve ser removido.

A solução correta é obter o usuário autenticado:

```java
Authentication authentication =
    SecurityContextHolder.getContext().getAuthentication();

String email = authentication.getName();
Perfil perfil = perfilRepository.findPerfilByEmail(email)
    .orElseThrow(...);
```

Para isso, é recomendável alterar o repository para retornar `Optional<Perfil>` em método separado.

### Regra de match

A condição usada foi:

```java
if (analiseDaIA.porcentagemMatch() > 70)
```

Confirmar se o requisito desejado é `> 70` ou `>= 70`.

### Resposta da IA

Configuração:

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3
spring.ai.ollama.chat.options.format=json
spring.ai.ollama.chat.options.temperature=0.2
```

A IA deve retornar somente JSON compatível com o record.

### LaTeX

Não pedir para a IA gerar o documento inteiro. Manter o template no Java e injetar textos gerados.

---

## 12. Autenticação e segurança

### Login

Rota:

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

### JWT

- biblioteca Auth0 Java JWT;
- algoritmo HMAC256;
- issuer: `AutoJobMatch`;
- subject: e-mail;
- expiração aproximada: duas horas;
- API stateless.

### BCrypt

Há um bean `PasswordEncoder` com `BCryptPasswordEncoder`.

Senhas antigas em texto simples não funcionam no login. Elas devem ser convertidas para hash BCrypt.

### `SecurityFilter`

O filtro:

1. lê `Authorization`;
2. verifica `Bearer `;
3. valida o JWT;
4. recupera o e-mail;
5. carrega o usuário;
6. preenche o `SecurityContext`.

### Rotas públicas

Confirmadas ou discutidas:

- `POST /auth/login`;
- rota de cadastro do perfil;
- `OPTIONS /**`;
- temporariamente `/api/bot/buscar` durante testes.

### Inconsistência atual

A segurança liberava `/perfis/cadastrar`, mas o controller usava `/api/perfis`. Alinhar o matcher com a rota real.

### CORS

Permitir:

- origem `http://localhost:5173`;
- métodos GET, POST, PUT, DELETE e OPTIONS;
- cabeçalhos `Authorization` e `Content-Type`;
- credenciais, se mantidas na configuração.

---

## 13. Endpoints conhecidos

### Perfis

```http
POST /api/perfis
GET  /api/perfis
```

### Login

```http
POST /auth/login
```

### Match

```http
POST /api/match
GET  /api/match
```

`POST /api/match` recebe `VagaRequest` e retorna `VagaAnalisada`.

`GET /api/match` lista o histórico.

### Busca Remotive

```http
POST /api/match/buscar-web
```

Prova de conceito síncrona.

### Bot LinkedIn

Caminhos discutidos:

```text
/api/bot/gerar-sessao
/api/bot/buscar
```

Confirmar método HTTP no `BotController` real.

### Divergência do frontend

O frontend antigo chama:

```text
/api/vagas/analisar
```

Mas o backend consolidado usa:

```text
/api/match
```

Ajustar imediatamente.

---

## 14. Automação do LinkedIn

### Dependência

Playwright para Java.

### Sessão

Método `gerarSessaoLinkedin()`:

1. abre o login em navegador visível;
2. usuário faz login manualmente;
3. espera cerca de 60 segundos;
4. salva `linkedin-session.json`.

Esse arquivo é sensível e deve estar no `.gitignore`.

### Busca

A automação:

1. carrega a sessão;
2. abre a URL de busca;
3. espera os cards;
4. encontra `li[data-occludable-job-id]`;
5. obtém `jobId`;
6. relocaliza o card pelo ID;
7. faz scroll e hover;
8. clica;
9. extrai título, empresa, descrição e link;
10. cria `VagaRequest`;
11. chama `MatchService` com retry;
12. marca como processada somente após sucesso;
13. continua;
14. faz scroll para carregar novas vagas;
15. encerra após tentativas sem novidades.

### Seletores

- cards: `li[data-occludable-job-id]`;
- descrição principal: `#job-details`;
- título: `h1` ou seletor específico da top card;
- empresa: contêiner `.job-details-jobs-unified-top-card__primary-description`, sem exigir tag `a`.

### Deduplicação

Existe em memória:

```java
Set<String> vagasProcessadas = new HashSet<>();
```

Ainda falta deduplicação no banco entre execuções.

### Logs

A automação foi refatorada de `System.out.println` para SLF4J.

### Retry

O método de retry deve relançar a exceção após a última tentativa. Caso contrário, uma falha pode ser contada como sucesso.

---

## 15. Integração com Gupy

Endpoint identificado:

```text
https://employability-portal.gupy.io/api/v1/jobs
```

Parâmetros:

- `jobName`;
- `limit`;
- `offset`.

Mapeamento relevante:

- `name` → título;
- `careerPageName` → empresa;
- `description` → descrição;
- `jobUrl` → link.

A decisão técnica foi consumir o JSON diretamente, evitando Playwright para Gupy.

Não há confirmação de que essa integração tenha sido finalizada no pipeline atual. Verificar se existe um serviço concreto no backend.

---

## 16. Integração com Remotive

Foi implementada como prova de conceito.

Fluxo:

1. chamar API da Remotive;
2. limitar a poucos resultados;
3. transformar em `VagaRequest`;
4. analisar com `MatchService`;
5. persistir.

Essa integração demonstra o padrão correto para novas fontes, mas não é a fonte principal do projeto.

---

## 17. Frontend atual

### Arquivos principais

```text
src/main.tsx
src/App.tsx
src/index.css
src/pages/Login.tsx
src/pages/Dashboard.tsx
src/services/api.ts
```

### Tailwind

Instalação adotada:

```bash
npm install tailwindcss @tailwindcss/vite
```

`vite.config.ts`:

```ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [react(), tailwindcss()]
});
```

`src/index.css`:

```css
@import "tailwindcss";
```

`App.css` foi removido ou deve deixar de ser importado.

### `App.tsx`

Mantém estados simples:

- `autenticado`;
- `carregando`.

Na inicialização, verifica se existe token no `localStorage`.

Não há React Router nem validação imediata da expiração do token.

### `Login.tsx`

Já possui:

- formulário estilizado;
- e-mail;
- senha;
- loading;
- mensagem de erro;
- chamada `api.login()`;
- armazenamento do token;
- callback de sucesso.

### `Dashboard.tsx`

Ponto final do histórico.

A tela atual:

- possui navbar;
- botão de logout;
- textarea para colar descrição da vaga;
- botão para chamar a IA;
- painel para mostrar código LaTeX;
- botão para copiar o código;
- visual estilizado com Tailwind.

O dashboard cria um payload provisório com título e empresa genéricos:

```ts
const vagaMock: VagaPayload = {
  titulo: 'Desenvolvedor Analisado pela IA',
  empresa: 'Empresa da Vaga',
  descricaoCompleta: vagaTexto
};
```

### Problemas atuais do frontend

1. a rota em `api.ts` provavelmente ainda é `/api/vagas/analisar`, mas deve ser `/api/match`;
2. `api.gerarCurriculo()` provavelmente retorna `Promise<string>` e usa `response.text()`;
3. o backend retorna JSON de `VagaAnalisada`, não uma string;
4. o payload pode estar sem `linkVaga`;
5. o header deve ser `Bearer ${token}`, com espaço;
6. o dashboard só mostra LaTeX e não mostra score, justificativa, requisitos ou carta;
7. o estado considera qualquer token como válido;
8. não há tratamento global de 401;
9. não há tela de histórico;
10. não há tela de perfil.

---

## 18. Contrato TypeScript recomendado

Criar em `api.ts` ou `types`:

```ts
export interface VagaPayload {
  titulo: string;
  empresa: string;
  descricaoCompleta: string;
  linkVaga?: string;
}

export interface VagaAnalisada {
  id: number;
  titulo: string;
  empresa: string;
  descricaoCompleta: string;
  linkVaga?: string;
  porcentagemMatch: number;
  justificativa: string;
  valeApenaAplicar: boolean;
  requisitosAtendidos: string[];
  habilidadesFaltantes: string[];
  curriculoGerado?: string;
  coverLetterGerada?: string;
}
```

A chamada deve ser semelhante a:

```ts
async analisarVaga(
  dados: VagaPayload
): Promise<VagaAnalisada> {
  const token = localStorage.getItem('token');

  const response = await fetch(
    `${BASE_URL}/api/match`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(dados)
    }
  );

  if (response.status === 401) {
    localStorage.removeItem('token');
    throw new Error('Sessão expirada');
  }

  if (!response.ok) {
    throw new Error('Erro ao analisar vaga');
  }

  return response.json();
}
```

---

## 19. Bugs já encontrados e lições

### Backend

- repository sem tipos genéricos;
- assinatura malformada de controller;
- configurações H2 e PostgreSQL misturadas;
- driver H2 ausente;
- perfil buscado por ID fixo;
- resposta da IA fora do JSON;
- LaTeX quebrando JSON;
- campos de currículo/carta ausentes na entidade;
- exemplo conceitual usando métodos/classes inexistentes;
- rotas públicas divergentes;
- senha em texto simples incompatível com BCrypt;
- classe `GerarHash` fora do classpath.

### Playwright

- timeout em seletor da empresa;
- seletor exigia tag `a` inexistente;
- URL alterada;
- descrição ainda não carregada;
- DOM virtualizado;
- locators antigos após clique;
- somente primeira vaga processada;
- duplicidades após scroll;
- vaga marcada como processada antes do sucesso;
- retry ocultando falha final.

### Frontend

- `npm run dev` sem script/pasta errada;
- Tailwind configurado pela abordagem antiga;
- CSS padrão do Vite interferindo;
- import relativo de `api.ts` potencialmente errado;
- CORS;
- header JWT sem espaço;
- rota diferente do backend;
- frontend esperando texto e backend retornando JSON.

---

## 20. Estado atual do projeto

### O que está funcional ou foi testado no histórico

- inicialização do Spring Boot;
- conexão PostgreSQL;
- criação de entidade e repository de perfil;
- cadastro e listagem básica de perfil;
- integração Ollama/Spring AI;
- análise de compatibilidade estruturada;
- persistência de vagas;
- histórico básico pelo backend;
- geração condicional de currículo e carta;
- construção de LaTeX no Java;
- login com JWT;
- BCrypt;
- filtro de segurança;
- CORS;
- geração da sessão do LinkedIn;
- busca e extração de vagas com Playwright;
- processamento de várias vagas;
- deduplicação em memória;
- retry e logs;
- frontend React inicial;
- tela de login;
- dashboard manual;
- Tailwind CSS.

### O que não está confirmado como finalizado

- integração Gupy completa no pipeline;
- compilação LaTeX para PDF;
- candidatura automática;
- busca agendada;
- tela de histórico;
- tela de edição do perfil;
- associação de vagas ao usuário;
- deduplicação persistente;
- processamento assíncrono;
- múltiplos usuários isolados corretamente;
- contrato frontend/backend totalmente alinhado.

---

## 21. Próximo passo exato

O próximo passo recomendado é **corrigir o contrato entre frontend e backend** antes de criar novas telas.

Ordem:

1. abrir `MatchController` e confirmar a rota real;
2. confirmar o JSON de resposta de `POST /api/match`;
3. abrir `src/services/api.ts`;
4. trocar `/api/vagas/analisar` por `/api/match`;
5. trocar `Promise<string>` por `Promise<VagaAnalisada>`;
6. trocar `response.text()` por `response.json()`;
7. garantir `Authorization: Bearer ${token}`;
8. atualizar `Dashboard.tsx` para armazenar o objeto completo;
9. exibir:
   - porcentagem;
   - justificativa;
   - requisitos atendidos;
   - habilidades faltantes;
   - indicação de candidatura;
   - carta;
   - LaTeX;
10. testar o fluxo completo pelo navegador.

Depois disso:

11. remover o ID fixo no `MatchService` e usar o usuário autenticado;
12. criar associação entre vaga e perfil;
13. implementar `GET /api/match` no frontend como tela de histórico;
14. criar tela de perfil;
15. implementar deduplicação persistente;
16. transformar a busca automática em job assíncrono;
17. adicionar agendamento apenas depois de o job assíncrono estar estável.

---

## 22. Melhorias prioritárias

### Alta prioridade

- alinhar rota e resposta frontend/backend;
- remover perfil por ID fixo;
- ocultar senha nos JSONs;
- vincular vagas ao usuário;
- validar DTOs com Bean Validation;
- tratar 401 no frontend;
- guardar `linkedin-session.json` fora do Git;
- adicionar deduplicação no banco.

### Média prioridade

- histórico no dashboard;
- tela de perfil;
- filtros de busca;
- paginação;
- estado de processamento do bot;
- tratamento centralizado de exceções;
- logs estruturados;
- migrações Flyway.

### Futuro

- geração de PDF;
- filas e jobs;
- agendamento;
- integração final com Gupy;
- novas fontes;
- métricas;
- candidatura assistida;
- testes automatizados;
- Docker Compose com backend, frontend, banco e Ollama.

---

## 23. Regras que não devem ser quebradas

1. A IA não pode inventar experiências ou tecnologias.
2. O perfil é a fonte oficial dos dados do candidato.
3. O currículo completo deve ser considerado.
4. A resposta de análise deve ser estruturada.
5. Currículo e carta só são gerados quando o match ultrapassa o limite definido.
6. A automação apenas coleta a vaga; o `MatchService` decide, gera e persiste.
7. Uma vaga só é marcada como processada após sucesso.
8. Uma falha não deve interromper o processamento das demais vagas.
9. O LaTeX deve ser estruturado pelo backend, não integralmente pela IA.
10. Senhas devem usar BCrypt.
11. Rotas protegidas exigem JWT.
12. O bot deve preservar o link da vaga.
13. A sessão do LinkedIn é sensível.
14. API estruturada deve ser preferida a scraping quando disponível.

---

## 24. Instrução para a próxima IA

Antes de alterar código:

1. peça ou leia os arquivos reais do projeto atual;
2. não assuma que todos os exemplos do histórico foram copiados literalmente;
3. confirme os nomes atuais de pacotes, classes, rotas e campos;
4. preserve as decisões arquiteturais descritas neste documento;
5. comece pelo alinhamento do frontend com `POST /api/match`;
6. não crie métodos como `calcularScoreDeMatch` ou classes como `Candidatura` sem necessidade, pois o fluxo já pertence ao `MatchService`;
7. não volte a pedir para a IA gerar o LaTeX inteiro;
8. não use ID de perfil fixo na solução definitiva.

---

## 25. Ponto de retomada em uma frase

**O backend já possui o núcleo de perfil, autenticação, análise com Ollama, persistência, geração de materiais e automação Playwright; o frontend acabou de receber uma interface Tailwind de login e dashboard, mas agora precisa ser conectado corretamente ao contrato JSON real de `POST /api/match` e evoluído para mostrar toda a análise.**
