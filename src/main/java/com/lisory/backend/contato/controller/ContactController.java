package com.lisory.backend.contato.controller;

import com.lisory.backend.contato.dto.ContactRequest;
import com.lisory.backend.contato.dto.ContactResponse;
import com.lisory.backend.contato.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> submit(@Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.submit(request));
    }
}
