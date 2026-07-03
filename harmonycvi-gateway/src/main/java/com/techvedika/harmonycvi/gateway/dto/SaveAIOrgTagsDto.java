package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "DTO to carry image data for processing")
public class SaveAIOrgTagsDto {

    @Schema(description = "Organization ID", example = "2")
    @NotBlank(message = "orgId is required")
    private String orgId;

    @Schema(
        description = "Access key (WADO URL encoded string)",
        example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%"
    )
    @NotBlank(message = "accessKey is required")
    private String accessKey;

    @Schema(
        description = "Image data grouped by type (e.g. Magnitude, Phase)",
        example = """
        {
          "Magnitude": [["ORIGINAL","PRIMARY","M_FFE","M","FFE"]],
          "Phase": [["ORIGINAL","PRIMARY","PHASE CONTRAST M","P","PCA"]],
          "null": [["ORIGINAL","PRIMARY","MAG","RETRO","DIS2D"]]
        }
        """
    )
    private Map<String, List<List<String>>> imageData;
    
    @Schema(
            description = "Tags data grouped by type (e.g. Magnitude, Phase)",
            example = """
            {"FunctionalShortaxisview": {"protocol_names": ["sa", "BTFE_BH_SA hr",
             "cine_tf2d13_retro_SA", "SSh_cine_SA_M2D", "cine_tf2d12_retro_SA", 
             "short axis Func", "Short axis", "sax", "sBTFE_BH", "SA STACK", "CINE_SA",
              "BTFE_BH_SA", "SA", "CINE_segmented_SAX_b8", "CINE_segmented_SAX_b1",
               "CINE_segmented_SAX_b2", "CINE_segmented_SAX_b3", "CINE_segmented_SAX_b4",
                "CINE_segmented_SAX_b5", "CINE_segmented_SAX_b6", "CINE_segmented_SAX_b7",
                 "CINE_segmented_SAX_b9", "CINE_segmented_SAX_b10", "CINE_segmented_SAX_InlineVF",
                  "TRUFI SA_cine", "cine_tf2d16_retro_iPAT_10sl anonymized", "BTFE SA fb", "SSh_IR-BTFE_SA", 
                  "SHORT AXIS A Fiesta gated", "B-TFE_SA", "CINE SAX", "B-TFE_BH-SA", "short axis Func. test", 
                  "B-TFE_BH-SA CLEAR", "CINE SA", "DYN_sBTFE_3sl PRE GADO -SA", "Cine SA", 
                  "SAX", "Short Axis View", "SAx Sonic-DL 3RR BH", "cine_sax_12sl",
                   "sBTFE_M.SLICE", "csBTFE_BH SA", null, "CINE_SEG_SA", "cine_SA",
                    "tf2d18_retro_iPAT_10sl_5bh_SA", "BTFE_BH_M2D_SA", "tf2d20_retro_SHORT AXIS", 
                    "csBTFE_BH_MV", "T2_STIR_BB", "DYN_perfusion", "cine_sax_9sl"],
                     "ImagePlane": "Short Axis View", "SequenceType": "Functional"}
            """
        )
    private Map<String, Object> tagsData;

	// getters and setters
    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Map<String, List<List<String>>> getImageData() {
        return imageData;
    }

    public void setImageData(Map<String, List<List<String>>> imageData) {
        this.imageData = imageData;
    }
    
    public Map<String, Object> getTagsData() {
		return tagsData;
	}

	public void setTagsData(Map<String, Object> tagsData) {
		this.tagsData = tagsData;
	}
}
