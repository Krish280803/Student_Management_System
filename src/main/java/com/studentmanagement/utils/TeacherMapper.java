package com.studentmanagement.utils;

import com.studentmanagement.dto.TeacherRequestDTO;
import com.studentmanagement.dto.TeacherResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Teacher;

public class TeacherMapper {

    /**
     * Map request DTO and parent department entity to database Teacher entity.
     */
    public static Teacher toEntity(TeacherRequestDTO dto, Department department) {
        if (dto == null) {
            return null;
        }

        return Teacher.builder()
                .teacherNumber(dto.getTeacherNumber())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .specialization(dto.getSpecialization())
                .department(department)
                .build();
    }

    /**
     * Update an existing Teacher entity with fields from request DTO and new department.
     */
    public static void updateEntity(Teacher teacher, TeacherRequestDTO dto, Department department) {
        if (dto == null || teacher == null) {
            return;
        }

        teacher.setTeacherNumber(dto.getTeacherNumber());
        teacher.setFirstName(dto.getFirstName());
        teacher.setLastName(dto.getLastName());
        teacher.setEmail(dto.getEmail());
        teacher.setPhone(dto.getPhone());
        teacher.setSpecialization(dto.getSpecialization());
        teacher.setDepartment(department);
    }

    /**
     * Map database Teacher entity to output response DTO.
     */
    public static TeacherResponseDTO toResponseDTO(Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        TeacherResponseDTO.TeacherResponseDTOBuilder builder = TeacherResponseDTO.builder()
                .id(teacher.getId())
                .teacherNumber(teacher.getTeacherNumber())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .specialization(teacher.getSpecialization())
                .photoPath(teacher.getPhotoPath())
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .deletedAt(teacher.getDeletedAt());

        // Extract and flatten department information
        if (teacher.getDepartment() != null) {
            builder.departmentId(teacher.getDepartment().getId())
                   .departmentCode(teacher.getDepartment().getCode())
                   .departmentName(teacher.getDepartment().getName());
        }

        return builder.build();
    }
}
