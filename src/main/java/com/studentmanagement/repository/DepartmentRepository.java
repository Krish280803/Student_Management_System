package com.studentmanagement.repository;

import com.studentmanagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    /**
     * Retrieve active department by its code (e.g. CS).
     */
    Optional<Department> findByCode(String code);
}
