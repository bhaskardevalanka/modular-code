package com.techvedika.harmonycvi.gateway.constant;

public class UserConstants {
	
	public static final String RESPONSE_MESSAGE = "responseMessage";

	public static final String STATUS_CODE = "statusCode";
	public static final String STATUS = "status";
	public static final String STATUS_MESSAGE = "statusMessage";
	public static final String DATA = "data";

	public static final String SUCCESS_CODE = "200";
	public static final String SUCCESS_WITH_EMPTY_RESULT_CODE = "202";
	public static final String INVALID_STUDY_ID = "Invalid study id";
	public static final String BAD_REQUEST_CODE = "400";
	public static final String OPERATION_FAILED = "500";
	public static final String UNAUTHORIZED = "401";

	public static final String SAVED = "Data Saved Successfully !";
	public static final String FETCHED = "Data Fetched Successfully !";
	public static final String EMPTY_RESULT = "No data found !";
	public static final String UPDATED = " Details Updated Successfully !";

	public static final String INVALID_TOKEN = "Invalid security tokken";
	public static final String BAD_REQUEST = "Missing Mandatory Field Or Invalid Request";

	public static final String SERVER_ERROR = "Sorry,server unable to process your request";
	public static final String EMPTY_MESUREMENT = "Msesurement list empty";
	public static final String INVALID_CREDENTIAL = "Invalid email or password !";
	public static final String LOGIN_SUCCESS = "login successful!";
	public static final String SUPER_ADMIN = "SUPER_ADMIN";

	public static final String ORG_ADMIN = "ADMIN";

	public static final String USER_ADMIN = "TECHNICIAN";
	
	public static final String DOCTOR = "RESIDENT_DOCTOR";
	public static final String CONSULTANT_DOCTOR = "CONSULTANT_DOCTOR";
	public static final String TECHNICIAN = "TECHNICIAN";


	public static final String PRIVILEGE_LIST_ORGANIZATION = "PRIVILEGE_LIST_ORGANIZATION";
	public static final String PRIVILEGE_ADD_ORGANIZATION = "PRIVILEGE_ADD_ORGANIZATION";
	public static final String PRIVILEGE_UPDATE_ORGANIZATION = "PRIVILEGE_UPDATE_ORGANIZATION";
	public static final String PRIVILEGE_DELETE_ORGANIZATION = "PRIVILEGE_DELETE_ORGANIZATION";

	public static final String PRIVILEGE_LIST_USER = "PRIVILEGE_LIST_USER";
	public static final String PRIVILEGE_ADD_USER = "PRIVILEGE_ADD_USER";
	public static final String PRIVILEGE_UPDATE_USER = "PRIVILEGE_UPDATE_USER";
	public static final String PRIVILEGE_DELETE_USER = "PRIVILEGE_DELETE_USER";
	
	public static final String PRIVILEGE_LIST_DEVICE = "PRIVILEGE_LIST_DEVICE";
	public static final String PRIVILEGE_ADD_DEVICE = "PRIVILEGE_ADD_DEVICE";
	public static final String PRIVILEGE_UPDATE_DEVICE = "PRIVILEGE_UPDATE_DEVICE";
	public static final String PRIVILEGE_DELETE_DEVICE = "PRIVILEGE_DELETE_DEVICE";
	
	public static final String PRIVILEGE_LIST_CENTER = "PRIVILEGE_LIST_CENTER";
	public static final String PRIVILEGE_ADD_CENTER = "PRIVILEGE_ADD_CENTER";
	public static final String PRIVILEGE_UPDATE_CENTER = "PRIVILEGE_UPDATE_CENTER";
	public static final String PRIVILEGE_DELETE_CENTER = "PRIVILEGE_DELETE_CENTER";

	public static final String PRIVILEGE_LIST_ROLE = "PRIVILEGE_LIST_ROLE";

	public static final String PRIVILEGE_LIST_STUDY = "PRIVILEGE_LIST_STUDY";
	public static final String PRIVILEGE_ADD_STUDY = "PRIVILEGE_ADD_STUDY";
	public static final String PRIVILEGE_UPDATE_STUDY = "PRIVILEGE_UPDATE_STUDY";
	public static final String PRIVILEGE_DELETE_STUDY = "PRIVILEGE_DELETE_STUDY";
	public static final String PRIVILEGE_UPLOAD_STUDY = "PRIVILEGE_UPLOAD_STUDY";

	// Status Codes
	public static final String STATUS_SUCCESS = "200"; // if success
	public static final String STATUS_FAILURE = "501"; // if some exception occurs
	public static final String STATUS_FAILURE_505 = "505"; // if some exception occurs
	public static final String STATUS_MISSING = "405"; // if some mandatoryfields missing in your
									// request
	public static final String STATUS_INVALID = "404"; // for invalid requests
	public static final String STATUS_DUPLICATE_CODE = "409"; // for invalid requests
	public static final String STATUS_EXPIRED = "401";// for expired requests
	public static final String STATUS_RESTRICTED = "403";
	//String NO_CONTENT_CODE = "404";
	public static final String NO_CONTENT_CODE = "200";


	// Status MESSAGES
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";
	public static final String DUPLICATE = "DUPLICATE";
	public static final String MISSING_PARAMS = "MISSING_PARAMS";
	public static final String INVALID_REQ = "INVALID_REQ";
	public static final String FORBIDDEN = "FORBIDDEN";

	// Status Success Messages
	public static final String DEVICE_ADD_SUCCESS_MESSAGE = "Device Details Added Successfully !";
	public static final String DEVICE_NOT_EXIST_MESSAGE = "Device Not Fund With These Details";
	public static final String CENTER_NOT_EXIST_MESSAGE = "Center Not Found With These Details";
	public static final String USER_ADD_SUCCESS_MESSAGE = "User Details Added Successfully !";
	public static final String USER_UPDATE_SUCCESS_MESSAGE = "User Details Updated Successfully !";
	public static final String USER_DELETE_SUCCESS_MESSAGE = "User Details Deleted Successfully !";
	public static final String USER_NOT_EXIST_MESSAGE = "User Not Found With These Details";
	public static final String ROLE_ADD_SUCCESS_MESSAGE = "Role Details Added Successfully !";
	public static final String ROLE_UPDATE_SUCCESS_MESSAGE = "Role Details Updated Successfully !";
	public static final String ROLE_DELETE_SUCCESS_MESSAGE = "Role Details Deleted Successfully !";
	public static final String ROLE_NOT_EXIST_MESSAGE = "Role Not Found With These Details";
	public static final String ORGANIZATION_ADD_SUCCESS_MESSAGE = "Organization Details Added Successfully !";
	public static final String ORGANIZATION_UPDATE_SUCCESS_MESSAGE = "Organization Details Updated Successfully !";
	public static final String ORGANIZATION_DELETE_SUCCESS_MESSAGE = "Organization Details Deleted Successfully !";
	public static final String ORGANIZATION_NOT_EXIST_MESSAGE = "Organization Not Found With These Details";
	public static final String LOGIN_SUCCESS_MESSAGE = "User Logged In Successfully !";
	public static final String LOGOUT_SUCCESS_MESSAGE = "User Logged Out Successfully !";
	public static final String DELETE_SUCCESS_MESSAGE = "Deleted Successfully !";

	public static final String USER_GET_SUCCESS_MESSAGE = "User Details Got Successfully !";
	public static final String ROLE_GET_SUCCESS_MESSAGE = "Role Details Got Successfully !";
	public static final String ORGANIZATION_GET_SUCCESS_MESSAGE = "Organization Details Got Successfully !";
	public static final String CHANGE_PASSWORD_SUCCESS_MESSAGE = "Password Changed Successfully !";
	public static final String CSV_UPDATE_SUCCESS_MESSAGE = "Csv File Has Been Updated Successfully !";

	public static final String EMAIL_ALREADY_EXIST_MESSAGE = "Email Already Exists !";
	
	public static final String DEVICE_ALREADY_EXIST_MESSAGE = "Device Already Exists !";
	public static final String EMAIL_DET_ADD_SUCCESS_MESSAGE = "Email Details Added Successfully !";
	public static final String EMAIL_UPDATE_SUCCESS_MESSAGE = "Mail Details Updated Successfully !";
	public static final String EMAIL_DELETE_SUCCESS_MESSAGE = "Mail Details Deleted Successfully !";
	public static final String EMAIL_GET_SUCCESS_MESSAGE = "Email List Got Successfully !";
	public static final String FORGOT_PASSWORD_SUCCESS_MESSAGE = "New Password Has Sent Successfully !";
	public static final String EMAIL_RE_ACTIVATE_SUCCESS_MESSAGE = "Mail Details Reactivated Successfully !";

	// Error Messages
	public static final String USER_ADD_FAILURE_MESSAGE = "Error While Adding User Details";
	public static final String ROLE_ADD_FAILURE_MESSAGE = "Error While Adding Role Details";
	public static final String ORGANIZATION_ADD_FAILURE_MESSAGE = "Error While Adding Organization Details";
	public static final String LOGIN_FAILURE_MESSAGE = "Wrong Password Or User Name, Please Try Again";
	public static final String GENERATE_TOKEN_FAILURE_MESSAGE = "Error While Generating Security Token";
	public static final String User_MESSAGE = "User Not Found";
	public static final String USER_UPDATE_FAILURE_MESSAGE = "Error While Updating User Details";
	public static final String USER_DELETE_FAILURE_MESSAGE = "Error While Deleting User Details";
	public static final String ROLE_UPDATE_FAILURE_MESSAGE = "Error While Updating Role Details";
	public static final String ROLE_DELETE_FAILURE_MESSAGE = "Error While Deleting Role Details";
	public static final String ORGANIZATION_UPDATE_FAILURE_MESSAGE = "Error While Updating Organization Details";
	public static final String ORGANIZATION_DELETE_FAILURE_MESSAGE = "Error While Deleting Organization Details";
	public static final String USER_GET_FAILURE_MESSAGE = "Error While Getting User Details";
	public static final String ROLE_GET_FAILURE_MESSAGE = "Error While Getting Role Details";
	public static final String ORGANIZATION_GET_FAILURE_MESSAGE = "Error While Getting Organization Details";
	public static final String ORGANIZATION_RE_ACTIVATE_FAILURE_MESSAGE = "Error While Re-activating Organization";
	public static final String ORGANIZATION_RE_ACTIVATE_SUCCESS_MESSAGE = "Organization Details Reactivated Successfully !";
	public static final String NOT_AUTHORIZED_TO_ACCESS_MESSAGE = "You Are Not Authorised To Access This Feature";
	public static final String LOGOUT_FAIL_MESSAGE = "Error While Logging Out";
	public static final String CHANGEPASSWORD_FAILURE_MESSAGE = "Error While Updating Current Password";
	public static final String FORGOT_PASSWORD_FAILURE_MESSAGE = "Error While Creating a New Password";
	public static final String USER_DOES_NOT_EXIST = "User Doesnt Exist";
	public static final String INVALID_USERID = "Invalid Logged User Or User Deactivated";
	public static final String Admin_ALREADY_EXISTS_MESSAGE = "Admin Details Already Exists";
	public static final String INVALID_EMAIL_ID = "Please Log in with registered email";
	// Status Missing Messages
	public static final String MISSING_REQUEST_PARAM_MESSAGE = "Missing Mandatory Fields Information In Your Request";
	public static final String MISSING_MEETING_MEMBER_MESSAGE = "Missing Meeting Members Information In Your Request";
	public static final String RESTRICTED_ACCESS_MESSAGE = "You Are Not Able Access This Feature";

	// Status invalid Messages
	public static final String INVALID_REQUEST_MESSAGE = "Invalid Request Data";
	public static final String INVALID_RECURRING_MEETING_REQUEST_MESSAGE = "Invalid Recurring meeting";
	public static final String UNIQUE_EMAIL_REQUEST_MESSAGE = "Email Already Exists , Please Try With Different Email";
	public static final String USER_ACCOUNT_EXITS_EMAIL = "User With This Email Already Exists!!";
	public static final String USER_ACCOUNT_EXITS_LOGIN = "User With This Login Id Already Exists!";
	public static final String UNIQUE_USER_REQUEST_MESSAGE = "User Details Already Exists!";
	public static final String UNIQUE_LOGINID_REQUEST_MESSAGE = "Login Id Already Exists, Please Try With Different Login Id";
	public static final String UNIQUE_NAME_REQUEST_MESSAGE = "Name Already Exists, Please Try With Different Name";
	public static final String INVALID_EMAIL_REQUEST_MESSAGE = "Email Not Registered Or Account May Not Be Active";
	public static final String ACCOUNT_LOCKED_MESSAGE = "Account Locked";
	public static final String INVALID_CREDENTIALS_REQUEST_MESSAGE = "Invalid Credentials";
	public static final String NOT_AUTHORIZED_REQUEST_MESSAGE = "You Are Not Authorized To Access This ";
	// String INVALID_TOKEN_MESSAGE = "Please send a valid token!!";
	public static final String INVALID_TOKEN_MESSAGE = "Your Account Is Already Logged-In";
	public static final String INVALID_PASSWORD_MESSAGE = "Invalid Password";
	public static final String INVALID_TOKEN_ERROR_MESSAGE = "Invalid Token, Please Send a Valid Token ";

	public static final String NO_PRIVILAGES_TO_ACCESS = "No Privilages To Access This Service";
	public static final String ORG_CUSTOMFIELDS_ADD_SUCCESS_MESSAGE = "Organization Custom Fields Is Added Successfully !";
	public static final String SESSION_EXPIRY = "Session Expired, Re-authentication Needed";
	public static final String MEETING_NOT_EXIST_MESSAGE = "Meeting Not Found With These Details";
	public static final String NO_PERMISSIONS_TO_UPDATE_MEETING = "No Permissions ";

	public static final String INVALID_EMAIL = "Invalid Mail Id";

	public static final String INVALID_OTP = "Invalid OTP";

	public static final String NOT_A_EMPLOYEE = "Don't Have Permissions To Access";

	public static final String OTP_EXPIRED = "OTP Expired, Please Request For New OTP";

	public static final String OTP_FAILURE_MESSAGE = "Error While OTP Verification";


	public static final String USER_LOG_OUT_SUCCESS = "User Log Out Successfully !";

	public static final String ROLE_ID ="roleId";

	public static final String INVALID_ROLE = "Not a Valid Role";

	public static final String INVALID_ORG = "Not a Valid Org";

	public static final String EMAIL = "email";

	public static final String FIRST_NAME = "firstName";

	public static final String LAST_NAME = "lastName";

	public static final String ORG_ID = "orgId";

	public static final String PASSWORD = "password";

	public static final String WRONG_OLD_PASSWORD = "Wrong old password";

	public static final String OLD_PASSWORD = "oldPassword";

	public static final String NEW_PASSWORD = "newPassword";

	public static final String SELECT_ORG = "Select The Organization To Get The User List Or Organization Deactivated";

	public static final String NAME = "name";
	
	public static final String ID = "id";
	
	public static final String ACTIVE = "active";
	
	public static final String JWT_SECKET_KEY = "JWT_SECKET_KEY";
	public static final String JWT_EXPIRE_HRS_MILLIS = "JWT_EXPIRE_HRS_MILLIS";
	public static final String JWT_TOKEN = "Authorization";
	public static final String MISSING_JWT_TOKKEN = "jwtToken Is Missing";
	public static final String EXPIRED = "expired";
	public static final String TOKEN_EXPIRE = "token expired";
	public static final String ERROR = "error";
	public static final String SESSION_VALIDATION_ERROR = "Error While Session Validation";
	public static final String ONETIME_PASSWORD_SENT_MSG = "Onetime Password Sent To Your Register Email";
	public static final String REGISTRACTION = "Registration";
	public static final String FORGOT_PASSWORD = "Forgot Password";

	public static final String CREATED_BY = "createdBy";

	public static final String IS_ACTIVE = "isActive";

	public static final String LAST_UPDATED_BY = "lastUpdatedBy";

	public static final String INVALID_USER_TO_ORG = "User Doesn't Belong To This Organization";

	public static final String FILTER = "filter";

	public static final String ALL = "all";

	public static final String ID_LIST = "idList";

	public static final String DOCTOR_ID = "doctorId";

	public static final String INVALID_DOCTOR = "Doctor Doesn't Exist or Deactivated";

	public static final String INVALID_DOCTOR_TO_ORG = "Doctor Doesn't Exist To This Organization";

	public static final String STUDY_LIST = "studyList";

	public static final String USER_STUDIES = "User Study Saved Successfully !";

	public static final String USER_ID = "userId";

	public static final String API_URL = "API_URL";

	public static final String STUDY_ID = "studyId";

	public static final String NO = "no";

	public static final String YES = "yes";

	public static final String USER = "user";

	public static final String PROPERTIES_FILE_PATH = "/opt/wildfly/standalone/configuration/application.properties";

	public static final String SHA_256 = "SHA-256";

	public static final String USER_ALREADY_LOGOUT = "You Have Already Logout";

	public static final String TOTAL_DEVICE = "totalDevice";
	public static final String TOTAL_USER = "totalUser";
	public static final String TOTAL_ORG = "totalOrg";
	public static final String TOTAL_STUDY = "totalStudy";
	public static final String TOTAL_DOCTOR = "totalDoctor";

	public static final String ADDRESS_ONE = "address1";

	public static final String ADDRESS_TWO = "address2";

	public static final String CITY = "city";

	public static final String STATE = "state";

	public static final String COUNTRY = "country";

	public static final String AREA = "area";

	public static final String PIN_CODE = "pinCode";

	public static final String IS_CONSULTANT = "isConsultant";

	public static final String ORG_ALLREADY_EXIST = "Org Is Already Exists With Same Name";

	public static final String NO_ORG = "Org Is Not Exists";

	public static final String ERROR_CATEGORY = "errorCategory";

	public static final String ERROR_MESSAGE = "errorMessage";

	public static final String CODE = "code";

	public static final String TYPE = "type";

	public static final String MESSAGE = "message";

	public static final String ERRORS = "errors";

	public static final String API_REQUEST_ERRORS = "apiReqErrors";

	public static final String TRANS_NO = "transNum";

	public static final String NO_CENTER = "No Center Found";

	public static final String NO_DEVICE = "Device Not Found";

	public static final String NO_ROLE_ID = "Role Id Doesn't Exist";

	public static final String INVALID_OLD_OR_NEW_PASSWORD = "Old Or New Passwoed Missing";

	public static final String MISSING_EMAIL = "Email Missing";

	public static final String DUPLICATE_CENTER = "Center Is Already Exists With Same Name";

	public static final String DUPLICAT_DEVICE = "Device Is Already Exists With Same uid";

	public static final String ORG_LIST = "orgList";

	public static final String DEVICE_UID = "deviceUID";

	public static final String CENTER_ID = "centerId";

	public static final String DEVICE_TYPE = "deviceType";

	public static final String ADDRESS_ONE_ORG = "addressOne";
	public static final String ADDRESS_TWO_ORG = "addressTwo";

	public static final String PHONE_NO = "phoneNo";

	public static final String DEVICE_ID = "deviceId";

	public static final String ADDRESS = "address";

	public static final String LATITUDE = "latitude";

	public static final String LONGITUDE = "longitude";

	public static final String PHONE = "phone";

	public static final String OFFSET_VALUE = "offsetValue";

	public static final String TIME_ZONE = "timeZone";

	public static final String ORG_NAME = "organizationName";

	public static final String CENTER_NAME = "centerName";

	public static final String ROLE_NAME = "roleName";

	public static final String TENANT = "tenant";
	public static final String ADMIN = "admin";

	public static final String USER_STUDIES_DELETED = "User Study Deleted Successfully !";

	public static final String IS_STATUS_UPDATE = "isStatusUpdate";

	public static final String IS_ADMIN = "isAdmin";

	public static final String NO_PRIVILAGES_TO_LOGIN_THROUGH_THIS_PORTAL ="You Can't Login Through This Portal";

	public static final String DISABLED = " Disabled Successfully !";
	public static final String ENABLED = " Enabled Successfully !";

	public static final String CENTER = "Center";

	public static final String DEVICE = "Device";

	public static final String ORGANIZATION = "Organization";

	public static final String PASSWORD_UPDATE_SUCCESS_MESSAGE = "Password Updated Successfully !";

	public static final String OLD_AND_NEW_PASSWORD_SAME = "Please Check The Password And Re-Enter a New Password";

	public static final String IS_LOGINE_THROUGH_ONE_TIME_PASS = "isLoginThroughOneTimePass";

	public static final String UPLOADS = "/uploads/";

	public static final String UPLOADS_DIRECTORY = "/var/www/html/dicomAdmin/uploads/";

	public static final String END_DATE = "endDate";

	public static final String START_DATE = "startDate";

	public static final String CLASSIFICATIONRANGE = "classificationRange";
	
	public static final String VERSION_VIEWER = "viewer_version";
	
	public static final String VERSION_ADMIN= "admin_version";
	
	public static final String IS_MFA_ENABLED = "mfaEnabled";
	
	public static final String LOG_IN_COUNT = "login_count";
	
	public static final String SKIP_MFA = "skip_mfa";
	public static final String SAVE_BOOKMARK = "save_bookmark";
	public static final String WORD_REPORT = "word_report";
	public static final String PDF_REPORT = "pdf_report";

}
