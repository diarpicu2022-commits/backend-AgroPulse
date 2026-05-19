package com.agropulse.service;

import com.agropulse.dao.AlertRecipientRepository;
import com.agropulse.model.AlertRecipient;
import com.agropulse.model.SensorAnomaly;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.*;
import com.sendgrid.helpers.mail.objects.*;
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
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private AlertRecipientRepository recipientRepository;

    @Value("${SENDGRID_API_KEY:}")
    private String sendgridApiKey;

    @Value("${SENDGRID_FROM_EMAIL:noreply@agropulse.app}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void notifyAnomaly(SensorAnomaly anomaly, int greenhouseId,
                               String sensorName, String greenhouseName) {
        List<AlertRecipient> recipients = recipientRepository
                .findByGreenhouseIdAndActiveTrue(greenhouseId);
        for (AlertRecipient r : recipients) {
            if (r.getEmail() != null && !r.getEmail().isBlank()) {
                sendEmail(r, anomaly, sensorName, greenhouseName);
            }
            if (r.getPhone() != null && !r.getPhone().isBlank()
                    && r.getCallmebotApikey() != null && !r.getCallmebotApikey().isBlank()) {
                sendWhatsApp(r, anomaly, sensorName, greenhouseName);
            }
        }
    }

    private void sendEmail(AlertRecipient recipient, SensorAnomaly anomaly,
                            String sensorName, String greenhouseName) {
        if (sendgridApiKey == null || sendgridApiKey.isBlank()) return;
        try {
            String subject = "[AgroPulse] ⚠️ Anomalía: " + anomaly.getAnomalyType()
                             + " — Sensor \"" + sensorName + "\"";
            String body = "<h2 style='color:#dc2626'>⚠️ Alerta de Sensor — AgroPulse</h2>"
                + "<p><b>Invernadero:</b> " + greenhouseName + "</p>"
                + "<p><b>Sensor:</b> " + sensorName + "</p>"
                + "<p><b>Tipo de anomalía:</b> " + translateType(anomaly.getAnomalyType()) + "</p>"
                + (anomaly.getValueAtDetection() != null
                    ? "<p><b>Valor detectado:</b> " + anomaly.getValueAtDetection() + "</p>" : "")
                + "<p><b>Hora:</b> " + anomaly.getDetectedAt() + "</p>"
                + "<p><b>Detalle:</b> " + anomaly.getMessage() + "</p>"
                + "<hr><p style='color:#6b7280;font-size:12px'>AgroPulse IoT Monitoring</p>";

            Email from = new Email(fromEmail, "AgroPulse");
            Email to   = new Email(recipient.getEmail(), recipient.getName());
            Content content = new Content("text/html", body);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendgridApiKey);
            Request req = new Request();
            req.setMethod(Method.POST);
            req.setEndpoint("mail/send");
            req.setBody(mail.build());
            Response resp = sg.api(req);
            if (resp.getStatusCode() >= 400) {
                System.err.println("[NotificationService] SendGrid error " + resp.getStatusCode()
                        + ": " + resp.getBody());
            }
        } catch (IOException e) {
            System.err.println("[NotificationService] Email failed: " + e.getMessage());
        }
    }

    private void sendWhatsApp(AlertRecipient recipient, SensorAnomaly anomaly,
                               String sensorName, String greenhouseName) {
        try {
            String text = "⚠️ AgroPulse: " + translateType(anomaly.getAnomalyType())
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
                System.err.println("[NotificationService] CallMeBot error " + resp.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[NotificationService] WhatsApp failed: " + e.getMessage());
        }
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
