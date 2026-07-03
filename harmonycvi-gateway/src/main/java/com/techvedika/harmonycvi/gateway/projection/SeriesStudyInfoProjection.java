package com.techvedika.harmonycvi.gateway.projection;

public interface SeriesStudyInfoProjection {
    String getInstitutionName();
    String getInstitutionalDepartmentName();
    String getPerformedProcedureStepStartDate();
    String getPerformedProcedureStepStartTime();
    String getStationName();
    String getModality();
    String getPerformingPhysicianName();
    Long getSeriesCount();
}
