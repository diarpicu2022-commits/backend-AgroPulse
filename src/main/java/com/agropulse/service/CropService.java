package com.agropulse.service;

import com.agropulse.dao.CropDao;
import com.agropulse.model.Crop;
import com.agropulse.pattern.behavioral.state.CropContext;
import com.agropulse.pattern.creational.prototype.GreenhousePrototype;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para cultivos.
 * Aplica SRP: solo logica de negocio, sin acceso directo a BD.
 * Aplica DIP: depende de la abstraccion GenericDao, no de implementaciones.
 * Integra Patron State (CropContext) y Patron Prototype (clonacion de cultivos).
 */
@Service
public class CropService {

    private final CropDao cropDao;

    public CropService(CropDao cropDao) { this.cropDao = cropDao; }

    public List<Crop> getAllCrops()              { return cropDao.findAll(); }
    public List<Crop> getActiveCrops()           { return cropDao.findActive(); }
    public Optional<Crop> getCropById(int id)    { return cropDao.findById(id); }
    public Crop saveCrop(Crop crop)              { return cropDao.save(crop); }
    public void deleteCrop(int id)               { cropDao.delete(id); }

    /**
     * Avanza el cultivo al siguiente estado del ciclo de vida.
     * Usa el Patron State (CropContext) para gestionar la transicion.
     */
    public Crop advanceCropStage(int cropId) {
        Optional<Crop> optional = cropDao.findById(cropId);
        if (optional.isEmpty()) throw new RuntimeException("Cultivo no encontrado: " + cropId);
        Crop crop    = optional.get();
        CropContext ctx = new CropContext(crop); // Contexto del Patron State
        ctx.advance();                            // Transicion de estado
        return cropDao.save(crop);                // Persistir nuevo estado
    }

    /**
     * Clona un cultivo existente usando el Patron Prototype.
     * Util para crear un nuevo ciclo con la misma configuracion.
     */
    public Crop cloneCropForNewCycle(int cropId) {
        Optional<Crop> optional = cropDao.findById(cropId);
        if (optional.isEmpty()) throw new RuntimeException("Cultivo no encontrado: " + cropId);
        Crop original = optional.get();
        Crop clone    = original.clone(); // Patron Prototype
        clone.setId(0); // ID 0 = nuevo registro
        clone.setName(original.getName() + " (Nuevo Ciclo)");
        return cropDao.save(clone);
    }
}
