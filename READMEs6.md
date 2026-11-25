# Trabalho API — Cadastro e listagem de usuários

API REST em Java e Spring Boot com PostgreSQL. O projeto reúne duas atividades:

- **Atividade 1 — Cadastro e tratamento de erros.** Validação de nome e e-mail, tratamento global de exceções, respostas de erro em JSON e registro de falhas internas no servidor.
- **Atividade 2 — Listagem paginada.** Consulta de usuários com paginação, ordenação, filtros por texto e intervalo de IDs, metadados e links de navegação.

## Como executar

O projeto usa Java 21, Maven, Spring Boot 3.5.7, Spring Data JPA, Lombok e PostgreSQL. Os comandos abaixo usam PowerShell e devem ser executados na pasta que contém o `pom.xml`. Se `psql` ou `mvn` não estiverem no `PATH`, use o caminho completo do executável instalado na sua máquina.

### 1. Preparar o PostgreSQL

Com o PostgreSQL em execução, crie o banco caso ele ainda não exista:

```powershell
psql -h 127.0.0.1 -p 5432 -U postgres -d postgres -c "CREATE DATABASE trabalho;"
```

Execute o script de criação da tabela:

```powershell
psql -h 127.0.0.1 -p 5432 -U postgres -d trabalho -f criar_usuarios.sql
```

O script cria a tabela `usuarios`. O banco gera o `id` de cada cadastro e aplica a restrição `uk_usuarios_email` para impedir que o mesmo e-mail seja cadastrado duas vezes.

### 2. Configurar a conexão

No PowerShell, informe o endereço do banco, o usuário e a senha. Ajuste os valores se a sua instalação usar outro endereço, porta ou usuário.

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://127.0.0.1:5432/trabalho'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Senha do PostgreSQL' -AsSecureString)).Password
```

A senha é digitada no terminal e não precisa ser salva no repositório.

### 3. Iniciar a API

No mesmo terminal, com JDK 21, execute:

```powershell
mvn spring-boot:run
```

Se estiver usando **JDK 26**, este foi o comando usado nos testes do projeto para compilar com uma versão compatível do Lombok:

```powershell
mvn '-Dlombok.version=1.18.46' '-Dmaven.compiler.proc=full' spring-boot:run
```

A API fica disponível em `http://localhost:8080`. Para encerrá-la, pressione `Ctrl+C`.

## Rotas

| Método | Rota | Função |
|---|---|---|
| `POST` | `/usuarios` | Valida os dados e cadastra um usuário. |
| `GET` | `/usuarios` | Lista os usuários com paginação, filtros e ordenação. |

As rotas estão em `src/main/java/com/trabalho/controller/UsuarioController.java`. A configuração da aplicação fica em `src/main/resources/application.properties`, e o script do banco está em `criar_usuarios.sql`.

## Atividade 1 — Cadastro e tratamento global de erros

No Postman, selecione **POST**, use `http://localhost:8080/usuarios` e escolha **Body → raw → JSON**. Envie:

```json
{
  "nome": "Ana",
  "email": "ana@exemplo.com"
}
```

Para um e-mail novo, a resposta é **201 Created**. O nome e o e-mail são obrigatórios. A API verifica os campos antes de tentar inserir o registro no banco. Se o e-mail já estiver cadastrado, a aplicação lança `EmailJaCadastradoException`, e o tratador global devolve **409 Conflict**.

### Formato das respostas de erro

O status HTTP indica o resultado da requisição. No corpo, `error` identifica o tipo de falha, `message` descreve o problema e `details` informa quais campos precisam de correção. Por exemplo, um e-mail inválido gera:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Os dados fornecidos são inválidos.",
  "details": [
    {
      "field": "email",
      "message": "Informe um e-mail válido."
    }
  ],
  "timestamp": "2026-11-25T18:00:00Z"
}
```

| Cenário para testar no Postman | Status esperado | Código no JSON |
|---|---|---|
| Enviar `{"nome":"","email":"sem-arroba"}` | `400 Bad Request` | `VALIDATION_ERROR` |
| Enviar um JSON com a vírgula entre os campos faltando | `400 Bad Request` | `INVALID_JSON` |
| Acessar `GET /rota-inexistente` | `404 Not Found` | `NOT_FOUND` |
| Enviar duas vezes o mesmo cadastro válido | `201` no primeiro envio e `409 Conflict` no segundo | `EMAIL_ALREADY_EXISTS` no segundo |
| Ocorrer uma falha interna inesperada | `500 Internal Server Error` | `INTERNAL_ERROR` |

No teste de erro 500, foi simulada uma exceção temporária no controller. O Postman recebeu a mensagem “Erro interno no servidor. Tente novamente mais tarde.”, e o terminal registrou a exceção e o stack trace. Depois do teste, o fluxo de cadastro foi restaurado.

### Reflexão — Por que proteger o stack trace e padronizar os erros?

O stack trace pode revelar nomes de classes, caminhos de arquivos, bibliotecas e detalhes de implementação. Uma pessoa pode usar essas informações para identificar tecnologias e procurar vulnerabilidades específicas. Por isso, a API registra os detalhes técnicos no servidor e envia ao cliente uma mensagem adequada ao problema.

O formato consistente permite que o frontend trate as falhas pelo código `error`. Quando recebe `VALIDATION_ERROR`, por exemplo, a tela pode percorrer `details` e mostrar a mensagem ao lado do campo correspondente. Quando recebe `EMAIL_ALREADY_EXISTS`, pode pedir outro e-mail. Esse comportamento continua funcionando mesmo que o texto de uma mensagem seja revisado.

## Atividade 2 — Paginação, ordenação e filtros

No Postman, selecione **GET** e deixe **Body → none**. Envie os parâmetros na URL.

| Parâmetro | Função | Padrão |
|---|---|---|
| `page` | Página solicitada, começando em 1 | `1` |
| `limit` | Itens por página. Valores acima de 100 são limitados a 100 | `10` |
| `sort` | Campo de ordenação. Aceita `id`, `nome` ou `email` | `id` |
| `order` | Direção da ordenação. Aceita `asc` ou `desc` | `asc` |
| `nome` | Busca por trecho do nome, sem diferenciar maiúsculas de minúsculas | Sem filtro |
| `email` | Busca por trecho do e-mail, sem diferenciar maiúsculas de minúsculas | Sem filtro |
| `minId` | Menor ID incluído na busca | Sem limite inferior informado |
| `maxId` | Maior ID incluído na busca | Sem limite superior informado |

Os filtros podem ser combinados. O banco aplica os filtros e a ordenação antes de selecionar os registros da página. Quando há empate na ordenação por nome ou e-mail, o ID determina a ordem entre os registros empatados.

### Exemplos para testar no Postman

| Objetivo | Requisição |
|---|---|
| Buscar a primeira página com um usuário | `GET /usuarios?page=1&limit=1` |
| Ordenar por nome em ordem decrescente | `GET /usuarios?sort=nome&order=desc` |
| Combinar filtros de nome e e-mail | `GET /usuarios?nome=ana&email=exemplo` |
| Buscar IDs de 1 a 5, incluindo os dois limites | `GET /usuarios?minId=1&maxId=5` |
| Buscar a partir do ID 2 | `GET /usuarios?minId=2` |
| Testar um intervalo invertido e receber 400 | `GET /usuarios?minId=5&maxId=2` |

Use `http://localhost:8080` antes da rota. Uma busca válida sem correspondências retorna **200 OK**, com `data: []` e `totalItems: 0`.

### Formato da resposta e links de navegação

Exemplo de `GET /usuarios?page=1&limit=1` em uma base com três usuários:

```json
{
  "data": [
    {
      "id": 1,
      "nome": "Ana",
      "email": "ana@exemplo.com"
    }
  ],
  "meta": {
    "totalItems": 3,
    "itemsPerPage": 1,
    "currentPage": 1,
    "totalPages": 3,
    "next": "http://localhost:8080/usuarios?page=2&limit=1",
    "prev": null,
    "first": "http://localhost:8080/usuarios?page=1&limit=1",
    "last": "http://localhost:8080/usuarios?page=3&limit=1"
  }
}
```

`data` contém os usuários da página atual. Em `meta`, `totalItems` conta os usuários que atendem aos filtros, e `itemsPerPage` indica o limite aplicado. Os links `first`, `prev`, `next` e `last` preservam os filtros e a ordenação da consulta. Para testar a navegação no Postman, copie a URL de `next` e envie outro `GET`. Na primeira página, `prev` é `null`. Na última, `next` é `null`.

### Reflexão — Por que paginar e retornar metadados?

Uma consulta que retorna todos os registros pode consumir muita memória no servidor durante a leitura e a criação do JSON. Com uma tabela grande ou vários acessos simultâneos, isso pode esgotar a memória disponível. Uma resposta extensa também ocupa mais banda e exige mais processamento do navegador, mesmo quando a tela só precisa mostrar alguns usuários.

Com metadados previsíveis, o frontend consegue exibir “Página 1 de 3”, mostrar quantos registros atendem à busca e habilitar ou desabilitar os botões de navegação. Como os links preservam os parâmetros da consulta, o usuário pode mudar de página sem perder os filtros. Outras listagens que adotem o mesmo formato podem reutilizar o mesmo componente de paginação.