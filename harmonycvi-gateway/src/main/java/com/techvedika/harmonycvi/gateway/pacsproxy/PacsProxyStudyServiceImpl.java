package com.techvedika.harmonycvi.gateway.pacsproxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.dto.KeycloakToken;
import com.techvedika.harmonycvi.gateway.dto.PatientUpdateDTO;
import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;
import com.techvedika.harmonycvi.gateway.repository.UserOrganizationRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.serviceimpl.AsyncDeleteServiceImpl;
import com.techvedika.harmonycvi.gateway.serviceimpl.PacsTokenService;
import com.techvedika.harmonycvi.gateway.util.JsonUtils;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.io.OutputStream;

@Service
@Transactional
public class PacsProxyStudyServiceImpl implements PacsProxyStudyService {
	

	private static final Logger LOG = LoggerFactory.getLogger(PacsProxyStudyServiceImpl.class);

	// private final String dcm4cheeBaseUrl =
	// "https://cvidev.techvedika.com/dcm4chee-arc/aets/DCM4CHEE/rs/studies";

	//private final String dcm4cheeBaseUrl = "http://192.168.2.170:8080/dcm4chee-arc";
	//private final String dcm4cheeBaseUrl = "http://192.168.1.103:8080/dcm4chee-arc";
	// + "/aets/DCM4CHEE/rs/studies"; // New one

	@Value("${dcm4cheeBaseUrl}")
	private String dcm4cheeBaseUrl; // e.g. http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final WebClient webClient;

	@Autowired
	RestTemplateBuilder restTemplateBuilder;
	
	@Autowired
	private UserUtils userUtils;
	
	@Autowired
	private StudyExtensionRepository studyExtensionRepo;
	
	@Autowired
	private JwtDecoder jwtDecoder;
	
	@Autowired
	private UserOrganizationRepository userOrganizationRepo;
	
	@Autowired
	public PacsProxyStudyServiceImpl(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl(dcm4cheeBaseUrl).build();
	}
	
	@Autowired
	AsyncDeleteServiceImpl asyncDeleteServiceImpl; 
	
	@Autowired
	PacsTokenService pacsTokenService;

	
	@Bean
	public WebClient dcm4cheeWebClient(WebClient.Builder builder) {
	    return builder.build();
	}

	@Autowired
	private WebClient dcm4cheeWebClient;
	
	public Mono<ResponseEntity<String>> uploadDicom(
	        MultipartFile file,
	        PatientUpdateDTO patientUpdateDTO
	) {

	    LOG.info("uploadDicom called");
	    try {
	        String url = dcm4cheeBaseUrl + "/aets/DCM4CHEE/rs/studies";
	        String token = pacsTokenService.getToken(null);
	        String boundary = "stow-" + UUID.randomUUID();

	        Flux<DataBuffer> requestBody =
	                buildMultipartBody(file, boundary, patientUpdateDTO);

	        return dcm4cheeWebClient
	                .post()
	                .uri(url)
	                .headers(headers -> {
	                    headers.setBearerAuth(token);
	                    headers.setAccept(
	                            List.of(MediaType.valueOf("application/dicom+json"))
	                    );
	                    headers.set(
	                            HttpHeaders.CONTENT_TYPE,
	                            "multipart/related; type=\"application/dicom\"; boundary=" + boundary
	                    );
	                })
	                .body(BodyInserters.fromDataBuffers(requestBody))
	                .exchangeToMono(this::mapResponse)
	                .timeout(Duration.ofMinutes(5))
	                .doOnSubscribe(s -> LOG.info("Starting DICOM STOW upload"))
	                .doOnSuccess(r -> LOG.info("DICOM STOW completed: {}", r.getStatusCode()))
	                .doOnError(e -> LOG.error("DICOM STOW failed", e));

	    } catch (IOException ex) {
	        LOG.error("Error reading DICOM file", ex);
	        return Mono.just(
	                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                        .body("Unexpected error uploading DICOM: " + ex.getMessage())
	        );
	    } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			 LOG.error("Error reading DICOM file", e1);
		        return Mono.just(
		                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                        .body("Unexpected error uploading DICOM: " + e1.getMessage())
		        );
		}
	}
	
	private Mono<ResponseEntity<String>> mapResponse(ClientResponse response) {
	    return response
	            .bodyToMono(String.class)
	            .defaultIfEmpty("")
	            .map(body ->
	                    ResponseEntity
	                            .status(response.statusCode())
	                            .body(body)
	            );
	}

	@Override
	public ResponseEntity<String> uploadBulkDicom(List<MultipartFile> files) {
		String uploaddcm4cheeBaseUrl = dcm4cheeBaseUrl + "/aets/DCM4CHEE/rs/studies";
		try {
			// Prepare boundary manually
			String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");
			String CRLF = "\r\n";

			// Build multipart/related payload manually
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			for (MultipartFile file : files) {
				String fileName = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8);
				String partHeader = "--" + boundary + CRLF
						+ "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + CRLF
						+ "Content-Type: application/dicom" + CRLF + CRLF;

				// Write file part header and content
				outputStream.write(partHeader.getBytes(StandardCharsets.UTF_8));
				outputStream.write(file.getBytes());
				outputStream.write(CRLF.getBytes(StandardCharsets.UTF_8)); // separate files by CRLF
			}

			// End the multipart/related message with the final boundary
			outputStream.write(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));

			byte[] requestBody = outputStream.toByteArray();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(
					MediaType.parseMediaType("multipart/related; type=\"application/dicom\"; boundary=" + boundary));
			headers.setAccept(List.of(MediaType.valueOf("application/dicom+json")));
			headers.set("Authorization",
					"eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4NDUxNTciLCJpYXQiOjE3NTMxOTM2ODgsInN1YiI6IkRJQ09NLVdFQiIsImlzcyI6IkRJQ09NIiwiZXhwIjoxNzUzMjIyNDg4fQ.k0KwwWP8LHc9Uqlo4H_aRbL3hp2iLuMyEDXno737_wU");

			HttpEntity<byte[]> requestEntity = new HttpEntity<>(requestBody, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			ResponseEntity<String> response = restTemplate.postForEntity(uploaddcm4cheeBaseUrl, requestEntity, String.class);

			return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
		} catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("Error reading DICOM files: " + e.getMessage());
	    } catch (WebClientResponseException e) {
	        return ResponseEntity.status(e.getStatusCode())
	                .body("Error from DICOM server: " + e.getResponseBodyAsString());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Unexpected error uploading DICOM files: " + e.getMessage());
	    }
	}

	@Override
	public ResponseEntity<byte[]> downloadDicom(String studyUID, String seriesUID, String objectUID) {
	    String url = "http://192.168.2.170:8080" + UriComponentsBuilder.fromPath("/dcm4chee-arc/aets/DCM4CHEE/wado")
	            .queryParam("requestType", "WADO")
	            .queryParam("studyUID", studyUID)
	            .queryParam("seriesUID", seriesUID)
	            .queryParam("objectUID", objectUID)
	            .queryParam("contentType", "application/dicom")
	            .queryParam("transferSyntax", "*")
	            .build()
	            .toUriString();

	    try {
	        byte[] dicomBytes = webClient.get()
	                .uri(url)
	                .accept(MediaType.valueOf("application/dicom"))
	                .retrieve()
	                .onStatus(status -> status.isError(),
	                    clientResponse -> clientResponse.bodyToMono(String.class)
	                        .map(errorBody -> new RuntimeException("WADO-RS failed: " + errorBody))
	                )
	                .bodyToMono(byte[].class)
	                .block();

	        if (dicomBytes == null || dicomBytes.length == 0) {
	            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	        }

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.valueOf("application/dicom"));
	        headers.setContentLength(dicomBytes.length);

	        return new ResponseEntity<>(dicomBytes, headers, HttpStatus.OK);

	    } catch (RuntimeException e) {
	        // Return 502 Bad Gateway with error message from WADO-RS failure or other exceptions
	        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
	                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
	                .body(e.getMessage().getBytes(StandardCharsets.UTF_8));
	    } catch (Exception e) {
	        // Return 500 Internal Server Error for unexpected exceptions
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
	                .body(("Failed to download DICOM: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
	    }
	}


	@Override
	public JSONObject fetchStudyMetadata(String studyUID) {
		try {
			String url = dcm4cheeBaseUrl + "/aets/DCM4CHEE/rs/studies/" + URLEncoder.encode(studyUID, StandardCharsets.UTF_8)
					+ "/metadata";
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			return JsonUtils.parseJsonString(response.getBody());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch study metadata", ex);
		}
	}

	// Example method to get the total number of studies in DCM4CHEE's public schema
	public int getTotalStudies() {
		// Explicitly specifying the schema (public.study)
		String sql = "SELECT COUNT(*) FROM public.study";
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}

	@Override
	public JSONObject fetchStudies() {
		try {
			String url = dcm4cheeBaseUrl + "/aets/DCM4CHEE/rs/studies/";
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			return JsonUtils.parseJsonString(response.getBody());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch studies", ex);
		}
	}

	@Override
	public JSONObject fetchStudyByUID(String studyUID) {
		try {
			String url = dcm4cheeBaseUrl + "/aets/DCM4CHEE/rs/studies/" + URLEncoder.encode(studyUID, StandardCharsets.UTF_8);
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			return JsonUtils.parseJsonString(response.getBody());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch study from PACS", ex);
		}
	}
	
	@Override
//	public void rejectStudy(String studyInstanceUID) {
//		
//	    String url = dicomUrl + "/studies/" + studyInstanceUID + "/reject"+"/113001";
//	    System.out.println("url ---------------"+url);
//	    HttpHeaders headers = new HttpHeaders();
//	    headers.setContentType(MediaType.APPLICATION_JSON);
//
//	    // Rejection code from DICOM standard
//	    Map<String, String> rejectionBody = Map.of(
//	        "CodeValue", "113001",
//	        "CodingSchemeDesignator", "DCM",
//	        "CodeMeaning", "Rejected for Quality Reasons"
//	    );
//	   // System.out.println("rejectionBody--------------"+rejectionBody);
//	    
//	    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(rejectionBody, headers);
//
//	    try {
//	        //restTemplate.postForEntity(url, requestEntity, Void.class);
//	        //restTemplate.postForEntity(url,null, Void.class);
//	    	
//	    	// Body can be empty JSON "{}"
//	    	HttpEntity<String> request = new HttpEntity<>("{}", headers);
//
//	    	//String url = "http://192.168.2.170:8080/dcm4chee-arc/studies/1.2.156.112605.66988331192476.250613023737.2.12060.16051/reject/113001";
//
//	    	ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
//
//	    	System.out.println("Status: " + response.getStatusCode());
//
//	    } catch (HttpClientErrorException ex) {
//	    	HttpStatusCode status = ex.getStatusCode();
//	    	System.out.println("status-------------"+status);
//	        if (status == HttpStatus.CONFLICT) {
//	            throw new ResponseStatusException(HttpStatus.CONFLICT, "Study is already rejected or cannot be rejected again.");
//	        } else if (status == HttpStatus.NOT_FOUND) {
//	            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Study not found in PACS.");
//	        } else {
//	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to reject study: " + ex.getMessage());
//	        }
//	    }
//	}
	
	public void rejectStudy(String studyInstanceUID) {
	    String url = dcm4cheeBaseUrl + "/studies/" + studyInstanceUID + "/reject/113001^DCM";

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    // No body needed, just headers
	    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

	    try {
	        ResponseEntity<Void> response = restTemplate.postForEntity(url, requestEntity, Void.class);
	        System.out.println("Status: " + response.getStatusCode());

	    } catch (HttpClientErrorException ex) {
	        HttpStatusCode status = ex.getStatusCode();
	        System.out.println("status-------------" + status);
	        if (status == HttpStatus.CONFLICT) {
	            throw new ResponseStatusException(HttpStatus.CONFLICT,
	                    "Study is already rejected or cannot be rejected again.");
	        } else if (status == HttpStatus.NOT_FOUND) {
	            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
	                    "Study not found in PACS.");
	        } else {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
	                    "Failed to reject study: " + ex.getMessage());
	        }
	    }
	}

	@Override
	@Transactional
	public JSONObject deleteStudy(String studyInstanceUID) {
		JSONObject resp = new JSONObject();
		studyExtensionRepo.updateDeleteStatus(studyInstanceUID);
		resp = userUtils.updateResponse(resp, UserConstants.STATUS_SUCCESS, "Study deleted successfully.",
				"Study deleted successfully.", null);
		String userEmailId = SecurityUtil.currentUserEmailId();
		asyncDeleteServiceImpl.performAsyncDelete(studyInstanceUID,userEmailId);

		return resp;
	}
		
	@Override
	@Transactional()
	public JSONObject deleteStudyExtension(String studyInstanceUID) {
		studyExtensionRepo.deleteByStudyId(studyInstanceUID);
		JSONObject resp = new JSONObject();
		resp = userUtils.updateResponse(resp, UserConstants.STATUS_SUCCESS, UserConstants.DELETE_SUCCESS_MESSAGE,
				UserConstants.DELETE_SUCCESS_MESSAGE, null);
		return resp;

	}

	@Override
	public JSONObject getToken(JSONObject json) throws Exception {
		JSONObject response = new JSONObject();
		String userId = json.get("userId").toString();
		String orgId = json.get("orgId").toString();
		String userGroup = userOrganizationRepo.findByUserIdANDOrgId(Long.parseLong(userId),Long.parseLong(orgId));
		String token = pacsTokenService.getToken(orgId);
		Jwt jwt = jwtDecoder.decode(token); // parses and verifies the JWT
	   List<String> groups = jwt.getClaimAsStringList("groups");
	   if (groups != null && groups.stream().anyMatch(g -> g.equalsIgnoreCase(userGroup))) {
	        response.put("token", token);
	        response.put("statusCode", "200");
	        response.put("statusMessage", "success");
	    } else {
	        response.put("statusCode", "401");
	        response.put("statusMessage", "UnAuthorized");
	    }

	   return response;
		
	}

	private Flux<DataBuffer> buildMultipartBody(MultipartFile file, String boundary, PatientUpdateDTO dto)
			throws IOException {

		DataBufferFactory factory = new DefaultDataBufferFactory();

		DataBuffer header = factory.wrap(("--" + boundary + "\r\n" + "Content-Type: application/dicom\r\n\r\n")
				.getBytes(StandardCharsets.UTF_8));

		DataBuffer footer = factory.wrap(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

		Flux<DataBuffer> dicomBody = (dto != null) ? updateDicomTags(file, dto, factory)
				: Flux.just(factory.wrap(file.getBytes()));

		return Flux.concat(Flux.just(header), dicomBody, Flux.just(footer));
	}

	private Flux<DataBuffer> updateDicomTags(MultipartFile file, PatientUpdateDTO dto,
			DataBufferFactory bufferFactory) {

		return Flux.create(sink -> {
			try (InputStream is = file.getInputStream();
					DicomInputStream dis = new DicomInputStream(is);
					ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

				Attributes dataset = dis.readDataset(-1, -1);
				if(dto != null) {
				if (dto.getPatientName() != null) {
					dataset.setString(Tag.PatientName, VR.PN, dto.getPatientName());
				}
				if (dto.getPatientId() != null) {
					dataset.setString(Tag.PatientID, VR.LO, dto.getPatientId());
				}
				if (dto.getInstitutionName() != null) {
					dataset.setString(Tag.InstitutionName, VR.LO, dto.getInstitutionName());
				}

				if (dto.getPatientSize() != null) {
					dataset.setDouble(Tag.PatientSize, VR.DS, dto.getPatientSize());
				}

				if (dto.getPatientWeight() != null) {
					dataset.setDouble(Tag.PatientWeight, VR.DS, dto.getPatientWeight());
				}

				if (dto.getStationName() != null) {
					dataset.setString(Tag.StationName, VR.SH, dto.getStationName());
				}
				
				if (dto.getForcePixelRepresentation() != null) {

					dataset.setInt(Tag.PixelRepresentation, VR.US, dto.getForcePixelRepresentation());
				}
				}
				
				String datasetTsuid = dis.getTransferSyntax();
				Attributes fmi = new Attributes();
				fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[] { 0, 1 });
				fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, dataset.getString(Tag.SOPClassUID));
				fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, dataset.getString(Tag.SOPInstanceUID));
				fmi.setString(Tag.TransferSyntaxUID, VR.UI, datasetTsuid);
				fmi.setString(Tag.ImplementationClassUID, VR.UI, UIDUtils.createUID());

				try (DicomOutputStream dos = new DicomOutputStream(baos, UID.ExplicitVRLittleEndian)) {

					dos.writeDataset(fmi, dataset);
					dos.flush();
				}

				sink.next(bufferFactory.wrap(baos.toByteArray()));
				sink.complete();

			} catch (Exception e) {
				sink.error(e);
			}
		});
	}
}
