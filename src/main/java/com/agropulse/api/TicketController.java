package com.agropulse.api;

import com.agropulse.dao.TicketRepository;
import com.agropulse.model.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    // ── GET /tickets → JSON array (not wrapped) ───────────────────────────
    @GetMapping
    public ResponseEntity<List<Ticket>> getAll() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    // ── POST /tickets ─────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Ticket ticket = new Ticket();
        if (body.containsKey("title"))       ticket.setTitle((String) body.get("title"));
        if (body.containsKey("description")) ticket.setDescription((String) body.get("description"));
        if (body.containsKey("status"))      ticket.setStatus((String) body.get("status"));
        if (body.containsKey("priority"))    ticket.setPriority((String) body.get("priority"));
        if (body.containsKey("userId"))      ticket.setUserId(toInt(body.get("userId")));
        if (body.containsKey("userName"))    ticket.setUserName((String) body.get("userName"));
        ticketRepository.save(ticket);
        return ResponseEntity.ok(ticket);
    }

    // ── GET /tickets/{id} ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        Optional<Ticket> opt = ticketRepository.findById(id);
        return opt.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // ── PUT /tickets/{id} ─────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, Object> body) {
        Optional<Ticket> opt = ticketRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Ticket ticket = opt.get();
        if (body.containsKey("title"))       ticket.setTitle((String) body.get("title"));
        if (body.containsKey("description")) ticket.setDescription((String) body.get("description"));
        if (body.containsKey("status"))      ticket.setStatus((String) body.get("status"));
        if (body.containsKey("priority"))    ticket.setPriority((String) body.get("priority"));
        if (body.containsKey("userId"))      ticket.setUserId(toInt(body.get("userId")));
        if (body.containsKey("userName"))    ticket.setUserName((String) body.get("userName"));
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return ResponseEntity.ok(ticket);
    }

    // ── DELETE /tickets/{id} ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        if (!ticketRepository.existsById(id)) return ResponseEntity.notFound().build();
        ticketRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Double) return ((Double) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
