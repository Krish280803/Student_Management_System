package com.studentmanagement.utils;

import com.studentmanagement.dto.TeacherRequestDTO;
import com.studentmanagement.dto.TeacherResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Teacher;

public class TeacherMapper {

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
                .hireDate(dto.getHireDate())
                .department(department)
                .build();
    }

    public static void updateEntity(Teacher teacher, TeacherRequestDTO dto, Department department) {
        if (dto == null || teacher == null) {
            return;
        }

        teacher.setTeacherNumber(dto.getTeacherNumber());
        teacher.setFirstName(dto.getFirstName());
        teacher.setLastName(dto.getLastName());
        teacher.setEmail(dto.getEmail());
        teacher.setPhone(dto.getPhone());
        teacher.setHireDate(dto.getHireDate());
        teacher.setDepartment(department);
    }

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
                .hireDate(teacher.getHireDate())
                .photoPath(teacher.getPhotoPath())
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .deletedAt(teacher.getDeletedAt());

        if (teacher.getDepartment() != null) {
            builder.departmentId(teacher.getDepartment().getId())
                   .departmentCode(teacher.getDepartment().getCode())
                   .departmentName(teacher.getDepartment().getName());
        }

        return builder.build();
    }
}
