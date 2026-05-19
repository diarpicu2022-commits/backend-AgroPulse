package com.agropulse.api;

import com.agropulse.dao.AlertRecipientRepository;
import com.agropulse.model.AlertRecipient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class AlertRecipientController {

    @Autowired
    private AlertRecipientRepository recipientRepository;

    // GET /greenhouses/{id}/alert-recipients
    @GetMapping("/greenhouses/{id}/alert-recipients")
    public ResponseEntity<?> list(@PathVariable int id) {
        List<AlertRecipient> list = recipientRepository.findByGreenhouseId(id);
        return ResponseEntity.ok(Map.of("recipients", list));
    }

    // POST /greenhouses/{id}/alert-recipients
    @PostMapping("/greenhouses/{id}/alert-recipients")
    public ResponseEntity<?> create(@PathVariable int id,
                                    @RequestBody Map<String, Object> body) {
        AlertRecipient r = new AlertRecipient();
        r.setGreenhouseId(id);
        if (body.containsKey("name"))             r.setName((String) body.get("name"));
        if (body.containsKey("email"))            r.setEmail((String) body.get("email"));
        if (body.containsKey("phone"))            r.setPhone((String) body.get("phone"));
        if (body.containsKey("callmebotApikey"))  r.setCallmebotApikey((String) body.get("callmebotApikey"));
        r.setActive(true);
        recipientRepository.save(r);
        return ResponseEntity.ok(r);
    }

    // DELETE /greenhouses/{id}/alert-recipients/{recipientId}
    @DeleteMapping("/greenhouses/{id}/alert-recipients/{recipientId}")
    public ResponseEntity<?> remove(@PathVariable int id,
                                    @PathVariable int recipientId) {
        Optional<AlertRecipient> opt = recipientRepository.findById(recipientId);
        if (opt.isEmpty() || opt.get().getGreenhouseId() != id)
            return ResponseEntity.notFound().build();
        recipientRepository.deleteById(recipientId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
