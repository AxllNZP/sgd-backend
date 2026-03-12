package com.mesapartes.sgd.event;

import com.mesapartes.sgd.dto.DocumentoRequestDTO;
import com.mesapartes.sgd.entity.Documento;
import org.springframework.context.ApplicationEvent;

public class DocumentoRegistradoEvent extends ApplicationEvent {
    private final Documento documento;
    private final DocumentoRequestDTO request;

    public DocumentoRegistradoEvent(Object source, Documento documento, DocumentoRequestDTO request) {
        super(source);
        this.documento = documento;
        this.request = request;
    }

    public Documento getDocumento() { return documento; }
    public DocumentoRequestDTO getRequest() { return request; }
}