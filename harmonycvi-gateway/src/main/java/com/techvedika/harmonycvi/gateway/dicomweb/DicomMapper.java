package com.techvedika.harmonycvi.gateway.dicomweb;

import com.fasterxml.jackson.databind.JsonNode;

public class DicomMapper {

//    public static StudyDTO mapStudy(JsonNode studyNode) {
//        StudyDTO dto = new StudyDTO();
//
//        dto.setStudyInstanceUID(getTagValue(studyNode, "0020000D"));
//        dto.setStudyDescription(getTagValue(studyNode, "00081030"));
//        dto.setStudyDate(getTagValue(studyNode, "00080020"));
//        dto.setStudyTime(getTagValue(studyNode, "00080030"));
//        dto.setPatientSex(getTagValue(studyNode, "00100040"));
//        dto.setPatientBirthDate(getTagValue(studyNode, "00100030"));
//        dto.setProcedureCodes(getTagValue(studyNode, "00081032"));
//        dto.setSeriesCount(Integer.valueOf(getTagValue(studyNode, "00201206")));
//
//        dto.setPatientName(buildPatientName(studyNode));
//
//        return dto;
//    }
    
    public static StudyDTO mapStudy(JsonNode studyNode) {
        try {
            StudyDTO dto = new StudyDTO();

            dto.setStudyInstanceUID(getTagValue(studyNode, "0020000D"));
            dto.setStudyDescription(getTagValue(studyNode, "00081030"));
            dto.setStudyDate(getTagValue(studyNode, "00080020"));
            dto.setStudyTime(getTagValue(studyNode, "00080030"));
            dto.setPatientSex(getTagValue(studyNode, "00100040"));
            dto.setPatientBirthDate(getTagValue(studyNode, "00100030"));
            dto.setProcedureCodes(getTagValue(studyNode, "00081032"));

            String sc = getTagValue(studyNode, "00201206");
            dto.setSeriesCount(sc != null ? Integer.valueOf(sc) : 0);

            dto.setPatientName(buildPatientName(studyNode));

            return dto;

        } catch (Exception ex) {
        	ex.printStackTrace();
            System.out.println("Error mapping study --> " + ex.getMessage());
            return null;  // Will skip this study safely
        }
    }


    public static SeriesDTO mapSeries(JsonNode seriesNode, Integer seriesCount) {
        SeriesDTO dto = new SeriesDTO();

        dto.setSeriesInstanceUID(getTagValue(seriesNode, "0020000E"));
        dto.setModality(getTagValue(seriesNode, "00080060"));
        dto.setInstitutionName(getTagValue(seriesNode, "00080080"));
        dto.setDepartment(getTagValue(seriesNode, "00081040"));
        dto.setPhysicianName(getTagValue(seriesNode, "00081050"));
        dto.setStationName(getTagValue(seriesNode, "00081010"));
        dto.setPpsStartDate(getTagValue(seriesNode, "00400244"));
        dto.setPpsStartTime(getTagValue(seriesNode, "00400245"));

        return dto;
    }

    private static String getTagValue(JsonNode node, String tag) {
        if (!node.has(tag)) {
            return "";
        }

        JsonNode tagNode = node.get(tag);

        // If the tag itself has a Value array
        JsonNode values = tagNode.get("Value");
        if (values == null || !values.isArray() || values.isEmpty()) {
            return "";
        }

        JsonNode firstValue = values.get(0);
        if (firstValue.isTextual()) {
            // Direct string value
            return firstValue.asText("");
        } else if (firstValue.isObject() && firstValue.has("Alphabetic")) {
            // PersonName object, get Alphabetic
            String alphabetic = firstValue.get("Alphabetic").asText("");
            if (!alphabetic.isEmpty() && !alphabetic.equalsIgnoreCase("UNKNOWN")) {
                return alphabetic.replace('^', ' ').trim();
            }
            return "";
        }

        return "";
    }

    private static String buildPatientName(JsonNode studyNode) {
        if (!studyNode.has("00100010")) return "Anonymous";

        JsonNode nameArr = studyNode.get("00100010");
        if (!nameArr.isObject() || nameArr.isEmpty() || !nameArr.has("Value")) return "Anonymous";

        JsonNode valueArr = nameArr.get("Value");
        if (!valueArr.isArray() || valueArr.isEmpty()) return "Anonymous";

        JsonNode nameValue = valueArr.get(0);
        String alphabetic = nameValue.has("Alphabetic") ? nameValue.get("Alphabetic").asText("") : "";

        if (!alphabetic.isEmpty() && !alphabetic.equalsIgnoreCase("UNKNOWN")) {
            return alphabetic.replace("^", " ").trim();
        }

        // Fallback: PatientID
        String patientId = getTagValue(studyNode, "00100020");
        if (!patientId.isEmpty()) return "UNKNOWN " + patientId;

        return "Anonymous";
    }
}
