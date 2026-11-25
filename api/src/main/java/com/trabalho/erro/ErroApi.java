package com.trabalho.erro;

import java.time.Instant;
import java.util.List;

// Define os campos que toda resposta de erro terá no JSON.
public record ErroApi(
        int status,            // Código HTTP, como 400, 404 ou 500.
        String error,          // Código que o frontend pode usar, como VALIDATION_ERROR.
        String message,        // Mensagem geral para quem usa a API.
        List<Detalhe> details, // Lista de problemas encontrados nos campos.
        Instant timestamp     // Momento em que a resposta de erro foi gerada.
) {
    // Cada detalhe informa qual campo falhou e por quê.
    public record Detalhe(String field, String message) {
    }
}