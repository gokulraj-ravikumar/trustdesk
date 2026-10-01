package com.gokul.trustdesk.domain.service;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class AdversarialGuardrailService {

    private static final Pattern BYPASS_IDENTITY_PATTERN = Pattern.compile("(?i)(skip|bypass)\\s+(identity|verification|check)");
    private static final Pattern HIDDEN_PROMPT_PATTERN = Pattern.compile("(?i)(reveal|ignore|print).*?(instructions|prompts|secrets|internal)");
    private static final Pattern HIDDEN_COUPON_PATTERN = Pattern.compile("(?i)(hidden|secret).*?(coupon|discount)");

    public boolean isSafe(String ticketBody) {
        if (ticketBody == null) return true;

        if (BYPASS_IDENTITY_PATTERN.matcher(ticketBody).find() ||
                HIDDEN_PROMPT_PATTERN.matcher(ticketBody).find() ||
                HIDDEN_COUPON_PATTERN.matcher(ticketBody).find()) {
            return false;
        }
        return true;
    }
}