package com.techvedika.harmonycvi.gateway.util;

public enum StudyStatus {
    UPLOADED_TO_S3(0),
    DELETED_FROM_PACS(1),
    DELETED_FROM_DB(2);

    private final int statusCode;

    StudyStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static StudyStatus fromStatusCode(int statusCode) {
        for (StudyStatus status : StudyStatus.values()) {
            if (status.getStatusCode() == statusCode) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + statusCode);
    }
}


/*
Here is use of this above enum class under Archive Study entity
@Column(name = "status")
@Enumerated(EnumType.ORDINAL)  // OR EnumType.STRING if you want to store the string value
private StudyStatus status;
*/