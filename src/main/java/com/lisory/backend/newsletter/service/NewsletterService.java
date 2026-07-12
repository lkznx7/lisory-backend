package com.lisory.backend.newsletter.service;

import com.lisory.backend.newsletter.dto.NewsletterRequest;
import com.lisory.backend.newsletter.dto.NewsletterResponse;
import com.lisory.backend.newsletter.entity.NewsletterSubscriber;
import com.lisory.backend.newsletter.repository.NewsletterSubscriberRepository;
import com.lisory.backend.shared.log.StructuredLogger;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NewsletterService {

    private static final StructuredLogger log = StructuredLogger.forClass(NewsletterService.class);

    private final NewsletterSubscriberRepository repository;

    public NewsletterService(NewsletterSubscriberRepository repository) {
        this.repository = repository;
    }

    public NewsletterResponse subscribe(NewsletterRequest request) {
        String email = request.email().toLowerCase().trim();

        if (repository.existsByEmail(email)) {
            log.info("newsletter_already_subscribed", Map.of("email", email));
            return new NewsletterResponse(true, "Você já está inscrito na newsletter.");
        }

        repository.save(new NewsletterSubscriber(email));
        log.info("newsletter_new_subscriber", Map.of("email", email));
        return new NewsletterResponse(false, "Inscrita com sucesso!");
    }
}
