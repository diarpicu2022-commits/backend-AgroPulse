package com.agropulse.pattern.creational.singleton;

import org.springframework.stereotype.Component;
import java.sql.*;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  PATRÓN SINGLETON — Conexión a la base de datos             ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  Garantiza una única instancia de la conexión en toda la    ║
 * ║  aplicación, evitando múltiples conexiones simultáneas.     ║
 * ║                                                             ║
 * ║  Componentes (PDF Singleton):                               ║
 * ║   - Constructor privado: impide instanciación externa       ║
 * ║   - Método estático getInstance(): punto de acceso global   ║
 * ║   - Variable estática instance: instancia única             ║
 * ║                                                             ║
 * ║  Justificación en AgroPulse:                                ║
 * ║   Una base de datos SQLite/PostgreSQL no debe tener         ║
 * ║   múltiples conexiones abiertas simultáneamente.            ║
 * ║   El Singleton garantiza que todos los DAOs compartan       ║
 * ║   exactamente la misma conexión.                            ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Component
public class DatabaseConnection {

    // Variable estática: instancia única (Singleton)
    private static DatabaseConnection instance;

    // Conexión SQLite (desarrollo local)
    private Connection localConnection;
    private static final String LOCAL_DB_URL = "jdbc:sqlite:agropulse.db";

    // Conexión PostgreSQL (producción — Supabase)
    private Connection onlineConnection;
    private boolean onlineEnabled = false;
    private String  onlineUrl     = "";

    // ── Constructor PRIVADO — impide instanciación externa ────────────
    private DatabaseConnection() {
        connectToLocal();
        initializeSchema(localConnection);
        // Intentar Supabase desde variable de entorno
        String supabaseUrl = System.getenv("SUPABASE_JDBC_URL");
        if (supabaseUrl != null && !supabaseUrl.isBlank()) {
            configureOnlineDatabase(supabaseUrl, true);
        }
    }

    /**
     * Método estático de acceso global.
     * Synchronized para garantizar thread-safety en entornos concurrentes.
     *
     * Concurrencia (Bloque 2 Diseño de Software):
     * Si dos hilos llaman a getInstance() simultáneamente sin synchronized,
     * podrían crearse dos instancias — violando el patrón.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Obtiene la conexión activa.
     * Estrategia Online-First: si Supabase está disponible, lo usa como principal.
     * Si falla, cae automáticamente a SQLite local.
     */
    public Connection getConnection() {
        if (onlineEnabled && !onlineUrl.isBlank()) {
            try {
                connectToOnline();
                if (onlineConnection != null && !onlineConnection.isClosed()) {
                    return onlineConnection; // ✅ PostgreSQL/Supabase disponible
                }
            } catch (SQLException e) {
                System.err.println("[DB] Supabase no disponible, usando SQLite: " + e.getMessage());
            }
        }
        // Fallback a SQLite local
        connectToLocal();
        return localConnection;
    }

    /** Configura y activa la base de datos online (PostgreSQL/Supabase). */
    public boolean configureOnlineDatabase(String jdbcUrl, boolean enabled) {
        this.onlineUrl     = jdbcUrl;
        this.onlineEnabled = enabled;
        if (!enabled || jdbcUrl.isBlank()) return false;
        return connectToOnline();
    }

    /** Verifica si la base de datos online está disponible. */
    public boolean isOnlineAvailable() {
        try {
            return onlineEnabled && onlineConnection != null && !onlineConnection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /** Informa qué base de datos está activa actualmente. */
    public String getActiveDatabaseName() {
        return isOnlineAvailable() ? "PostgreSQL/Supabase (PRINCIPAL)" : "SQLite Local (FALLBACK)";
    }

    // ── Métodos privados de conexión ──────────────────────────────────

    private void connectToLocal() {
        try {
            if (localConnection == null || localConnection.isClosed()) {
                localConnection = DriverManager.getConnection(LOCAL_DB_URL);
                System.out.println("[DB-Local] SQLite conectado.");
            }
        } catch (SQLException e) {
            System.err.println("[DB-Local] Error: " + e.getMessage());
        }
    }

    private boolean connectToOnline() {
        try {
            Class.forName("org.postgresql.Driver");
            onlineConnection = DriverManager.getConnection(onlineUrl);
            System.out.println("[DB-Online] PostgreSQL/Supabase conectado.");
            initializeSchema(onlineConnection);
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DB-Online] Driver PostgreSQL no encontrado.");
            return false;
        } catch (SQLException e) {
            System.err.println("[DB-Online] Error de conexión: " + e.getMessage());
            onlineConnection = null;
            return false;
        }
    }

    // ── Inicialización del esquema de la base de datos ────────────────

    /**
     * Crea todas las tablas si no existen.
     * Compatible con SQLite y PostgreSQL (DDL portable).
     */
    public void initializeSchema(Connection conn) {
        if (conn == null) return;
        try (Statement stmt = conn.createStatement()) {
            boolean isPostgres = conn.getMetaData().getDatabaseProductName()
                                     .toLowerCase().contains("postgresql");
            String autoIncrement = isPostgres ? "SERIAL PRIMARY KEY"
                                              : "INTEGER PRIMARY KEY AUTOINCREMENT";

            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                + "id         " + autoIncrement + ","
                + "username   TEXT UNIQUE NOT NULL,"
                + "password   TEXT,"
                + "full_name  TEXT,"
                + "email      TEXT,"
                + "phone      TEXT,"
                + "avatar     TEXT,"
                + "role       TEXT NOT NULL DEFAULT 'OPERATOR',"
                + "active     INTEGER NOT NULL DEFAULT 1,"
                + "created_at TEXT NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS greenhouses ("
                + "id          " + autoIncrement + ","
                + "name        TEXT NOT NULL,"
                + "location    TEXT,"
                + "description TEXT,"
                + "owner_id    INTEGER NOT NULL DEFAULT 1,"
                + "active      INTEGER NOT NULL DEFAULT 1,"
                + "created_at  TEXT NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS sensors ("
                + "id           " + autoIncrement + ","
                + "name         TEXT NOT NULL,"
                + "type         TEXT NOT NULL,"
                + "location     TEXT,"
                + "last_value   REAL DEFAULT 0.0,"
                + "active       INTEGER NOT NULL DEFAULT 1,"
                + "greenhouse_id INTEGER NOT NULL DEFAULT 1)");

            stmt.execute("CREATE TABLE IF NOT EXISTS sensor_readings ("
                + "id           " + autoIncrement + ","
                + "sensor_id    INTEGER NOT NULL,"
                + "sensor_type  TEXT NOT NULL,"
                + "value        REAL NOT NULL,"
                + "timestamp    TEXT NOT NULL,"
                + "source       TEXT DEFAULT 'MANUAL',"
                + "greenhouse_id INTEGER DEFAULT 1)");

            stmt.execute("CREATE TABLE IF NOT EXISTS crops ("
                + "id                " + autoIncrement + ","
                + "name              TEXT NOT NULL,"
                + "variety           TEXT,"
                + "temp_min          REAL NOT NULL,"
                + "temp_max          REAL NOT NULL,"
                + "humidity_min      REAL NOT NULL,"
                + "humidity_max      REAL NOT NULL,"
                + "soil_moisture_min REAL NOT NULL,"
                + "soil_moisture_max REAL NOT NULL,"
                + "planting_date     TEXT,"
                + "current_stage     TEXT DEFAULT 'SEEDING',"
                + "active            INTEGER NOT NULL DEFAULT 1)");

            stmt.execute("CREATE TABLE IF NOT EXISTS alerts ("
                + "id           " + autoIncrement + ","
                + "message      TEXT NOT NULL,"
                + "level        TEXT NOT NULL,"
                + "sent         INTEGER NOT NULL DEFAULT 0,"
                + "created_at   TEXT NOT NULL,"
                + "greenhouse_id INTEGER DEFAULT 1)");

            stmt.execute("CREATE TABLE IF NOT EXISTS actuators ("
                + "id           " + autoIncrement + ","
                + "name         TEXT NOT NULL,"
                + "type         TEXT NOT NULL,"
                + "enabled      INTEGER NOT NULL DEFAULT 0,"
                + "auto_mode    INTEGER NOT NULL DEFAULT 1,"
                + "greenhouse_id INTEGER NOT NULL DEFAULT 1,"
                + "last_toggled TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS system_logs ("
                + "id           " + autoIncrement + ","
                + "action       TEXT NOT NULL,"
                + "details      TEXT,"
                + "performed_by TEXT,"
                + "timestamp    TEXT NOT NULL)");

            // Datos por defecto solo en SQLite (desarrollo)
            if (!isPostgres) {
                stmt.execute("INSERT OR IGNORE INTO users "
                    + "(username, password, full_name, role, created_at) "
                    + "VALUES ('admin', 'admin123', 'Administrador', 'ADMIN', datetime('now'))");
                stmt.execute("INSERT OR IGNORE INTO greenhouses "
                    + "(id, name, location, description, owner_id, created_at) "
                    + "VALUES (1,'Invernadero Principal','Pasto, Nariño','Invernadero por defecto',1,datetime('now'))");
            }

            System.out.println("[DB] Esquema inicializado en "
                + (isPostgres ? "PostgreSQL" : "SQLite") + ".");

        } catch (SQLException e) {
            System.err.println("[DB] Error al inicializar esquema: " + e.getMessage());
        }
    }

    public void closeAll() {
        try {
            if (localConnection  != null) localConnection.close();
            if (onlineConnection != null) onlineConnection.close();
            System.out.println("[DB] Conexiones cerradas.");
        } catch (SQLException ignored) {}
    }
}
