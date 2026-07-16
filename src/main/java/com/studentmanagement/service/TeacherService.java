package com.studentmanagement.service;

import com.studentmanagement.dto.TeacherRequestDTO;
import com.studentmanagement.dto.TeacherResponseDTO;

import java.util.List;

public interface TeacherService {

    TeacherResponseDTO createTeacher(TeacherRequestDTO request);

    TeacherResponseDTO getTeacherById(Long id);

    TeacherResponseDTO getTeacherByNumber(String teacherNumber);

    TeacherResponseDTO getTeacherByEmail(String email);

    List<TeacherResponseDTO> getAllTeachers();

    List<TeacherResponseDTO> getTeachersByDepartment(Long departmentId);

    TeacherResponseDTO updateTeacher(Long id, TeacherRequestDTO request);

    void deleteTeacher(Long id);

    List<TeacherResponseDTO> getSoftDeletedTeachers();

    void restoreTeacher(Long id);

    TeacherResponseDTO updatePhoto(Long id, String photoPath);
}
