package com.vjagarwal.veyra;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class FirebasePushService {
    private final boolean enabled;

    public FirebasePushService(@Value("${veyra.firebase.service-account-json:}") String json) {
        boolean ready = false;
        try {
            if (json != null && !json.isBlank()) {
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(
                                    new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))))
                            .build();
                    FirebaseApp.initializeApp(options);
                }
                ready = true;
            }
        } catch (Exception ignored) {
            ready = false;
        }
        this.enabled = ready;
    }

    public Object send(String token, String title, String body) {
        if (!enabled) return Map.of("enabled", false, "message", "Firebase Admin credentials are not configured");
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .build();
            return Map.of("enabled", true, "messageId", FirebaseMessaging.getInstance().send(message));
        } catch (Exception e) {
            return Map.of("enabled", true, "sent", false, "error", e.getMessage() == null ? "FCM send failed" : e.getMessage());
        }
    }
}
