package com.example.clinic.repository;

import com.example.clinic.entity.Xray;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XrayRepository extends JpaRepository<Xray, Long> {

    List<Xray> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}