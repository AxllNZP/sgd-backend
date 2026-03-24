package com.mesapartes.sgd.event;

import com.mesapartes.sgd.entity.PersonaNatural;
import com.mesapartes.sgd.entity.PersonaJuridica;
import org.springframework.context.ApplicationEvent;

public class CodigoVerificacionEvent extends ApplicationEvent {
    private final PersonaNatural natural; // null si no aplica
    private final PersonaJuridica juridica; // null si no aplica

    public CodigoVerificacionEvent(Object source, PersonaNatural natural, PersonaJuridica juridica) {
        super(source);
        this.natural = natural;
        this.juridica = juridica;
    }

    public PersonaNatural getNatural() { return natural; }
    public PersonaJuridica getJuridica() { return juridica; }
}