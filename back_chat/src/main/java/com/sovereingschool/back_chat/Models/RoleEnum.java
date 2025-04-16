package com.sovereingschool.back_chat.Models;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum RoleEnum {
    ADMIN, USER, GUEST, PROF;

    Iterable<SimpleGrantedAuthority> stream() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stream'");
    }
}
