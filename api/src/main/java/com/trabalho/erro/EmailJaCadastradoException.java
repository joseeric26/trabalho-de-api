package com.trabalho.erro;

// Representa a quebra da regra de que cada e-mail só pode ser cadastrado uma vez.
public class EmailJaCadastradoException extends RuntimeException {

    // Define a mensagem usada ao criar essa exceção.
    public EmailJaCadastradoException() {
        super("Já existe um usuário cadastrado com esse e-mail.");
    }
}
