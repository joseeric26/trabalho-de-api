package com.trabalho.controller;

import com.trabalho.erro.EmailJaCadastradoException;
import com.trabalho.repository.UsuarioRepository;
import com.trabalho.usuarios.UsuarioEntrada;
import com.trabalho.usuarios.UsuarioEntradaValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    // O Spring fornece o repositório ao criar o controller.
    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Registra o validador para os dados do tipo UsuarioEntrada.
    @InitBinder("usuarioEntrada")
    public void configurarValidacao(WebDataBinder binder) {
        binder.addValidators(UsuarioEntradaValidator.VALIDADOR);
    }

    // Faz o Spring chamar este metodo quando chegar um POST para /usuarios.
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioEntrada> receber(@RequestBody @Validated UsuarioEntrada entrada) {
        // @RequestBody converte o JSON da requisição em um UsuarioEntrada.
        // @Validated faz o Spring validar os dados antes de executar o método.

        // Linha usada no teste temporário do erro 500:
        // throw new IllegalStateException("Falha simulada para testar o erro 500.");

        // Tenta salvar o cadastro no PostgreSQL.
        int linhasInseridas = usuarioRepository.inserirUsuario(entrada.nome(), entrada.email());

        // Se o e-mail já existe, o banco insere zero linhas e o tratador devolve 409.
        if (linhasInseridas == 0) {
            throw new EmailJaCadastradoException();
        }

        // Envia os dados de volta como JSON com o status 201 Created.
        return ResponseEntity.status(201).body(entrada);
    }
}
