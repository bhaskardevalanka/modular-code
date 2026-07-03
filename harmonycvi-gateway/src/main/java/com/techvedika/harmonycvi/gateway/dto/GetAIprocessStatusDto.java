package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class GetAIprocessStatusDto {
	
	 @Schema(
		        description = "Study Instance UID", 
		        example = "1.2.156.112605.66988331192476.250613023737.2.12060.16051"
		    )
		    @NotBlank(message = "studyInstanceUid is required")
		    private String studyId;

		    @Schema(
		        description = "Access key (WADO URL encoded string)", 
		        example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%"
		    )
		    @NotBlank(message = "accessKey is required")
		    private String accessKey;

			public String getStudyId() {
				return studyId;
			}

			public void setStudyId(String studyId) {
				this.studyId = studyId;
			}

			public String getAccessKey() {
				return accessKey;
			}

			public void setAccessKey(String accessKey) {
				this.accessKey = accessKey;
			}

		    
}
