package com.agropulse.api;

import com.agropulse.pattern.structural.facade.GreenhouseFacade;
import com.agropulse.pattern.structural.facade.GreenhouseReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST del invernadero.
 * Usa el Patron Facade para exponer operaciones complejas en un solo endpoint.
 */
@RestController
@RequestMapping("/greenhouse")
@CrossOrigin(origins = "*")
public class GreenhouseController {

    private final GreenhouseFacade facade;
    public GreenhouseController(GreenhouseFacade facade) { this.facade = facade; }

    @GetMapping("/{id}/report")
    public ResponseEntity<GreenhouseReport> getFullReport(@PathVariable int id) {
        return ResponseEntity.ok(facade.getGreenhouseFullReport(id));
    }

    @PostMapping("/{id}/irrigate")
    public ResponseEntity<Boolean> triggerIrrigation(
            @PathVariable int id,
            @RequestParam(defaultValue = "50.0") double threshold) {
        return ResponseEntity.ok(facade.triggerIrrigationIfNeeded(id, threshold));
    }
}
