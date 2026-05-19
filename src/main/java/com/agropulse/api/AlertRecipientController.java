package com.agropulse.api;

import com.agropulse.api.dto.AlertRecipientCreateDto;
import com.agropulse.dao.AlertRecipientRepository;
import com.agropulse.model.AlertRecipient;
import com.agropulse.service.OwnershipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    @Autowired
    private OwnershipService ownershipService;

    // GET /greenhouses/{id}/alert-recipients
    @GetMapping("/greenhouses/{id}/alert-recipients")
    public ResponseEntity<?> list(@PathVariable int id) {
        List<AlertRecipient> list = recipientRepository.findByGreenhouseId(id);
        return ResponseEntity.ok(Map.of("recipients", list));
    }

    // POST /greenhouses/{id}/alert-recipients
    @PostMapping("/greenhouses/{id}/alert-recipients")
    public ResponseEntity<?> create(@PathVariable int id,
                                    @Valid @RequestBody AlertRecipientCreateDto body,
                                    HttpServletRequest request) {
        if (!ownershipService.canModifyGreenhouse(id, request))
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para agregar destinatarios a este invernadero"));
        AlertRecipient r = new AlertRecipient();
        r.setGreenhouseId(id);
        r.setName(body.getName());
        if (body.getEmail()           != null) r.setEmail(body.getEmail());
        if (body.getPhone()           != null) r.setPhone(body.getPhone());
        if (body.getCallmebotApikey() != null) r.setCallmebotApikey(body.getCallmebotApikey());
        r.setActive(true);
        recipientRepository.save(r);
        return ResponseEntity.ok(r);
    }

    // DELETE /greenhouses/{id}/alert-recipients/{recipientId}
    @DeleteMapping("/greenhouses/{id}/alert-recipients/{recipientId}")
    public ResponseEntity<?> remove(@PathVariable int id,
                                    @PathVariable int recipientId,
                                    HttpServletRequest request) {
        if (!ownershipService.canModifyGreenhouse(id, request))
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para eliminar destinatarios de este invernadero"));
        Optional<AlertRecipient> opt = recipientRepository.findById(recipientId);
        if (opt.isEmpty() || opt.get().getGreenhouseId() != id)
            return ResponseEntity.notFound().build();
        recipientRepository.deleteById(recipientId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
