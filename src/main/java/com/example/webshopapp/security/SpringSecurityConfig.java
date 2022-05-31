package com.example.webshopapp.security;

import com.example.webshopapp.security.jwt.JwtEntryPoint;
import com.example.webshopapp.security.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity
@DependsOn("passwordEncoder")
@RequiredArgsConstructor
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtFilter jwtFilter;
    private final JwtEntryPoint accessDenyHandler;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    DataSource dataSource;

    @Value("${userQuery}")
    private String usersQuery;

    @Value("${roleQuery}")
    private String rolesQuery;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth
                .jdbcAuthentication()
                .usersByUsernameQuery(usersQuery)
                .authoritiesByUsernameQuery(rolesQuery)
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.cors().and().csrf().disable()
                .authorizeRequests()

                .antMatchers("/profile/**").authenticated()
                .antMatchers("/cart/**").access("hasAnyRole('CUSTOMER')")
                .antMatchers("/order/finish/**").access("hasAnyRole('EMPLOYEE', 'MANAGER')")
                .antMatchers("/order/**").authenticated()
                .antMatchers("/profiles/**").authenticated()
                .antMatchers("/seller/product/new").access("hasAnyRole('MANAGER')")
                .antMatchers("/seller/**/delete").access("hasAnyRole( 'MANAGER')")
                .antMatchers("/seller/**").access("hasAnyRole('EMPLOYEE', 'MANAGER')")
                .anyRequest().permitAll()

                .and()
                .exceptionHandling().authenticationEntryPoint(accessDenyHandler)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
