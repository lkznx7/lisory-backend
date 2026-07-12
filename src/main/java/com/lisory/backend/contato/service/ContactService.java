package com.lisory.backend.contato.service;

import com.lisory.backend.contato.dto.ContactRequest;
import com.lisory.backend.contato.dto.ContactResponse;
import com.lisory.backend.contato.entity.ContactMessage;
import com.lisory.backend.contato.repository.ContactMessageRepository;
import com.lisory.backend.shared.log.StructuredLogger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ContactService {

    private static final StructuredLogger log = StructuredLogger.forClass(ContactService.class);

    private final ContactMessageRepository repository;

    public ContactService(ContactMessageRepository repository) {
        this.repository = repository;
    }

    public ContactResponse submit(ContactRequest request) {
        ContactMessage entity = new ContactMessage(
            request.name().trim(),
            request.email().toLowerCase().trim(),
            request.subject() != null ? request.subject().trim() : "",
            request.message().trim()
        );

        repository.save(entity);

        log.info("contact_message_received", Map.of(
            "email", request.email(),
            "subject", request.subject() != null ? request.subject() : ""
        ));

        return new ContactResponse("Mensagem recebida com sucesso.", LocalDateTime.now());
    }
}
