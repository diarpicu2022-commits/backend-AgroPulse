package com.agropulse.service;

import com.agropulse.dao.AlertRecipientRepository;
import com.agropulse.model.AlertRecipient;
import com.agropulse.model.SensorAnomaly;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.*;
import com.sendgrid.helpers.mail.objects.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private AlertRecipientRepository recipientRepository;

    @Value("${SENDGRID_API_KEY:}")
    private String sendgridApiKey;

    @Value("${SENDGRID_FROM_EMAIL:noreply@agropulse.app}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Circuit breakers: tras 5 fallos consecutivos, pausa 30s (SendGrid) / 60s (CallMeBot)
    private final CircuitBreaker sendgridCb;
    private final CircuitBreaker callmebotCb;

    public NotificationService() {
        CircuitBreakerConfig sendgridCfg = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build();
        CircuitBreakerConfig callmebotCfg = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        sendgridCb  = registry.circuitBreaker("sendgrid",  sendgridCfg);
        callmebotCb = registry.circuitBreaker("callmebot", callmebotCfg);
    }

    public void notifyAnomaly(SensorAnomaly anomaly, int greenhouseId,
                               String sensorName, String greenhouseName) {
        List<AlertRecipient> recipients = recipientRepository
                .findByGreenhouseIdAndActiveTrue(greenhouseId);
        for (AlertRecipient r : recipients) {
            if (r.getEmail() != null && !r.getEmail().isBlank()) {
                try { sendEmail(r, anomaly, sensorName, greenhouseName); }
                catch (Exception e) { log.warn("[NotificationService] Email omitido (circuit breaker): {}", e.getMessage()); }
            }
            if (r.getPhone() != null && !r.getPhone().isBlank()
                    && r.getCallmebotApikey() != null && !r.getCallmebotApikey().isBlank()) {
                try { sendWhatsApp(r, anomaly, sensorName, greenhouseName); }
                catch (Exception e) { log.warn("[NotificationService] WhatsApp omitido (circuit breaker): {}", e.getMessage()); }
            }
        }
    }

    private void sendEmail(AlertRecipient recipient, SensorAnomaly anomaly,
                            String sensorName, String greenhouseName) {
        if (sendgridApiKey == null || sendgridApiKey.isBlank()) return;
        sendgridCb.executeRunnable(() -> {
            try {
                String subject = "[AgroPulse] Anomalía: " + anomaly.getAnomalyType()
                                 + " — Sensor \"" + sensorName + "\"";
                String body = "<h2 style='color:#dc2626'>Alerta de Sensor — AgroPulse</h2>"
                    + "<p><b>Invernadero:</b> " + greenhouseName + "</p>"
                    + "<p><b>Sensor:</b> " + sensorName + "</p>"
                    + "<p><b>Tipo de anomalía:</b> " + translateType(anomaly.getAnomalyType()) + "</p>"
                    + (anomaly.getValueAtDetection() != null
                        ? "<p><b>Valor detectado:</b> " + anomaly.getValueAtDetection() + "</p>" : "")
                    + "<p><b>Hora:</b> " + anomaly.getDetectedAt() + "</p>"
                    + "<p><b>Detalle:</b> " + anomaly.getMessage() + "</p>"
                    + "<hr><p style='color:#6b7280;font-size:12px'>AgroPulse IoT Monitoring</p>";

                Email from    = new Email(fromEmail, "AgroPulse");
                Email to      = new Email(recipient.getEmail(), recipient.getName());
                Content content = new Content("text/html", body);
                Mail mail     = new Mail(from, subject, to, content);

                SendGrid sg = new SendGrid(sendgridApiKey);
                Request req = new Request();
                req.setMethod(Method.POST);
                req.setEndpoint("mail/send");
                req.setBody(mail.build());
                Response resp = sg.api(req);
                if (resp.getStatusCode() >= 400) {
                    log.error("[NotificationService] SendGrid error {}: {}",
                            resp.getStatusCode(), resp.getBody());
                    throw new RuntimeException("SendGrid " + resp.getStatusCode());
                }
                log.info("[NotificationService] Email enviado a {}", recipient.getEmail());
            } catch (IOException e) {
                log.error("[NotificationService] Email failed: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private void sendWhatsApp(AlertRecipient recipient, SensorAnomaly anomaly,
                               String sensorName, String greenhouseName) {
        callmebotCb.executeRunnable(() -> {
            try {
                String text = "AgroPulse: " + translateType(anomaly.getAnomalyType())
                        + " en \"" + sensorName + "\" (" + greenhouseName + ")."
                        + (anomaly.getValueAtDetection() != null
                            ? " Valor: " + String.format("%.2f", anomaly.getValueAtDetection()) + "." : "")
                        + " " + anomaly.getDetectedAt().toString().substring(0, 16).replace("T", " ");
                String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
                String url = "https://api.callmebot.com/whatsapp.php?phone="
                        + recipient.getPhone() + "&text=" + encoded
                        + "&apikey=" + recipient.getCallmebotApikey();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> resp = httpClient.send(req,
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 400) {
                    log.error("[NotificationService] CallMeBot error {}", resp.statusCode());
                    throw new RuntimeException("CallMeBot " + resp.statusCode());
                }
                log.info("[NotificationService] WhatsApp enviado a {}", recipient.getPhone());
            } catch (Exception e) {
                log.error("[NotificationService] WhatsApp failed: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private String translateType(String type) {
        return switch (type) {
            case "OUT_OF_RANGE" -> "Valor fuera de rango";
            case "NO_DATA"      -> "Sin datos del sensor";
            case "STUCK"        -> "Valor constante (sensor posiblemente dañado)";
            case "SPIKE"        -> "Cambio brusco de valor";
            default             -> type;
        };
    }
}
