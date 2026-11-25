// Indica em qual pacote este arquivo fica.
package com.trabalho.usuarios;

// Guarda o nome e o e-mail que serão recebidos no JSON da requisição.
public record UsuarioEntrada(String nome, String email) {
}