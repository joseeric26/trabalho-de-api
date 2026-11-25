package com.trabalho.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Diz ao JPA que esta classe representa dados armazenados no banco.
@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_usuarios_email",
                columnNames = "email"
        )
)
public class Usuario {

    // Identifica cada usuário. O banco gera o ID ao inserir o registro.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // As colunas nome e email são obrigatórias no banco.
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    // O JPA usa este construtor para carregar usuários do banco.
    protected Usuario() {
    }

    // A API usa este construtor para preparar um novo cadastro.
    public Usuario(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }
}