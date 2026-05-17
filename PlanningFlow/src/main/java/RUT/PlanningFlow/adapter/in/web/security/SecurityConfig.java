package RUT.PlanningFlow.adapter.in.web.security;

import RUT.PlanningFlow.domain.enums.UserRoles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] PLANNER_ROLES = {
            UserRoles.ORGANIZER.name(),
            UserRoles.COORDINATOR.name(),
            UserRoles.ADMIN.name(),
    };

    
    private static final String[] INTERNAL_RESOURCE_CREATE_ROLES = {
            UserRoles.ORGANIZER.name(),
            UserRoles.ADMIN.name(),
    };

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http, final JwtFilter jwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/register"
                        ).permitAll()
                        .requestMatchers("/api/v1/ws/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()

                        .requestMatchers("/api/v1/notifications", "/api/v1/notifications/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/tasks/*/status/**").authenticated()
                        
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/**").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/**").hasAnyRole(PLANNER_ROLES)
                        
                        .requestMatchers(HttpMethod.POST, "/api/v1/tasks").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tasks/**").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/tasks/*/assignments").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tasks/*/assignments/**").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/tasks/*/matching").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/tasks/*/resources/allocate").hasAnyRole(PLANNER_ROLES)
                        
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*/viewer-context").authenticated()
                        
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bookings/**").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings/*/status/**").hasAnyRole(PLANNER_ROLES)

                        .requestMatchers(HttpMethod.POST, "/api/v1/incidents/*/accept").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/incidents/*/resolve").hasAnyRole(PLANNER_ROLES)
                        
                        .requestMatchers(HttpMethod.POST, "/api/v1/skills").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/skills/*").hasAnyRole(PLANNER_ROLES)

                        .requestMatchers(HttpMethod.POST, "/api/v1/resources/internal")
                        .hasAnyRole(INTERNAL_RESOURCE_CREATE_ROLES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/resources/internal/**")
                        .hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/resources/internal/**").hasAnyRole(PLANNER_ROLES)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/resources/internal/**").hasAnyRole(PLANNER_ROLES)

                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}