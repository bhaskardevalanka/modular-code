package com.techvedika.harmonycvi.gateway.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private JwtAuthenticationEntryPoint jwtEntryPoint;

	@Value("${app.cors.allowed-origins}")
	private String allowedOrigins;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// .cors() // enable CORS
				// .apply(new CorsConfigurer<>())
				// .and()
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				// .sessionManagement(sm ->
				// sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/users/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/users/logout").permitAll()
						.requestMatchers(HttpMethod.POST, "/users/google/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/users/create").permitAll()
						.requestMatchers(HttpMethod.POST, "/users/forgotPassword").permitAll()
						.requestMatchers(HttpMethod.GET, "/organization/getOrgList").permitAll()
						.requestMatchers(HttpMethod.GET, "/organization/getOrgListByEmail").permitAll()
						.requestMatchers(HttpMethod.GET, "/organization/is-external/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/organization/getPacsUrl").permitAll()
						.requestMatchers(HttpMethod.GET, "/users/acceptInvitation/*/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/updateStatus").permitAll()
						.requestMatchers(HttpMethod.POST, "/seriesMeasurement/deleteContours").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/saveStudyVolumeInfo").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/saveStudyParam").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/AISaveOrgTags").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/getAIOrgTags").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/saveClassification").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/saveAIProcessStatus").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/getAIProcessStatus").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/getEndVolumeInfo").permitAll()
						.requestMatchers(HttpMethod.POST, "/studyParameter/saveRadialStrain").permitAll()
						.requestMatchers(HttpMethod.POST, "/seriesMeasurement/saveFreeHandData").permitAll()
						.requestMatchers(HttpMethod.POST, "/seriesMeasurement/saveQFlowFreeHandData").permitAll()
						.requestMatchers(HttpMethod.POST, "/seriesParameter/saveDESeriesParam").permitAll()
						.requestMatchers(HttpMethod.POST, "/seriesParameter/saveGLSSeriesParam").permitAll()
						.requestMatchers(HttpMethod.POST, "/seriesParameter/saveSeriesParam").permitAll()
						.requestMatchers(HttpMethod.POST, "/pacs/study/studyupload").permitAll()
						.requestMatchers(HttpMethod.POST, "/seriesMeasurement/saveSegments").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/s3/pdf/view-url").permitAll()
						.requestMatchers(HttpMethod.POST, "/report/**").permitAll()
						.requestMatchers(HttpMethod.GET,
								"/pacs/study/{studyUID}/series/{seriesUID}/instance/{objectUID}/download")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/hello/**").permitAll() // For testing purpose
						.requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll().anyRequest()
						.authenticated())
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		// .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtEntryPoint));
		return http.build();

		// If you don't want authentication then use below code only
		// http
		// .csrf(csrf -> csrf.disable()) // Disable CSRF
		// .authorizeHttpRequests(auth -> auth
		// .anyRequest().permitAll() // Allow all requests
		// )
		// .httpBasic(httpBasic -> httpBasic.disable()) // Disable basic auth
		// .formLogin(formLogin -> formLogin.disable()); // Disable form login
		//
		// return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Sha256PasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(new Sha256PasswordEncoder()); // or any PasswordEncoder
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		/*
		 * -----------------------------------------
		 * 1️⃣ Special CORS for NULL origin (file://)
		 * -----------------------------------------
		 */
		CorsConfiguration nullOriginConfig = new CorsConfiguration();
		nullOriginConfig.setAllowedOriginPatterns(List.of("*"));
		nullOriginConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
		nullOriginConfig.setAllowedHeaders(List.of("*"));
		nullOriginConfig.setAllowCredentials(false); // 🔴 MUST be false

		source.registerCorsConfiguration(
				"/api/s3/pdf/view-url",
				nullOriginConfig);
		source.registerCorsConfiguration(
				"/customreport/getReport",
				nullOriginConfig);
		source.registerCorsConfiguration(
				"/report/**",
				nullOriginConfig);

		/*
		 * -------------------------
		 * 2️⃣ Default CORS (JWT APIs)
		 * -------------------------
		 */
		CorsConfiguration defaultConfig = new CorsConfiguration();
		defaultConfig.setAllowedOrigins(List.of(allowedOrigins.split(",")));
		defaultConfig.setAllowedMethods(
				List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		defaultConfig.setAllowedHeaders(List.of("*"));
		defaultConfig.setAllowCredentials(true);

		source.registerCorsConfiguration("/**", defaultConfig);

		return source;
	}

}