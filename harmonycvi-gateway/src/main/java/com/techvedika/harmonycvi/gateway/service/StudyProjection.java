package com.techvedika.harmonycvi.gateway.service;

public interface StudyProjection {

	String getStudyInstanceUID();

	String getStudyID();

	String getStudyDescription();

	String getPatientFullName(); // Concatenated full name

	Boolean getIsAIProccessed();

	String getStatus();
	
	String getIssuerOfAdmissionID();
}
