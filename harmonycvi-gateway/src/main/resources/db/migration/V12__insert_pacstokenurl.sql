CREATE TABLE org_api_config (
    id BIGSERIAL PRIMARY KEY,
    org_id BIGINT NOT NULL,

    api_url TEXT,
    method VARCHAR(50),

    request_params_type VARCHAR(100),  -- e.g., FORM_URL_ENCODED, JSON, QUERY_STRING
    request_params text,                      -- dynamic params sent to external API

    headers text,                             -- dynamic headers

    response_token_field VARCHAR(100),
    
    user_api_url TEXT,
    user_method VARCHAR(50),

    user_request_params_type VARCHAR(100),  -- e.g., FORM_URL_ENCODED, JSON, QUERY_STRING
    user_request_params text,                      -- dynamic params sent to external API

    user_headers text,                             -- dynamic headers
    user_response_token_field VARCHAR(100),

    CONSTRAINT fk_org_api_config_organization
        FOREIGN KEY (org_id) REFERENCES organization(id)
);
