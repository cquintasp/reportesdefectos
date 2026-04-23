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
                      AND severity IN ('1 - Critical', '2 - High', '3 - Medium')
                      AND atiende = 'iLink'
                      AND creted_by ILIKE '%ilink%'
                      AND creted_by <> 'llealg@ilink-systems.com'
                      AND fecha_reporte = ?
                      AND secuencia_reporte = ?
                    """;

            String filtros2 = """
                    FROM public.defectos_tramites
                    WHERE bloque = ?
                      AND severity IN ('1 - Critical', '2 - High', '3 - Medium')
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
                    Sheet tipoTramitesSheet = workbook.createSheet(bloque.replace(" ", "_") + "_Tipos_Tramite");
                    int tipoRowNum = 0;
                    int rowNum = 0;
                    int totalSeq1iLink = 0;
                    int totalSeq1Ultrasist = 0;
                    int totalSeq2iLink = 0;
                    int totalSeq2Ultrasist = 0;

                    // Hoja de tipos de trámite por bloque
                    Row tipoTitle = tipoTramitesSheet.createRow(tipoRowNum++);
                    Cell tipoTitleCell = tipoTitle.createCell(0);
                    tipoTitleCell.setCellValue("TIPOS DE TRAMITE DEL BLOQUE: " + bloque);
                    tipoTitleCell.setCellStyle(boldStyle);

                    Row tipoHeader = tipoTramitesSheet.createRow(tipoRowNum++);
                    tipoHeader.createCell(0).setCellValue("Tipo Tramite");
                    tipoHeader.getCell(0).setCellStyle(boldStyle);

                    String tiposQuery = "SELECT DISTINCT tipo_tramite " +
                                        "FROM public.defectos_tramites " +
                                        "WHERE bloque = ? " +
                                        "  AND severity IN ('1 - Critical', '2 - High', '3 - Medium') " +
                                        "  AND atiende = 'iLink' " +
                                        "  AND (creted_by ILIKE '%ilink%' OR creted_by ILIKE '%ultrasist%') " +
                                        "  AND creted_by <> 'llealg@ilink-systems.com' " +
                                        "ORDER BY tipo_tramite";
                    try (PreparedStatement psTipos = conn.prepareStatement(tiposQuery)) {
                        psTipos.setString(1, bloque);
                        ResultSet rsTipos = psTipos.executeQuery();
                        while (rsTipos.next()) {
                            Row tipoRow = tipoTramitesSheet.createRow(tipoRowNum++);
                            tipoRow.createCell(0).setCellValue(rsTipos.getString("tipo_tramite"));
                        }
                    }

                    // Iterar filtros
                    for (int f = 0; f < filtros.length; f++) {
                        String query1 = "SELECT COUNT(*) " + filtros[f];
                        String query2 = "SELECT tipo_tramite, severity, COUNT(*) AS total_defectos " + filtros[f] +
                                        " GROUP BY tipo_tramite, severity ORDER BY severity ASC, total_defectos DESC";
                        String query3 = "SELECT tipo_tramite, severity, COUNT(*) AS total_defectos, ARRAY_AGG(id_bug) AS defectos " + filtros[f] +
                                        " GROUP BY tipo_tramite, severity ORDER BY severity ASC, total_defectos DESC";
                        String query4 = "SELECT tipo_tramite, severity, id_bug " + filtros[f] + " ORDER BY severity ASC, tipo_tramite ASC";

                        // Obtener sets de defectos para comparar secuencias
                        Set<Long> defectosSeq1 = obtenerDefectos(conn, query4, bloque, fecha1, secuencia1);
                        Set<Long> defectosSeq2 = obtenerDefectos(conn, query4, bloque, fecha2, secuencia2);

                        // Generar bloque completo para secuencia 1
                        int[] result1 = generarBloque(sheet, conn, rowNum, bloque, fecha1, secuencia1,
                                               query1, query2, query3, query4, filtros[f], "REPORTE 1 - " + etiquetas[f], boldStyle);
                        rowNum = result1[0];
                        int total1 = result1[1];
                        if (f == 0) totalSeq1iLink = total1;
                        else if (f == 1) totalSeq1Ultrasist = total1;

                        rowNum++;
                        // Generar bloque completo para secuencia 2
                        int[] result2 = generarBloque(sheet, conn, rowNum, bloque, fecha2, secuencia2,
                                               query1, query2, query3, query4, filtros[f], "REPORTE 2 - " + etiquetas[f], boldStyle);
                        rowNum = result2[0];
                        int total2 = result2[1];
                        if (f == 0) totalSeq2iLink = total2;
                        else if (f == 1) totalSeq2Ultrasist = total2;

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

                    // Desglose de totales por origen antes del total combinado
                    Row desgloseTitle = sheet.createRow(rowNum++);
                    Cell desgloseTitleCell = desgloseTitle.createCell(0);
                    desgloseTitleCell.setCellValue("DESGLOSE TOTAL DEFECTOS SECUENCIA 1:");
                    desgloseTitleCell.setCellStyle(boldStyle);

                    Row desgloseRow1 = sheet.createRow(rowNum++);
                    desgloseRow1.createCell(0).setCellValue("Total Secuencia 1 iLink:");
                    desgloseRow1.createCell(1).setCellValue(totalSeq1iLink);

                    Row desgloseRow2 = sheet.createRow(rowNum++);
                    desgloseRow2.createCell(0).setCellValue("Total Secuencia 1 Ultrasist:");
                    desgloseRow2.createCell(1).setCellValue(totalSeq1Ultrasist);

                    rowNum++;

                    // Desglose total de secuencia 2
                    Row desglose2Title = sheet.createRow(rowNum++);
                    Cell desglose2TitleCell = desglose2Title.createCell(0);
                    desglose2TitleCell.setCellValue("DESGLOSE TOTAL DEFECTOS SECUENCIA 2:");
                    desglose2TitleCell.setCellStyle(boldStyle);

                    Row desglose2Row1 = sheet.createRow(rowNum++);
                    desglose2Row1.createCell(0).setCellValue("Total Secuencia 2 iLink:");
                    desglose2Row1.createCell(1).setCellValue(totalSeq2iLink);

                    Row desglose2Row2 = sheet.createRow(rowNum++);
                    desglose2Row2.createCell(0).setCellValue("Total Secuencia 2 Ultrasist:");
                    desglose2Row2.createCell(1).setCellValue(totalSeq2Ultrasist);

                    rowNum++;

                    // Agregar renglón con la suma total de secuencia 1
                    Row sumaRow = sheet.createRow(rowNum++);
                    Cell sumaCell = sumaRow.createCell(0);
                    sumaCell.setCellValue("SUMA TOTAL DEFECTOS SECUENCIA 1 (iLink + Ultrasist): " + (totalSeq1iLink + totalSeq1Ultrasist));
                    sumaCell.setCellStyle(boldStyle);

                    // Agregar renglón con la suma total de secuencia 2
                    Row suma2Row = sheet.createRow(rowNum++);
                    Cell suma2Cell = suma2Row.createCell(0);
                    suma2Cell.setCellValue("SUMA TOTAL DEFECTOS SECUENCIA 2 (iLink + Ultrasist): " + (totalSeq2iLink + totalSeq2Ultrasist));
                    suma2Cell.setCellStyle(boldStyle);

                    // Autoajustar columnas
                    for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);
                    tipoTramitesSheet.autoSizeColumn(0);
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

    private static int[] generarBloque(Sheet sheet, Connection conn, int rowNum,
                                     String bloque, java.sql.Date fecha, int secuencia,
                                     String query1, String query2,
                                     String query3, String query4, String filtro,
                                     String titulo, CellStyle boldStyle) throws Exception {

        Row tituloRow = sheet.createRow(rowNum++);
        Cell tituloCell = tituloRow.createCell(0);
        tituloCell.setCellValue("==== " + titulo + " ====");
        tituloCell.setCellStyle(boldStyle);

        Row infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Bloque: " + bloque + " | Fecha: " + fecha + " | Secuencia: " + secuencia);
        rowNum++;

        // SECCION 1: TOTAL
        int totalDefectos = 0;
        try (PreparedStatement ps = conn.prepareStatement(query1)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            Row r = sheet.createRow(rowNum++);
            r.createCell(0).setCellValue("TOTAL DEFECTOS " + bloque);
            r.getCell(0).setCellStyle(boldStyle);
            if (rs.next()) {
                totalDefectos = rs.getInt(1);
                Row totalRow = sheet.createRow(rowNum++);
                totalRow.createCell(0).setCellValue(totalDefectos);
            }
        }
        rowNum++;

        // SECCION 2: POR TRAMITE
        Row header2 = sheet.createRow(rowNum++);
        header2.createCell(0).setCellValue("Tipo Tramite");
        header2.createCell(1).setCellValue("Severidad");
        header2.createCell(2).setCellValue("Total Defectos");
        header2.getCell(0).setCellStyle(boldStyle);
        header2.getCell(1).setCellStyle(boldStyle);
        header2.getCell(2).setCellStyle(boldStyle);

        try (PreparedStatement ps = conn.prepareStatement(query2)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(rs.getString("tipo_tramite"));
                r.createCell(1).setCellValue(rs.getString("severity"));
                r.createCell(2).setCellValue(rs.getInt("total_defectos"));
            }
        }
        rowNum++;

        // SECCION 3: AGRUPADO
        Row header3 = sheet.createRow(rowNum++);
        header3.createCell(0).setCellValue("Tipo Tramite");
        header3.createCell(1).setCellValue("Severidad");
        header3.createCell(2).setCellValue("Total");
        header3.createCell(3).setCellValue("IDs Defectos");
        for (int i = 0; i <= 3; i++) header3.getCell(i).setCellStyle(boldStyle);

        try (PreparedStatement ps = conn.prepareStatement(query3)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(rs.getString("tipo_tramite"));
                r.createCell(1).setCellValue(rs.getString("severity"));
                r.createCell(2).setCellValue(rs.getInt("total_defectos"));
                Array array = rs.getArray("defectos");
                r.createCell(3).setCellValue(array != null ? Arrays.toString((Object[]) array.getArray()) : "[]");
            }
        }
        rowNum++;

        // SECCION 4: DETALLE
        Row header4 = sheet.createRow(rowNum++);
        header4.createCell(0).setCellValue("Tipo Tramite");
        header4.createCell(1).setCellValue("Severidad");
        header4.createCell(2).setCellValue("ID Bug");
        header4.getCell(0).setCellStyle(boldStyle);
        header4.getCell(1).setCellStyle(boldStyle);
        header4.getCell(2).setCellStyle(boldStyle);

        try (PreparedStatement ps = conn.prepareStatement(query4)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(rs.getString("tipo_tramite"));
                r.createCell(1).setCellValue(rs.getString("severity"));
                r.createCell(2).setCellValue(rs.getLong("id_bug"));
            }
        }

        rowNum++;

        // SECCION 5: TOTAL POR SEVERIDAD
        Row header5 = sheet.createRow(rowNum++);
        header5.createCell(0).setCellValue("Severidad");
        header5.createCell(1).setCellValue("Total Defectos");
        header5.getCell(0).setCellStyle(boldStyle);
        header5.getCell(1).setCellStyle(boldStyle);

        String query5 = "SELECT severity, COUNT(*) AS total_defectos " + filtro + " GROUP BY severity ORDER BY severity ASC";
        try (PreparedStatement ps = conn.prepareStatement(query5)) {
            ps.setString(1, bloque);
            ps.setDate(2, fecha);
            ps.setInt(3, secuencia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(rs.getString("severity"));
                r.createCell(1).setCellValue(rs.getInt("total_defectos"));
            }
        }

        return new int[]{rowNum, totalDefectos};
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