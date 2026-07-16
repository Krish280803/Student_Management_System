package com.studentmanagement.controller;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.studentmanagement.dto.StudentResponseDTO;
import com.studentmanagement.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/students/export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Exporters Interface", description = "Endpoints for downloading student registers as Excel, PDF, or CSV")
public class ExportController {

    private final StudentService studentService;

    @GetMapping("/excel")
    @Operation(summary = "Export student list to Excel workbook spreadsheet")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        log.info("Request received to export student records to Excel");
        List<StudentResponseDTO> students = studentService.getAllStudents();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_" + getTimestamp() + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Student Registry");

            // Header Style
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Student Number", "First Name", "Last Name", "Email", "Phone", "Date of Birth", "Department"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Fill Data Rows
            int rowNum = 1;
            for (StudentResponseDTO student : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getStudentNumber());
                row.createCell(2).setCellValue(student.getFirstName());
                row.createCell(3).setCellValue(student.getLastName());
                row.createCell(4).setCellValue(student.getEmail());
                row.createCell(5).setCellValue(student.getPhone() != null ? student.getPhone() : "");
                row.createCell(6).setCellValue(student.getDateOfBirth().toString());
                row.createCell(7).setCellValue(student.getDepartmentName() != null ? student.getDepartmentName() : "N/A");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
        log.info("Excel export completed successfully");
    }

    @GetMapping("/pdf")
    @Operation(summary = "Export student list to PDF document booklet")
    public void exportToPDF(HttpServletResponse response) throws IOException {
        log.info("Request received to export student records to PDF");
        List<StudentResponseDTO> students = studentService.getAllStudents();

        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_" + getTimestamp() + ".pdf");

        try (Document document = new Document()) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // Fonts
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font dataFont = new Font(Font.HELVETICA, 9);

            // Title
            Paragraph title = new Paragraph("STUDENT MANAGEMENT SYSTEM REGISTRY", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Table configuration: 7 columns
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.5f, 2.0f, 2.0f, 3.5f, 2.5f, 3.0f});

            // Headers
            String[] headers = {"ID", "Student No", "First Name", "Last Name", "Email", "Phone", "Department"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Data Rows
            for (StudentResponseDTO student : students) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(student.getId()), dataFont)));
                table.addCell(new PdfPCell(new Phrase(student.getStudentNumber(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(student.getFirstName(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(student.getLastName(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(student.getEmail(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(student.getPhone() != null ? student.getPhone() : "", dataFont)));
                table.addCell(new PdfPCell(new Phrase(student.getDepartmentName() != null ? student.getDepartmentName() : "N/A", dataFont)));
            }

            document.add(table);
        }
        log.info("PDF export completed successfully");
    }

    @GetMapping("/csv")
    @Operation(summary = "Export student list to CSV spreadsheet text file")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        log.info("Request received to export student records to CSV");
        List<StudentResponseDTO> students = studentService.getAllStudents();

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_" + getTimestamp() + ".csv");

        try (PrintWriter writer = response.getWriter()) {
            // Write Headers
            writer.println("ID,Student Number,First Name,Last Name,Email,Phone,Date of Birth,Department");

            // Write Records
            for (StudentResponseDTO student : students) {
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                        student.getId(),
                        escapeCsvField(student.getStudentNumber()),
                        escapeCsvField(student.getFirstName()),
                        escapeCsvField(student.getLastName()),
                        escapeCsvField(student.getEmail()),
                        escapeCsvField(student.getPhone()),
                        student.getDateOfBirth().toString(),
                        escapeCsvField(student.getDepartmentName())
                );
            }
        }
        log.info("CSV export completed successfully");
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
