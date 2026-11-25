# API de cadastro de usuários

API REST desenvolvida como atividade de validação de dados e tratamento global de erros. O serviço cadastra usuários com nome e e-mail, persiste os dados no PostgreSQL e responde com erros JSON padronizados.

As atividades estão referenciadas nos nomes dos arquivos.

**Repositório:** [github.com/joseeric26/trabalho-de-api](https://github.com/joseeric26/trabalho-de-api)

## Tecnologias

- Java 21
- Spring Boot 3.5.7
- Spring Web e Spring Data JPA
- PostgreSQL
- Maven
- H2 para testes

## Requisitos

- JDK 21
- Maven
- PostgreSQL

## Configuração e execução

1. Crie um banco de dados PostgreSQL, por exemplo:

   ```sql
   CREATE DATABASE clinica;
   ```

2. Crie a tabela de usuários executando o script [api/criar_usuarios.sql](./api/criar_usuarios.sql) no banco `clinica`. Ele define as colunas obrigatórias e a restrição de unicidade do e-mail.

3. Configure a conexão por variáveis de ambiente. O Spring Boot reconhece estas propriedades:

   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/clinica
   export SPRING_DATASOURCE_USERNAME=seu_usuario
   export SPRING_DATASOURCE_PASSWORD=sua_senha
   ```

   Não coloque credenciais no código nem as publique no repositório.

4. Inicie a API:

   ```bash
   cd api
   mvn spring-boot:run
   ```

Por padrão, a aplicação fica disponível em `http://localhost:8080`.

## Endpoint

### Cadastrar usuário

`POST /usuarios`

Envie um corpo JSON com os campos obrigatórios `nome` e `email`:

```json
{
  "nome": "Ana",
  "email": "ana@exemplo.com"
}
```

Exemplo com cURL:

```bash
curl -i http://localhost:8080/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","email":"ana@exemplo.com"}'
```

Quando o cadastro é aceito, a API responde com `201 Created` e retorna os dados enviados:

```json
{
  "nome": "Ana",
  "email": "ana@exemplo.com"
}
```

O nome não pode estar ausente ou em branco; o e-mail é obrigatório e deve ter um formato válido. A validação acontece antes da gravação no banco.

## Respostas de erro

As respostas de erro seguem o mesmo formato:

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
  "timestamp": "2026-11-25T18:33:30Z"
}
```

| Situação | HTTP | Código `error` |
| --- | ---: | --- |
| Dados obrigatórios ausentes ou inválidos | 400 | `VALIDATION_ERROR` |
| JSON malformado ou corpo ilegível | 400 | `INVALID_JSON` |
| Rota inexistente | 404 | `NOT_FOUND` |
| E-mail já cadastrado | 409 | `EMAIL_ALREADY_EXISTS` |
| Erro inesperado do servidor | 500 | `INTERNAL_ERROR` |
| Outra requisição inválida reconhecida pelo Spring | 4xx correspondente | `REQUEST_ERROR` |

O tratamento centralizado está em `TratadorGlobalErros`. A exceção customizada `EmailJaCadastradoException` representa a regra de negócio de e-mail único. Erros inesperados são registrados no log do servidor com o stack trace; a resposta ao cliente contém apenas uma mensagem genérica, sem detalhes internos.

## Capturas dos testes

As imagens registram requisições feitas localmente durante o desenvolvimento. A captura de cadastro válido exibe `200 OK`, mas a implementação atual retorna `201 Created`, conforme documentado acima.

### Cadastro válido

![Envio de um usuário com nome e e-mail válidos. A captura é de uma execução anterior que retornava 200.](<./imagens trabalho semana 5/Captura de tela 2026-11-25 152150.png>)

### Dados inválidos

![Tentativa de cadastro com nome vazio e e-mail inválido, rejeitada com status 400. A captura é anterior à padronização atual do corpo de erro.](<./imagens trabalho semana 5/Captura de tela 2026-11-25 152125.png>)

### JSON malformado

![JSON malformado rejeitado com status 400 e erro INVALID_JSON.](<./imagens trabalho semana 5/Captura de tela 2026-11-25 153413.png>)

### Corpo vazio

![Requisição sem corpo rejeitada com status 400 e resposta de erro padronizada.](<./imagens trabalho semana 5/Captura de tela 2026-11-25 153811.png>)

### Erro interno

![Erro interno retornado como status 500 sem expor o stack trace ao cliente.](<./imagens trabalho semana 5/Captura de tela 2026-11-25 154555.png>)

### E-mail duplicado

![Tentativa de cadastrar e-mail existente respondida com 409 Conflict.](<./imagens trabalho semana 5/Captura de tela 2026-11-25 160204.png>)

## Testes

Na pasta `api`, execute:

```bash
mvn test
```

## Estrutura principal

```text
api/
├── criar_usuarios.sql
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/trabalho/
    │   │   ├── controller/UsuarioController.java
    │   │   ├── erro/                    # Formato, exceção customizada e tratador global
    │   │   ├── model/Usuario.java
    │   │   ├── repository/UsuarioRepository.java
    │   │   └── usuarios/                # Entrada e validação
    │   └── resources/application.properties
    └── test/
```

## Reflexão

Expor o *stack trace* diretamente ao usuário final é uma falha de segurança porque pode revelar a estrutura de pastas, nomes de classes, bibliotecas e detalhes da implementação. Essas informações ajudam a identificar as tecnologias usadas e possíveis vulnerabilidades do servidor. Por isso, erros internos devem ser registrados apenas nos logs da equipe, enquanto o cliente recebe uma mensagem genérica e segura.

Um padrão consistente de erros permite que o frontend interprete o código `error` e os campos de `details` para reagir automaticamente — por exemplo, mostrar a mensagem de validação junto ao campo correspondente ou orientar o usuário a escolher outro e-mail. Assim, quem consome a API não precisa adivinhar o formato ou inferir o tipo de falha a partir de mensagens variáveis.
