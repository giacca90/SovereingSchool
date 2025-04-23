package com.sovereingschool.back_base.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.sovereingschool.back_base.DTOs.AuthResponse;
import com.sovereingschool.back_base.DTOs.ChangePassword;
import com.sovereingschool.back_base.Interfaces.ILoginService;
import com.sovereingschool.back_common.Models.Login;
import com.sovereingschool.back_common.Models.Usuario;
import com.sovereingschool.back_common.Repositories.LoginRepository;
import com.sovereingschool.back_common.Repositories.UsuarioRepository;
import com.sovereingschool.back_common.Utils.JwtUtil;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class LoginService implements UserDetailsService, ILoginService {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Long compruebaCorreo(String correo) {
        Long result = this.loginRepository.compruebaCorreo(correo);
        if (result == null)
            return 0L;
        return result;
    }

    public String createNuevoLogin(Login login) {
        this.loginRepository.save(login);
        return "Nuevo Usuario creado con éxito!!!";
    }

    public String getCorreoLogin(Long id_usuario) {
        return this.loginRepository.findCorreoLoginForId(id_usuario);
    }

    public String getPasswordLogin(Long id_usuario) {
        return this.loginRepository.findPasswordLoginForId(id_usuario);
    }

    public String changeCorreoLogin(Login login) {
        this.loginRepository.changeCorreoLoginForId(login.getId_usuario(), login.getCorreo_electronico());
        return "Correo cambiado con éxito!!!";
    }

    public Integer changePasswordLogin(ChangePassword changepassword) {
        if (changepassword.getNew_password().length() < 1 || changepassword.getOld_password().length() < 1)
            return null;
        if (this.loginRepository.findPasswordLoginForId(changepassword.getId_usuario())
                .equals(changepassword.getOld_password())) {
            this.loginRepository.changePasswordLoginForId(changepassword.getId_usuario(),
                    changepassword.getNew_password());
            return 1;
        }
        return 0;
    }

    public String deleteLogin(Long id_usuario) {
        this.loginRepository.deleteById(id_usuario);
        return "Login eliminado con éxito!!!";
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        // Si es el usuario visitante, no buscar en BD
        if (correo.equals("Visitante")) {
            return User.withUsername("Visitante")
                    .password("visitante")
                    .roles("GUEST")
                    .accountExpired(false)
                    .credentialsExpired(false)
                    .accountLocked(false)
                    .build();
        }

        Login login = this.loginRepository.getLoginForCorreo(correo);
        if (login == null) {
            throw new UsernameNotFoundException("Correo electronico " + correo + " no encontrado");
        }

        Optional<Usuario> usuario = this.usuarioRepository.findById(login.getId_usuario());
        if (usuario.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        List<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ROLE_" + usuario.get().getRoll_usuario().name()));

        return new User(usuario.get().getNombre_usuario(),
                login.getPassword(),
                usuario.get().getIsEnabled(),
                usuario.get().getAccountNoExpired(),
                usuario.get().getCredentialsNoExpired(),
                usuario.get().getAccountNoLocked(),
                roles);
    }

    @Transactional
    public AuthResponse loginUser(Long id, String password) {
        String correo = this.loginRepository.findCorreoLoginForId(id);
        UserDetails userDetails = this.loadUserByUsername(correo);
        if (userDetails == null) {
            throw new BadCredentialsException("Usuario o password incorrecto");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Password incorrecta");
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(correo, userDetails.getPassword(),
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Usuario> usuarioOpt = this.usuarioRepository
                .findById(this.loginRepository.getLoginForCorreo(auth.getName()).getId_usuario());
        if (usuarioOpt.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();
        Hibernate.initialize(usuario.getCursos_usuario());

        String accessToken = jwtUtil.generateToken(auth, "access", usuario.getId_usuario());
        String refreshToken = jwtUtil.generateToken(auth, "refresh", usuario.getId_usuario());

        return new AuthResponse(true, "Login exitoso", usuario, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refreshAccessToken(Long id) {
        String correo = this.loginRepository.findCorreoLoginForId(id);
        UserDetails userDetails = this.loadUserByUsername(correo);
        if (userDetails == null) {
            throw new BadCredentialsException("Usuario no encontrado");
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(correo, userDetails.getPassword(),
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
        String accessToken = jwtUtil.generateToken(auth, "access", id);
        String refreshToken = jwtUtil.generateToken(auth, "refresh", id);
        return new AuthResponse(true, "Refresh exitoso", null, accessToken, refreshToken);
    }

    @Transactional
    public Usuario loginWithToken(String token) {
        try {
            Long id_usuario = jwtUtil.getIdUsuario(token);
            Optional<Usuario> opUsuario = this.usuarioRepository.findById(id_usuario);
            if (opUsuario.isEmpty()) {
                throw new BadCredentialsException("Usuario no encontrado");
            }
            Usuario usuario = opUsuario.get();
            return usuario;
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException("Error en hacer login con token: " + e.getMessage());
        }
    }
}
