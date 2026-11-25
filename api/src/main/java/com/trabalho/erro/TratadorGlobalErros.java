package com.trabalho.erro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.ErrorResponse;

// Registra esta classe para tratar exceções dos controllers da API.
@RestControllerAdvice
public class TratadorGlobalErros {

    private static final Logger LOGGER = LoggerFactory.getLogger(TratadorGlobalErros.class);

    // O Spring chama este metodo quando a validação dos dados da rota falha.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApi> tratarValidacao(MethodArgumentNotValidException excecao) {
        List<ErroApi.Detalhe> detalhes = new ArrayList<>();

        // Copia o nome e a mensagem de cada campo inválido para a resposta.
        for (FieldError erro : excecao.getBindingResult().getFieldErrors()) {
            detalhes.add(new ErroApi.Detalhe(erro.getField(), erro.getDefaultMessage()));
        }

        ErroApi resposta = new ErroApi(
                400,
                "VALIDATION_ERROR",
                "Os dados fornecidos são inválidos.",
                detalhes,
                Instant.now()
        );

        // badRequest() define o status HTTP 400. body() envia o ErroApi como JSON.
        return ResponseEntity.badRequest().body(resposta);
    }
    // O Spring chama este metodo quando não consegue ler o corpo da requisição.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroApi> tratarJsonInvalido() {
        ErroApi resposta = new ErroApi(
                400,
                "INVALID_JSON",
                "Envie um corpo JSON válido com os campos esperados.",
                List.of(), // Gera uma lista vazia em details.
                Instant.now()
        );

        return ResponseEntity.badRequest().body(resposta);
    }
    // Usa a mesma resposta para as duas exceções de rota ou recurso inexistente.
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErroApi> tratarRotaNaoEncontrada() {
        ErroApi resposta = new ErroApi(
                404,
                "NOT_FOUND",
                "A rota solicitada não foi encontrada.",
                List.of(), // Uma rota inexistente não tem campos inválidos.
                Instant.now()
        );

        // Define o status HTTP 404 e envia o corpo no formato ErroApi.
        return ResponseEntity.status(404).body(resposta);
    }
    // O Spring usa este metodo quando não encontra um tratamento mais específico.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroApi> tratarErroInesperado(Exception excecao) {
        // Preserva os erros de requisição que o Spring já classificou como 4xx.
        if (excecao instanceof ErrorResponse erro && erro.getStatusCode().is4xxClientError()) {
            ErroApi resposta = new ErroApi(
                    erro.getStatusCode().value(),
                    "REQUEST_ERROR",
                    "A requisição não pôde ser processada.",
                    List.of(),
                    Instant.now()
            );

            return ResponseEntity.status(erro.getStatusCode())
                    .headers(erro.getHeaders())
                    .body(resposta);
        }

        // Registra a exceção e o stack trace no terminal do servidor.
        LOGGER.error("Erro inesperado ao processar uma requisição.", excecao);

        // Envia ao cliente uma mensagem genérica, sem detalhes internos.
        ErroApi resposta = new ErroApi(
                500,
                "INTERNAL_ERROR",
                "Erro interno no servidor. Tente novamente mais tarde.",
                List.of(),
                Instant.now()
        );

        return ResponseEntity.internalServerError().body(resposta);
    }
    // O Spring chama este método quando a regra de e-mail único é violada.
    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroApi> tratarEmailDuplicado(EmailJaCadastradoException excecao) {
        ErroApi resposta = new ErroApi(
                409,
                "EMAIL_ALREADY_EXISTS",
                excecao.getMessage(), // Usa a mensagem fixa que você escreveu no super(...).
                List.of(),
                Instant.now()
        );

        // Define o status HTTP 409 Conflict e envia o JSON padronizado.
        return ResponseEntity.status(409).body(resposta);
    }
}