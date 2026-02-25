package com.reporte;

import java.sql.*;
import java.util.*;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReporteDefectosExcel {

    private static final String URL = "jdbc:postgresql://localhost:5432/mi_basedatos";
    private static final String USER = "admin";
    private static final String PASSWORD = "admin123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Bloques, fechas y secuencias
            System.out.print("Ingrese bloques separados por coma (ej: Bloque 1,Bloque 2): ");
            String[] bloques = scanner.nextLine().split(",");

            System.out.print("Ingrese primera fecha (yyyy-MM-dd): ");
            java.sql.Date fecha1 = java.sql.Date.valueOf(scanner.nextLine());

            System.out.print("Ingrese primera secuencia: ");
            int secuencia1 = Integer.parseInt(scanner.nextLine());

            System.out.print("Ingrese segunda fecha (yyyy-MM-dd): ");
            java.sql.Date fecha2 = java.sql.Date.valueOf(scanner.nextLine());

            System.out.print("Ingrese segunda secuencia: ");
            int secuencia2 = Integer.parseInt(scanner.nextLine());

            // Filtros separados
            String filtros1 = """
                    FROM public.defectos_tramites
                    WHERE bloque = ?
                      AND severity = '1 - Critical'
                      AND atiende = 'iLink'
                      AND creted_by ILIKE '%ilink%'
                      AND creted_by <> 'llealg@ilink-systems.com'
                      AND fecha_reporte = ?
                      AND secuencia_reporte = ?
                    """;

            String filtros2 = """
                    FROM public.defectos_tramites
                    WHERE bloque = ?
                      AND severity = '1 - Critical'
                      AND atiende = 'iLink'
                      AND creted_by ILIKE '%ultrasist%'
                      AND creted_by <> 'llealg@ilink-systems.com'
                      AND fecha_reporte = ?
                      AND secuencia_reporte = ?
                    """;

            String[] filtros = {filtros1, filtros2};
            String[] etiquetas = {"iLink", "Ultrasist"};

            String nombreArchivo = String.format("reporte_defectos_%s_vs_%s.xlsx",
                    fecha1.toString(), fecha2.toString());

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(nombreArchivo)) {

                // Estilos
                CellStyle boldStyle = workbook.createCellStyle();
                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                boldStyle.setFont(boldFont);

                CellStyle greenStyle = workbook.createCellStyle();
                Font greenFont = workbook.createFont();
                greenFont.setColor(IndexedColors.GREEN.getIndex());
                greenStyle.setFont(greenFont);

                CellStyle redStyle = workbook.createCellStyle();
                Font redFont = workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redStyle.setFont(redFont);

                // Iterar bloques
                for (String bloqueRaw : bloques) {
                    String bloque = bloqueRaw.trim();
                    Sheet sheet = workbook.createSheet(bloque.replace(" ", "_"));
                    int rowNum = 0;

                    // Iterar filtros
                    for (int f = 0; f < filtros.length; f++) {
                        String query1 = "SELECT COUNT(*) " + filtros[f];
                        String query2 = "SELECT tipo_tramite, COUNT(*) AS total_defectos " + filtros[f] +
                                        " GROUP BY tipo_tramite ORDER BY total_defectos DESC";
                        String query3 = "SELECT tipo_tramite, COUNT(*) AS total_defectos, ARRAY_AGG(id_bug) AS defectos " + filtros[f] +
                                        " GROUP BY tipo_tramite ORDER BY total_defectos DESC";
                        String query4 = "SELECT tipo_tramite, id_bug " + filtros[f] + " ORDER BY tipo_tramite";

                        // Obtener sets de defectos para comparar secuencias
                        Set<Long> defectosSeq1 = obtenerDefectos(conn, query4, bloque, fecha1, secuencia1);
                        Set<Long> defectosSeq2 = obtenerDefectos(conn, query4, bloque, fecha2, secuencia2);

                        // Generar bloque completo
                        rowNum = generarBloque(sheet, conn, rowNum, bloque, fecha1, secuencia1,
                                               query1, query2, query3, query4, "REPORTE 1 - " + etiquetas[f], boldStyle);
                        rowNum++;
                        rowNum = generarBloque(sheet, conn, rowNum, bloque, fecha2, secuencia2,
                                               query1, query2, query3, query4, "REPORTE 2 - " + etiquetas[f], boldStyle);

                        // Comparación defectos agregados/eliminados
                        Set<Long> agregados = new HashSet<>(defectosSeq2);
                        agregados.removeAll(defectosSeq1);

                        Set<Long> eliminados = new HashSet<>(defectosSeq1);
                        eliminados.removeAll(defectosSeq2);

                        rowNum++;
                        Row titulo = sheet.createRow(rowNum++);
                        Cell tituloCell = titulo.createCell(0);
                        tituloCell.setCellValue("COMPARACION ENTRE SECUENCIAS - " + etiquetas[f]);
                        tituloCell.setCellStyle(boldStyle);

                        Row headerAg = sheet.createRow(rowNum++);
                        Cell hAg = headerAg.createCell(0);
                        hAg.setCellValue("DEFECTOS AGREGADOS (Secuencia 2)");
                        hAg.setCellStyle(boldStyle);

                        for (Long id : agregados) {
                            Row r = sheet.createRow(rowNum++);
                            Cell c = r.createCell(0);
                            c.setCellValue(id);
                            c.setCellStyle(greenStyle);
                        }

                        rowNum++;
                        Row headerEl = sheet.createRow(rowNum++);
                        Cell hEl = headerEl.createCell(0);
                        hEl.setCellValue("DEFECTOS ELIMINADOS (vs Secuencia 1)");
                        hEl.setCellStyle(boldStyle);

                        for (Long id : eliminados) {
                            Row r = sheet.createRow(rowNum++);
                            Cell c = r.createCell(0);
                            c.setCellValue(id);
                            c.setCellStyle(redStyle);
                        }

                        rowNum += 2; // Espacio antes del siguiente filtro
                    }

                    // Autoajustar columnas
                    for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);
                }

                workbook.write(fileOut);
                System.out.println("✅ Reporte comparativo generado correctamente: " + nombreArchivo);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static int generarBloque(Sheet sheet, Connection conn, int rowNum,
                                     String bloque, java.sql.Date fecha, int secuencia,
                                     String query1, String query2,
                                     String query3, String query4,
                                     String titulo, CellStyle boldStyle) throws Exception {

        Row tituloRow = sheet.createRow(rowNum++);
        Cell tituloCell = tituloRow.createCell(0);
        tituloCell.setCellValue("==== " + titulo + " ====");
        tituloCell.setCellStyle(boldStyle);

        Row infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Bloque: " + bloque + " | Fecha: " + fecha + " | Secuencia: " + secuencia);
        rowNum++;

        // SECCION 1: TOTAL
        try (PreparedStatement ps = conn.prepareStatement(query1)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            Row r = sheet.createRow(rowNum++);
            r.createCell(0).setCellValue("TOTAL DEFECTOS " + bloque);
            r.getCell(0).setCellStyle(boldStyle);
            if (rs.next()) {
                Row totalRow = sheet.createRow(rowNum++);
                totalRow.createCell(0).setCellValue(rs.getInt(1));
            }
        }
        rowNum++;

        // SECCION 2: POR TRAMITE
        Row header2 = sheet.createRow(rowNum++);
        header2.createCell(0).setCellValue("Tipo Tramite");
        header2.createCell(1).setCellValue("Total Defectos");
        header2.getCell(0).setCellStyle(boldStyle);
        header2.getCell(1).setCellStyle(boldStyle);

        try (PreparedStatement ps = conn.prepareStatement(query2)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(rs.getString("tipo_tramite"));
                r.createCell(1).setCellValue(rs.getInt("total_defectos"));
            }
        }
        rowNum++;

        // SECCION 3: AGRUPADO
        Row header3 = sheet.createRow(rowNum++);
        header3.createCell(0).setCellValue("Tipo Tramite");
        header3.createCell(1).setCellValue("Total");
        header3.createCell(2).setCellValue("IDs Defectos");
        for (int i = 0; i <= 2; i++) header3.getCell(i).setCellStyle(boldStyle);

        try (PreparedStatement ps = conn.prepareStatement(query3)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(rs.getString("tipo_tramite"));
                r.createCell(1).setCellValue(rs.getInt("total_defectos"));
                Array array = rs.getArray("defectos");
                r.createCell(2).setCellValue(array != null ? Arrays.toString((Object[]) array.getArray()) : "[]");
            }
        }
        rowNum++;

        // SECCION 4: DETALLE
        Row header4 = sheet.createRow(rowNum++);
        header4.createCell(0).setCellValue("Tipo Tramite");
        header4.createCell(1).setCellValue("ID Bug");
        header4.getCell(0).setCellStyle(boldStyle);
        header4.getCell(1).setCellStyle(boldStyle);

        try (PreparedStatement ps = conn.prepareStatement(query4)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(rs.getString("tipo_tramite"));
                r.createCell(1).setCellValue(rs.getLong("id_bug"));
            }
        }

        return rowNum;
    }

    private static Set<Long> obtenerDefectos(Connection conn, String query,
                                             String bloque, java.sql.Date fecha, int secuencia) throws Exception {
        Set<Long> defectos = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                defectos.add(rs.getLong("id_bug"));
            }
        }
        return defectos;
    }
}