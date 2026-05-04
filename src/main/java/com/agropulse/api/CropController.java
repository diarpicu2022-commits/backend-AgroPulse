package com.agropulse.api;

import com.agropulse.model.Crop;
import com.agropulse.service.CropService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para cultivos.
 * Expone endpoints RESTful — el frontend React se conecta a estos.
 * Aplica principio de Separacion de Capas: el controlador solo orquesta,
 * la logica de negocio esta en CropService.
 */
@RestController
@RequestMapping("/crops")
@CrossOrigin(origins = "*")
public class CropController {

    private final CropService cropService;
    public CropController(CropService cropService) { this.cropService = cropService; }

    @GetMapping
    public ResponseEntity<List<Crop>> getAllCrops() {
        return ResponseEntity.ok(cropService.getAllCrops());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Crop>> getActiveCrops() {
        return ResponseEntity.ok(cropService.getActiveCrops());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Crop> getCropById(@PathVariable int id) {
        return cropService.getCropById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Crop> createCrop(@RequestBody Crop crop) {
        return ResponseEntity.ok(cropService.saveCrop(crop));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Crop> updateCrop(@PathVariable int id, @RequestBody Crop crop) {
        crop.setId(id);
        return ResponseEntity.ok(cropService.saveCrop(crop));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCrop(@PathVariable int id) {
        cropService.deleteCrop(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/advance")
    public ResponseEntity<Crop> advanceCropStage(@PathVariable int id) {
        return ResponseEntity.ok(cropService.advanceCropStage(id));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<Crop> cloneCrop(@PathVariable int id) {
        return ResponseEntity.ok(cropService.cloneCropForNewCycle(id));
    }
}
