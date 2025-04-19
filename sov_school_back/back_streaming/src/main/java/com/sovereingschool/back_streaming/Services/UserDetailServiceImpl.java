package com.sovereingschool.back_streaming.Services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_common.Models.Usuario;
import com.sovereingschool.back_common.Repositories.LoginRepository;
import com.sovereingschool.back_common.Repositories.UsuarioRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Si es el usuario visitante, no buscar en BD
        if (username.equals("Visitante")) {
            return User.withUsername("Visitante")
                    .password("visitante")
                    .roles("GUEST")
                    .build();
        }

        Usuario usuario = this.usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario " + username + " no encontrado"));

        List<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRoll_usuario().name()));

        System.out.println("Usuario: " + usuario.getNombre_usuario());
        System.out.println("Rol asignado: ROLE_" + usuario.getRoll_usuario().name());

        return new User(usuario.getNombre_usuario(),
                loginRepository.findPasswordLoginForId(usuario.getId_usuario()),
                usuario.getIsEnabled(),
                usuario.getAccountNoExpired(),
                usuario.getCredentialsNoExpired(),
                usuario.getAccountNoLocked(),
                roles);
    }

}
