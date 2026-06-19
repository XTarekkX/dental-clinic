package com.example.clinic.mapper;

import com.example.clinic.dto.response.XrayResponse;
import com.example.clinic.entity.Xray;
import org.springframework.stereotype.Component;

@Component
public class XrayMapper {

    // baseUrl is injected so we can build a proper URL for the frontend
    // e.g. http://localhost:8080/api/v1/xrays/12/file
    public XrayResponse toResponse(Xray xray, String baseUrl) {
        return new XrayResponse(
                xray.getId(),

                xray.getPatient().getId(),

                xray.getDoctor().getId(),
                xray.getDoctor().getFirstName()
                        + " " + xray.getDoctor().getLastName(),

                xray.getAppointment() != null
                        ? xray.getAppointment().getId() : null,

                // Build the URL the frontend calls to display the image
                baseUrl + "/api/v1/xrays/" + xray.getId() + "/file",
                xray.getFileName(),

                xray.getToothNumber(),
                xray.getDescription(),

                xray.getTakenAt(),
                xray.getCreatedAt()
        );
    }
}