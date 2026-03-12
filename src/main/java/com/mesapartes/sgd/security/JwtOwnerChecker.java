package com.mesapartes.sgd.security;

import com.mesapartes.sgd.repository.PersonaJuridicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("jwtOwnerChecker")
@RequiredArgsConstructor
public class JwtOwnerChecker {

    private final PersonaJuridicaRepository juridicaRepo;

    public boolean isOwnerJuridica(String ruc, Authentication auth) {

        return juridicaRepo.findByRuc(ruc)
                .map(e -> e.getEmailRepresentante().equals(auth.getName()))
                .orElse(false);
    }
}