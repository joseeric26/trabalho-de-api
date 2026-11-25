package com.trabalho.repository;

import com.trabalho.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

// Usuario é a entidade que o repositório salva. Long é o tipo do seu ID.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Abre uma transação para executar a gravação.
    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO usuarios (nome, email)
        VALUES (:nome, :email)
        ON CONFLICT ON CONSTRAINT uk_usuarios_email DO NOTHING
        """, nativeQuery = true)
    int inserirUsuario(@Param("nome") String nome, @Param("email") String email);
}