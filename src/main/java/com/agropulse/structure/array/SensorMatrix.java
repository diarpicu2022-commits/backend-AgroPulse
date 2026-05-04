package com.agropulse.structure.array;

/**
 * ESTRUCTURA: ARRAY BIDIMENSIONAL (matriz) de lecturas.
 * PDF Arrays: "Tiene dos dimensiones, es decir, filas y columnas".
 *
 * En AgroPulse: filas = zonas del invernadero, columnas = tipos de lectura.
 * Permite visualizar el mapa completo de valores del invernadero.
 */
public class SensorMatrix {

    private final double[][] matrix;   // Array bidimensional: [zona][tipoSensor]
    private final int rows;            // Numero de zonas
    private final int cols;            // Numero de tipos de sensor
    private final String[] rowLabels;  // Nombres de las zonas
    private final String[] colLabels;  // Nombres de los sensores

    public SensorMatrix(String[] zones, String[] sensorNames) {
        this.rows      = zones.length;
        this.cols      = sensorNames.length;
        this.matrix    = new double[rows][cols];
        this.rowLabels = zones;
        this.colLabels = sensorNames;
    }

    /** Acceso a elemento — PDF: "Acceder e imprimir al elemento de la fila y columna". */
    public double get(int row, int col) {
        validateIndices(row, col);
        return matrix[row][col];
    }

    /** Modificar elemento. */
    public void set(int row, int col, double value) {
        validateIndices(row, col);
        matrix[row][col] = value;
    }

    /** Obtener toda una fila (zona del invernadero). */
    public double[] getRow(int row) {
        validateRow(row);
        return matrix[row].clone();
    }

    /** Obtener toda una columna (todas las zonas para un tipo de sensor). */
    public double[] getColumn(int col) {
        validateCol(col);
        double[] column = new double[rows];
        for (int r = 0; r < rows; r++) column[r] = matrix[r][col];
        return column;
    }

    /** Promedio de toda la matriz. */
    public double globalAverage() {
        double sum = 0; int count = 0;
        for (double[] row : matrix) for (double v : row) { sum += v; count++; }
        return count > 0 ? sum / count : 0;
    }

    private void validateIndices(int r, int c) {
        if (r<0||r>=rows) throw new IndexOutOfBoundsException("Fila "+r+" invalida");
        if (c<0||c>=cols) throw new IndexOutOfBoundsException("Columna "+c+" invalida");
    }
    private void validateRow(int r) { if (r<0||r>=rows) throw new IndexOutOfBoundsException("Fila "+r+" invalida"); }
    private void validateCol(int c) { if (c<0||c>=cols) throw new IndexOutOfBoundsException("Columna "+c+" invalida"); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SensorMatrix [zonas x sensores]:\n");
        sb.append("           ");
        for (String col : colLabels) sb.append(String.format("%-12s", col));
        sb.append("\n");
        for (int r = 0; r < rows; r++) {
            sb.append(String.format("%-10s ", rowLabels[r]));
            for (int c = 0; c < cols; c++) sb.append(String.format("%-12.1f", matrix[r][c]));
            sb.append("\n");
        }
        return sb.toString();
    }
}