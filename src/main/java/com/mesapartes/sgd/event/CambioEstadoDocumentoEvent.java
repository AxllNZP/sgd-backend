package com.mesapartes.sgd.event;

import com.mesapartes.sgd.dto.CambioEstadoDTO;
import com.mesapartes.sgd.entity.Documento;
import org.springframework.context.ApplicationEvent;

public class CambioEstadoDocumentoEvent extends ApplicationEvent {
    private final Documento documento;
    private final CambioEstadoDTO cambio;

    public CambioEstadoDocumentoEvent(Object source, Documento documento, CambioEstadoDTO cambio) {
        super(source);
        this.documento = documento;
        this.cambio = cambio;
    }

    public Documento getDocumento() { return documento; }
    public CambioEstadoDTO getCambio() { return cambio; }
}