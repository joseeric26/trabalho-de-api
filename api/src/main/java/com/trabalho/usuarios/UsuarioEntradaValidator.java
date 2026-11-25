package com.trabalho.usuarios;

import org.springframework.validation.Validator;

public class UsuarioEntradaValidator {

    // Cria um validador para os dados de UsuarioEntrada.
    // entrada contém os dados recebidos. erros guarda os problemas encontrados.
    public static final Validator VALIDADOR =
            Validator.forInstanceOf(UsuarioEntrada.class, (entrada, erros) -> {
                // null significa que o nome veio ausente ou nulo.
                // isBlank() identifica texto vazio ou composto só por espaços.
                if (entrada.nome() == null || entrada.nome().isBlank()) {
                    erros.rejectValue("nome", "obrigatorio", "Informe o nome.");
                }

                String email = entrada.email();

                if (email == null || email.isBlank()) {
                    erros.rejectValue("email", "obrigatorio", "Informe o e-mail.");
                } else if (!email.matches("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+")) {
                    // Verifica o formato básico do e-mail, como ana@exemplo.com.
                    erros.rejectValue("email", "formato", "Informe um e-mail válido.");
                }
            });
}
