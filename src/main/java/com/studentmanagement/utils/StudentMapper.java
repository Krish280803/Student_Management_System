package com.studentmanagement.utils;

import com.studentmanagement.dto.StudentRequestDTO;
import com.studentmanagement.dto.StudentResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Student;

public class StudentMapper {

    /**
     * Map request DTO and parent department entity to database Student entity.
     */
    public static Student toEntity(StudentRequestDTO dto, Department department) {
        if (dto == null) {
            return null;
        }

        return Student.builder()
                .studentNumber(dto.getStudentNumber())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .department(department)
                .build();
    }

    /**
     * Update an existing Student entity with fields from request DTO and new department.
     */
    public static void updateEntity(Student student, StudentRequestDTO dto, Department department) {
        if (dto == null || student == null) {
            return;
        }

        student.setStudentNumber(dto.getStudentNumber());
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setPhone(dto.getPhone());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setDepartment(department);
    }

    /**
     * Map database Student entity to output response DTO.
     */
    public static StudentResponseDTO toResponseDTO(Student student) {
        if (student == null) {
            return null;
        }

        StudentResponseDTO.StudentResponseDTOBuilder builder = StudentResponseDTO.builder()
                .id(student.getId())
                .studentNumber(student.getStudentNumber())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .dateOfBirth(student.getDateOfBirth())
                .photoPath(student.getPhotoPath())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .deletedAt(student.getDeletedAt());

        // Extract and flatten department information
        if (student.getDepartment() != null) {
            builder.departmentId(student.getDepartment().getId())
                   .departmentCode(student.getDepartment().getCode())
                   .departmentName(student.getDepartment().getName());
        }

        return builder.build();
    }
}
