package com.proyecto.spring_proyecto.order;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.proyecto.spring_proyecto.core.entity.CustomerOrder;
import com.proyecto.spring_proyecto.order.dto.CreateOrderRequest;
import com.proyecto.spring_proyecto.shared.ApiException;

@Component
public class OrderIdempotencyService {

    private static final Pattern VALID_KEY = Pattern.compile("^[A-Za-z0-9._:-]{8,64}$");

    public String normalizeKey(String rawKey) {
        var key = rawKey == null ? "" : rawKey.trim();
        if (!VALID_KEY.matcher(key).matches()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_IDEMPOTENCY_KEY",
                    "Idempotency-Key debe tener entre 8 y 64 caracteres seguros");
        }
        return key;
    }

    public String fingerprint(CreateOrderRequest request) {
        var canonical = new StringBuilder(request.tableCode().trim().toUpperCase(Locale.ROOT)).append('|');
        request.items().stream()
                .sorted(Comparator.comparing(item -> item.productId()))
                .forEach(item -> canonical
                        .append(item.productId()).append(':')
                        .append(item.quantity()).append(':')
                        .append(normalizeNotes(item.notes())).append('|'));
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible", exception);
        }
    }

    public void verifySameRequest(CustomerOrder existingOrder, String fingerprint) {
        if (!existingOrder.getRequestFingerprint().equals(fingerprint)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "IDEMPOTENCY_KEY_REUSED",
                    "La clave de idempotencia ya fue usada con otro pedido");
        }
    }

    private String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? "" : notes.trim();
    }
}
