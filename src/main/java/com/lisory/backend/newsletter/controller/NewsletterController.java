package com.lisory.backend.newsletter.controller;

import com.lisory.backend.newsletter.dto.NewsletterRequest;
import com.lisory.backend.newsletter.dto.NewsletterResponse;
import com.lisory.backend.newsletter.service.NewsletterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping
    public ResponseEntity<NewsletterResponse> subscribe(@Valid @RequestBody NewsletterRequest request) {
        return ResponseEntity.ok(newsletterService.subscribe(request));
    }
}
