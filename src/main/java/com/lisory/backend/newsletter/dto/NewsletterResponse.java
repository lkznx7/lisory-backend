package com.lisory.backend.newsletter.dto;

public record NewsletterResponse(
    boolean alreadySubscribed,
    String message
) {}
