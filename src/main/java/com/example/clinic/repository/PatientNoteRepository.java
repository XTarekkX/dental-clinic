package com.example.clinic.repository;

import com.example.clinic.entity.PatientNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientNoteRepository extends JpaRepository<PatientNote, Long> {

    List<PatientNote> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}