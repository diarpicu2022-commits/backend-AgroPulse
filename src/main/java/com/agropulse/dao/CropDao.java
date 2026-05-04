package com.agropulse.dao;

import com.agropulse.model.Crop;
import com.agropulse.model.enums.CropStage;
import com.agropulse.pattern.creational.singleton.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la entidad Crop.
 * Usa el Singleton DatabaseConnection para obtener la conexion.
 * Herencia (Bloque 5): implementa GenericDao<Crop, Integer>.
 */
@Repository
public class CropDao implements GenericDao<Crop, Integer> {

    private Connection getConn() { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public Crop save(Crop crop) {
        if (crop.getId() == 0) return insert(crop);
        return update(crop);
    }

    private Crop insert(Crop crop) {
        String sql = "INSERT INTO crops (name, variety, temp_min, temp_max, humidity_min, humidity_max, " +
                     "soil_moisture_min, soil_moisture_max, planting_date, current_stage, active) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, crop.getName());
            ps.setString(2, crop.getVariety());
            ps.setDouble(3, crop.getTempMin());
            ps.setDouble(4, crop.getTempMax());
            ps.setDouble(5, crop.getHumidityMin());
            ps.setDouble(6, crop.getHumidityMax());
            ps.setDouble(7, crop.getSoilMoistureMin());
            ps.setDouble(8, crop.getSoilMoistureMax());
            ps.setString(9, crop.getPlantingDate() != null ? crop.getPlantingDate().toString() : LocalDate.now().toString());
            ps.setString(10, crop.getCurrentStage() != null ? crop.getCurrentStage().name() : CropStage.SEEDING.name());
            ps.setInt(11, crop.isActive() ? 1 : 0);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) crop.setId(rs.getInt(1));
        } catch (SQLException e) { System.err.println("[CropDao] Error insert: " + e.getMessage()); }
        return crop;
    }

    private Crop update(Crop crop) {
        String sql = "UPDATE crops SET name=?, variety=?, temp_min=?, temp_max=?, humidity_min=?, humidity_max=?, " +
                     "soil_moisture_min=?, soil_moisture_max=?, current_stage=?, active=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, crop.getName());  ps.setString(2, crop.getVariety());
            ps.setDouble(3, crop.getTempMin()); ps.setDouble(4, crop.getTempMax());
            ps.setDouble(5, crop.getHumidityMin()); ps.setDouble(6, crop.getHumidityMax());
            ps.setDouble(7, crop.getSoilMoistureMin()); ps.setDouble(8, crop.getSoilMoistureMax());
            ps.setString(9, crop.getCurrentStage() != null ? crop.getCurrentStage().name() : CropStage.SEEDING.name());
            ps.setInt(10, crop.isActive() ? 1 : 0); ps.setInt(11, crop.getId());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[CropDao] Error update: " + e.getMessage()); }
        return crop;
    }

    @Override
    public Optional<Crop> findById(Integer id) {
        try (PreparedStatement ps = getConn().prepareStatement("SELECT * FROM crops WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) { System.err.println("[CropDao] Error findById: " + e.getMessage()); }
        return Optional.empty();
    }

    @Override
    public List<Crop> findAll() {
        List<Crop> list = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM crops ORDER BY id")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[CropDao] Error findAll: " + e.getMessage()); }
        return list;
    }

    public List<Crop> findActive() {
        List<Crop> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement("SELECT * FROM crops WHERE active=1 ORDER BY id")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[CropDao] Error findActive: " + e.getMessage()); }
        return list;
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM crops WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[CropDao] Error delete: " + e.getMessage()); }
    }

    @Override
    public boolean existsById(Integer id) { return findById(id).isPresent(); }

    private Crop mapRow(ResultSet rs) throws SQLException {
        Crop c = new Crop();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setVariety(rs.getString("variety"));
        c.setTempMin(rs.getDouble("temp_min"));
        c.setTempMax(rs.getDouble("temp_max"));
        c.setHumidityMin(rs.getDouble("humidity_min"));
        c.setHumidityMax(rs.getDouble("humidity_max"));
        c.setSoilMoistureMin(rs.getDouble("soil_moisture_min"));
        c.setSoilMoistureMax(rs.getDouble("soil_moisture_max"));
        String date = rs.getString("planting_date");
        if (date != null) c.setPlantingDate(LocalDate.parse(date));
        String stage = rs.getString("current_stage");
        if (stage != null) try { c.setCurrentStage(CropStage.valueOf(stage)); } catch (Exception ignored) {}
        c.setActive(rs.getInt("active") == 1);
        return c;
    }
}
