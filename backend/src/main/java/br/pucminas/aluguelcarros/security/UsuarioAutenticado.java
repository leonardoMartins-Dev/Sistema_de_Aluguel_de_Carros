package br.pucminas.aluguelcarros.security;

import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.model.enums.Perfil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Usuário logado, guardado no SecurityContext.
 * Nos controllers: {@code @AuthenticationPrincipal UsuarioAutenticado usuario} → usuario.id()
 * serve para conferir se ele é dono do recurso (RN07, RN09).
 */
public record UsuarioAutenticado(
        Long id,
        String login,
        String senha,
        String nome,
        Perfil perfil
) implements UserDetails {

    public static UsuarioAutenticado from(Usuario usuario) {
        return new UsuarioAutenticado(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getSenha(),
                usuario.getNome(),
                usuario.getPerfil()
        );
    }

    // RN03: o perfil vira a role usada em hasRole("CLIENTE"), hasAnyRole("EMPRESA", "BANCO")
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return login;
    }
}
