package com.techvedika.harmonycvi.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class SaveGLSSeriesParamsDto {

    @Schema(description = "Parameter object containing strain and peak time values")
    private Parameter parameter;

    @Schema(
        description = "Graph data points for different strain segments",
        example = "{\"BasalAnteriorStrain\": [0.1, 0.2, 0.3], \"MidAnteriorStrain\": [0.05, 0.1, 0.15]}"
    )
    private Map<String, List<Double>> graph;

    @Schema(description = "Type of process", example = "preprocess")
    private String type;

    @Schema(description = "Two chamber series instance UID", example = "1.2.840.113619.2.55.3.604688433.819.1591189386.467")
    private String twoChSeriesInstanceUID;

    @Schema(description = "Four chamber series instance UID", example = "1.2.840.113619.2.55.3.604688433.819.1591189386.468")
    private String fourChSeriesInstanceUID;

    @Schema(description = "Study Instance UID", example = "1.2.156.112605.66988331192476.250613023737.2.12060.16051")
    @NotBlank(message = "studyInstanceUid is required")
    private String studyInstanceUid;

    @Schema(description = "Access key (WADO URL encoded string)", example = "-arc%2Faets%2FDCM4CHEE%2Fwado%3FrequestType%3DWADO%")
    @NotBlank(message = "accessKey is required")
    private String accessKey;

    @Schema(description = "Four chamber slice location", example = "2.56")
    private Double fourChSlicelocation;

    @Schema(description = "Two chamber slice location", example = "3.12")
    private Double twoChSlicelocation;

    @Schema(description = "Volume information for EDV/ESV frames in 2ch and 4ch views")
    private VolumesInfo volumes_info;

    @Schema(description = "Atrial parameters", example = "{\"LA_Volume\": 45.6, \"RA_Volume\": 40.2}")
    private Map<String, Object> atrial_params;

    // ---------- Nested DTOs ---------- //

    public static class Parameter {
        @Schema(description = "Strain values by region")
        private Strain strain;

        @Schema(description = "Peak time values by region")
        private PeakTime peakTime;

        public Strain getStrain() {
            return strain;
        }

        public void setStrain(Strain strain) {
            this.strain = strain;
        }

        public PeakTime getPeakTime() {
            return peakTime;
        }

        public void setPeakTime(PeakTime peakTime) {
            this.peakTime = peakTime;
        }
    }

    public static class Strain {
        @Schema(example = "-18.5") private Double BasalAnteriorStrain;
        @Schema(example = "-19.2") private Double MidAnteriorStrain;
        @Schema(example = "-20.1") private Double ApicalAnteriorStrain;
        @Schema(example = "-17.8") private Double BasalInferiorStrain;
        @Schema(example = "-18.9") private Double MidInferiorStrain;
        @Schema(example = "-19.5") private Double ApicalInferiorStrain;
        @Schema(example = "-18.2") private Double BasalInferoseptalStrain;
        @Schema(example = "-19.0") private Double MidInferoseptalStrain;
        @Schema(example = "-20.3") private Double ApicalSeptalStrain;
        @Schema(example = "-17.9") private Double BasalAnterolateralStrain;
        @Schema(example = "-18.7") private Double MidAnterolateralStrain;
        @Schema(example = "-19.6") private Double ApicalLateralStrain;
        @Schema(example = "-19.1") private Double GLS;
        @Schema(example = "-18.8") private Double ContourGLS;
		public Double getBasalAnteriorStrain() {
			return BasalAnteriorStrain;
		}
		public void setBasalAnteriorStrain(Double basalAnteriorStrain) {
			BasalAnteriorStrain = basalAnteriorStrain;
		}
		public Double getMidAnteriorStrain() {
			return MidAnteriorStrain;
		}
		public void setMidAnteriorStrain(Double midAnteriorStrain) {
			MidAnteriorStrain = midAnteriorStrain;
		}
		public Double getApicalAnteriorStrain() {
			return ApicalAnteriorStrain;
		}
		public void setApicalAnteriorStrain(Double apicalAnteriorStrain) {
			ApicalAnteriorStrain = apicalAnteriorStrain;
		}
		public Double getBasalInferiorStrain() {
			return BasalInferiorStrain;
		}
		public void setBasalInferiorStrain(Double basalInferiorStrain) {
			BasalInferiorStrain = basalInferiorStrain;
		}
		public Double getMidInferiorStrain() {
			return MidInferiorStrain;
		}
		public void setMidInferiorStrain(Double midInferiorStrain) {
			MidInferiorStrain = midInferiorStrain;
		}
		public Double getApicalInferiorStrain() {
			return ApicalInferiorStrain;
		}
		public void setApicalInferiorStrain(Double apicalInferiorStrain) {
			ApicalInferiorStrain = apicalInferiorStrain;
		}
		public Double getBasalInferoseptalStrain() {
			return BasalInferoseptalStrain;
		}
		public void setBasalInferoseptalStrain(Double basalInferoseptalStrain) {
			BasalInferoseptalStrain = basalInferoseptalStrain;
		}
		public Double getMidInferoseptalStrain() {
			return MidInferoseptalStrain;
		}
		public void setMidInferoseptalStrain(Double midInferoseptalStrain) {
			MidInferoseptalStrain = midInferoseptalStrain;
		}
		public Double getApicalSeptalStrain() {
			return ApicalSeptalStrain;
		}
		public void setApicalSeptalStrain(Double apicalSeptalStrain) {
			ApicalSeptalStrain = apicalSeptalStrain;
		}
		public Double getBasalAnterolateralStrain() {
			return BasalAnterolateralStrain;
		}
		public void setBasalAnterolateralStrain(Double basalAnterolateralStrain) {
			BasalAnterolateralStrain = basalAnterolateralStrain;
		}
		public Double getMidAnterolateralStrain() {
			return MidAnterolateralStrain;
		}
		public void setMidAnterolateralStrain(Double midAnterolateralStrain) {
			MidAnterolateralStrain = midAnterolateralStrain;
		}
		public Double getApicalLateralStrain() {
			return ApicalLateralStrain;
		}
		public void setApicalLateralStrain(Double apicalLateralStrain) {
			ApicalLateralStrain = apicalLateralStrain;
		}
		public Double getGLS() {
			return GLS;
		}
		public void setGLS(Double gLS) {
			GLS = gLS;
		}
		public Double getContourGLS() {
			return ContourGLS;
		}
		public void setContourGLS(Double contourGLS) {
			ContourGLS = contourGLS;
		}
        
        

        // Getters and Setters...
    }

    public static class PeakTime {
        @Schema(example = "430") private Double BasalAnteriorStrain;
        @Schema(example = "420") private Double MidAnteriorStrain;
        @Schema(example = "410") private Double ApicalAnteriorStrain;
        @Schema(example = "440") private Double BasalInferiorStrain;
        @Schema(example = "425") private Double MidInferiorStrain;
        @Schema(example = "415") private Double ApicalInferiorStrain;
        @Schema(example = "435") private Double BasalInferoseptalStrain;
        @Schema(example = "428") private Double MidInferoseptalStrain;
        @Schema(example = "412") private Double ApicalSeptalStrain;
        @Schema(example = "438") private Double BasalAnterolateralStrain;
        @Schema(example = "424") private Double MidAnterolateralStrain;
        @Schema(example = "419") private Double ApicalLateralStrain;
		public Double getBasalAnteriorStrain() {
			return BasalAnteriorStrain;
		}
		public void setBasalAnteriorStrain(Double basalAnteriorStrain) {
			BasalAnteriorStrain = basalAnteriorStrain;
		}
		public Double getMidAnteriorStrain() {
			return MidAnteriorStrain;
		}
		public void setMidAnteriorStrain(Double midAnteriorStrain) {
			MidAnteriorStrain = midAnteriorStrain;
		}
		public Double getApicalAnteriorStrain() {
			return ApicalAnteriorStrain;
		}
		public void setApicalAnteriorStrain(Double apicalAnteriorStrain) {
			ApicalAnteriorStrain = apicalAnteriorStrain;
		}
		public Double getBasalInferiorStrain() {
			return BasalInferiorStrain;
		}
		public void setBasalInferiorStrain(Double basalInferiorStrain) {
			BasalInferiorStrain = basalInferiorStrain;
		}
		public Double getMidInferiorStrain() {
			return MidInferiorStrain;
		}
		public void setMidInferiorStrain(Double midInferiorStrain) {
			MidInferiorStrain = midInferiorStrain;
		}
		public Double getApicalInferiorStrain() {
			return ApicalInferiorStrain;
		}
		public void setApicalInferiorStrain(Double apicalInferiorStrain) {
			ApicalInferiorStrain = apicalInferiorStrain;
		}
		public Double getBasalInferoseptalStrain() {
			return BasalInferoseptalStrain;
		}
		public void setBasalInferoseptalStrain(Double basalInferoseptalStrain) {
			BasalInferoseptalStrain = basalInferoseptalStrain;
		}
		public Double getMidInferoseptalStrain() {
			return MidInferoseptalStrain;
		}
		public void setMidInferoseptalStrain(Double midInferoseptalStrain) {
			MidInferoseptalStrain = midInferoseptalStrain;
		}
		public Double getApicalSeptalStrain() {
			return ApicalSeptalStrain;
		}
		public void setApicalSeptalStrain(Double apicalSeptalStrain) {
			ApicalSeptalStrain = apicalSeptalStrain;
		}
		public Double getBasalAnterolateralStrain() {
			return BasalAnterolateralStrain;
		}
		public void setBasalAnterolateralStrain(Double basalAnterolateralStrain) {
			BasalAnterolateralStrain = basalAnterolateralStrain;
		}
		public Double getMidAnterolateralStrain() {
			return MidAnterolateralStrain;
		}
		public void setMidAnterolateralStrain(Double midAnterolateralStrain) {
			MidAnterolateralStrain = midAnterolateralStrain;
		}
		public Double getApicalLateralStrain() {
			return ApicalLateralStrain;
		}
		public void setApicalLateralStrain(Double apicalLateralStrain) {
			ApicalLateralStrain = apicalLateralStrain;
		}
        
        

        // Getters and Setters...
    }

    public static class VolumesInfo {
        @Schema(example = "120") private String EDV_4ch;
        @Schema(example = "60") private String ESV_4ch;
        @Schema(example = "115") private String EDV_2ch;
        @Schema(example = "58") private String ESV_2ch;
		public String getEDV_4ch() {
			return EDV_4ch;
		}
		public void setEDV_4ch(String eDV_4ch) {
			EDV_4ch = eDV_4ch;
		}
		public String getESV_4ch() {
			return ESV_4ch;
		}
		public void setESV_4ch(String eSV_4ch) {
			ESV_4ch = eSV_4ch;
		}
		public String getEDV_2ch() {
			return EDV_2ch;
		}
		public void setEDV_2ch(String eDV_2ch) {
			EDV_2ch = eDV_2ch;
		}
		public String getESV_2ch() {
			return ESV_2ch;
		}
		public void setESV_2ch(String eSV_2ch) {
			ESV_2ch = eSV_2ch;
		}

        // Getters and Setters...
        
        
    }

	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public Map<String, List<Double>> getGraph() {
		return graph;
	}

	public void setGraph(Map<String, List<Double>> graph) {
		this.graph = graph;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTwoChSeriesInstanceUID() {
		return twoChSeriesInstanceUID;
	}

	public void setTwoChSeriesInstanceUID(String twoChSeriesInstanceUID) {
		this.twoChSeriesInstanceUID = twoChSeriesInstanceUID;
	}

	public String getFourChSeriesInstanceUID() {
		return fourChSeriesInstanceUID;
	}

	public void setFourChSeriesInstanceUID(String fourChSeriesInstanceUID) {
		this.fourChSeriesInstanceUID = fourChSeriesInstanceUID;
	}

	public String getStudyInstanceUid() {
		return studyInstanceUid;
	}

	public void setStudyInstanceUid(String studyInstanceUid) {
		this.studyInstanceUid = studyInstanceUid;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public Double getFourChSlicelocation() {
		return fourChSlicelocation;
	}

	public void setFourChSlicelocation(Double fourChSlicelocation) {
		this.fourChSlicelocation = fourChSlicelocation;
	}

	public Double getTwoChSlicelocation() {
		return twoChSlicelocation;
	}

	public void setTwoChSlicelocation(Double twoChSlicelocation) {
		this.twoChSlicelocation = twoChSlicelocation;
	}

	public VolumesInfo getVolumes_info() {
		return volumes_info;
	}

	public void setVolumes_info(VolumesInfo volumes_info) {
		this.volumes_info = volumes_info;
	}

	public Map<String, Object> getAtrial_params() {
		return atrial_params;
	}

	public void setAtrial_params(Map<String, Object> atrial_params) {
		this.atrial_params = atrial_params;
	}

    
}
