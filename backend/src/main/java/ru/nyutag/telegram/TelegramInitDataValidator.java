package ru.nyutag.telegram;

import org.springframework.stereotype.Component;
import ru.nyutag.common.UnauthorizedException;
import ru.nyutag.config.NyutagProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class TelegramInitDataValidator {
    private final NyutagProperties properties;

    public TelegramInitDataValidator(NyutagProperties properties) {
        this.properties = properties;
    }

    public TelegramUser validate(String initData) {
        if (initData == null || initData.isBlank()) {
            throw new UnauthorizedException("Missing init data");
        }
        String botToken = properties.getTelegram().getBotToken();
        if (botToken == null || botToken.isBlank()) {
            throw new UnauthorizedException("Telegram authentication is not configured");
        }

        Map<String, String> params = parse(initData);
        String hash = params.remove("hash");
        if (hash == null || hash.isBlank()) {
            throw new UnauthorizedException("Missing hash");
        }

        String dataCheckString = buildDataCheckString(params);
        byte[] secretKey = hmacSha256("WebAppData".getBytes(StandardCharsets.UTF_8), botToken.getBytes(StandardCharsets.UTF_8));
        byte[] computed = hmacSha256(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8));
        String computedHex = toHex(computed);
        if (!constantTimeEquals(computedHex, hash)) {
            throw new UnauthorizedException("Invalid signature");
        }

        String authDate = params.get("auth_date");
        if (authDate != null) {
            try {
                long ts = Long.parseLong(authDate.trim());
                long age = Instant.now().getEpochSecond() - ts;
                if (age > properties.getAuthMaxAgeSeconds()) {
                    throw new UnauthorizedException("Init data expired");
                }
            } catch (NumberFormatException e) {
                throw new UnauthorizedException("Invalid auth_date");
            }
        }

        String userJson = params.get("user");
        if (userJson == null || userJson.isBlank()) {
            throw new UnauthorizedException("Missing user");
        }
        return TelegramUser.fromJson(userJson);
    }

    private Map<String, String> parse(String initData) {
        Map<String, String> result = new TreeMap<>();
        for (String pair : initData.split("&")) {
            if (pair.isBlank()) continue;
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            result.put(key, value);
        }
        return result;
    }

    private String buildDataCheckString(Map<String, String> params) {
        List<String> lines = new ArrayList<>();
        params.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> lines.add(e.getKey() + "=" + e.getValue()));
        return String.join("\n", lines);
    }

    private byte[] hmacSha256(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}