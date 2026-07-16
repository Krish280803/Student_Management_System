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
import com.studentmanagement.dto.TeacherResponseDTO;
import com.studentmanagement.dto.ExamResultResponseDTO;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.TeacherService;
import com.studentmanagement.service.ExamResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Exporters Interface", description = "Endpoints for downloading registers as Excel, PDF, or CSV")
public class ExportController {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ExamResultService examResultService;

    // =========================================================================
    // Student Exports
    // =========================================================================
    @GetMapping("/api/students/export/excel")
    @Operation(summary = "Export student list to Excel workbook spreadsheet")
    public void exportStudentsToExcel(HttpServletResponse response) throws IOException {
        log.info("Request received to export student records to Excel");
        List<StudentResponseDTO> students = studentService.getAllStudents();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_" + getTimestamp() + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Student Registry");

            CellStyle headerCellStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Student Number", "First Name", "Last Name", "Email", "Phone", "Date of Birth", "Department"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

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

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
        log.info("Excel student export completed successfully");
    }

    @GetMapping("/api/students/export/pdf")
    @Operation(summary = "Export student list to PDF document booklet")
    public void exportStudentsToPDF(HttpServletResponse response) throws IOException {
        log.info("Request received to export student records to PDF");
        List<StudentResponseDTO> students = studentService.getAllStudents();

        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_" + getTimestamp() + ".pdf");

        try (Document document = new Document()) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font dataFont = new Font(Font.HELVETICA, 9);

            Paragraph title = new Paragraph("STUDENT MANAGEMENT SYSTEM REGISTRY", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.5f, 2.0f, 2.0f, 3.5f, 2.5f, 3.0f});

            String[] headers = {"ID", "Student No", "First Name", "Last Name", "Email", "Phone", "Department"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }

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
        log.info("PDF student export completed successfully");
    }

    @GetMapping("/api/students/export/csv")
    @Operation(summary = "Export student list to CSV spreadsheet text file")
    public void exportStudentsToCSV(HttpServletResponse response) throws IOException {
        log.info("Request received to export student records to CSV");
        List<StudentResponseDTO> students = studentService.getAllStudents();

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_" + getTimestamp() + ".csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Student Number,First Name,Last Name,Email,Phone,Date of Birth,Department");

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
        log.info("CSV student export completed successfully");
    }

    @GetMapping("/api/students/{id}/marksheet/pdf")
    @Operation(summary = "Export a single student's marksheet to a PDF document")
    public void exportStudentMarksheetToPDF(@PathVariable Long id, HttpServletResponse response) throws IOException {
        log.info("Request received to export marksheet to PDF for student ID: {}", id);
        
        StudentResponseDTO student = studentService.getStudentById(id);
        List<ExamResultResponseDTO> results = examResultService.getResultsByStudentId(id);

        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=marksheet_" + student.getStudentNumber() + ".pdf");

        try (Document document = new Document()) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font dataFont = new Font(Font.HELVETICA, 10);
            Font tableHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD);

            Paragraph title = new Paragraph("OFFICIAL ACADEMIC MARKSHEET", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Student Metadata Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);
            infoTable.setWidths(new float[]{3.0f, 7.0f});

            addInfoRow(infoTable, "Student Name:", student.getFirstName() + " " + student.getLastName(), labelFont, dataFont);
            addInfoRow(infoTable, "Student Number:", student.getStudentNumber(), labelFont, dataFont);
            addInfoRow(infoTable, "Department:", student.getDepartmentName() != null ? student.getDepartmentName() : "N/A", labelFont, dataFont);
            addInfoRow(infoTable, "Date of Birth:", student.getDateOfBirth().toString(), labelFont, dataFont);
            document.add(infoTable);

            // Results Ledger Table
            Paragraph resultsTitle = new Paragraph("REGISTERED COURSE EXAMINATION RESULTS", labelFont);
            resultsTitle.setSpacingAfter(10);
            document.add(resultsTitle);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.0f, 4.0f, 1.5f, 1.5f});

            String[] headers = {"Exam Title", "Course Name", "Marks Obtained", "Final Grade"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (ExamResultResponseDTO result : results) {
                table.addCell(new PdfPCell(new Phrase(result.getExamName(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(result.getCourseName() != null ? result.getCourseName() : "N/A", dataFont)));
                table.addCell(new PdfPCell(new Phrase(result.getMarksObtained() + " / " + result.getMaxMarks(), dataFont)));
                
                PdfPCell gradeCell = new PdfPCell(new Phrase(result.getGrade(), dataFont));
                gradeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(gradeCell);
            }

            document.add(table);
        }
        log.info("PDF marksheet export completed successfully");
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font dataFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(0);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, dataFont));
        valueCell.setBorder(0);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    // =========================================================================
    // Teacher Exports
    // =========================================================================
    @GetMapping("/api/teachers/export/excel")
    @Operation(summary = "Export teacher list to Excel workbook spreadsheet")
    public void exportTeachersToExcel(HttpServletResponse response) throws IOException {
        log.info("Request received to export teacher records to Excel");
        List<TeacherResponseDTO> teachers = teacherService.getAllTeachers();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teachers_" + getTimestamp() + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Teacher Registry");

            CellStyle headerCellStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Teacher Number", "First Name", "Last Name", "Email", "Phone", "Hire Date", "Department"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (TeacherResponseDTO teacher : teachers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(teacher.getId());
                row.createCell(1).setCellValue(teacher.getTeacherNumber());
                row.createCell(2).setCellValue(teacher.getFirstName());
                row.createCell(3).setCellValue(teacher.getLastName());
                row.createCell(4).setCellValue(teacher.getEmail());
                row.createCell(5).setCellValue(teacher.getPhone() != null ? teacher.getPhone() : "");
                row.createCell(6).setCellValue(teacher.getHireDate().toString());
                row.createCell(7).setCellValue(teacher.getDepartmentName() != null ? teacher.getDepartmentName() : "N/A");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
        log.info("Excel teacher export completed successfully");
    }

    @GetMapping("/api/teachers/export/pdf")
    @Operation(summary = "Export teacher list to PDF document booklet")
    public void exportTeachersToPDF(HttpServletResponse response) throws IOException {
        log.info("Request received to export teacher records to PDF");
        List<TeacherResponseDTO> teachers = teacherService.getAllTeachers();

        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teachers_" + getTimestamp() + ".pdf");

        try (Document document = new Document()) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font dataFont = new Font(Font.HELVETICA, 9);

            Paragraph title = new Paragraph("TEACHER MANAGEMENT SYSTEM REGISTRY", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.5f, 2.0f, 2.0f, 3.5f, 2.5f, 3.0f});

            String[] headers = {"ID", "Teacher No", "First Name", "Last Name", "Email", "Phone", "Department"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (TeacherResponseDTO teacher : teachers) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(teacher.getId()), dataFont)));
                table.addCell(new PdfPCell(new Phrase(teacher.getTeacherNumber(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(teacher.getFirstName(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(teacher.getLastName(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(teacher.getEmail(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(teacher.getPhone() != null ? teacher.getPhone() : "", dataFont)));
                table.addCell(new PdfPCell(new Phrase(teacher.getDepartmentName() != null ? teacher.getDepartmentName() : "N/A", dataFont)));
            }

            document.add(table);
        }
        log.info("PDF teacher export completed successfully");
    }

    @GetMapping("/api/teachers/export/csv")
    @Operation(summary = "Export teacher list to CSV spreadsheet text file")
    public void exportTeachersToCSV(HttpServletResponse response) throws IOException {
        log.info("Request received to export teacher records to CSV");
        List<TeacherResponseDTO> teachers = teacherService.getAllTeachers();

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teachers_" + getTimestamp() + ".csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Teacher Number,First Name,Last Name,Email,Phone,Hire Date,Department");

            for (TeacherResponseDTO teacher : teachers) {
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                        teacher.getId(),
                        escapeCsvField(teacher.getTeacherNumber()),
                        escapeCsvField(teacher.getFirstName()),
                        escapeCsvField(teacher.getLastName()),
                        escapeCsvField(teacher.getEmail()),
                        escapeCsvField(teacher.getPhone()),
                        teacher.getHireDate().toString(),
                        escapeCsvField(teacher.getDepartmentName())
                );
            }
        }
        log.info("CSV teacher export completed successfully");
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================
    private CellStyle createHeaderStyle(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        return headerCellStyle;
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
