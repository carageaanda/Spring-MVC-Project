package com.example.project.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String ADMIN = "ADMIN";
    public static final String MANAGER = "MANAGER";
    public static final String ROLE_ADMIN = "ROLE_" + ADMIN;
    public static final String ROLE_MANAGER = "ROLE_" + MANAGER;

    @Autowired
    private DataSource dataSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
//                .anyRequest().authenticated()
                .antMatchers("/h2-console/login.do*").permitAll()

                .antMatchers("/recordlabels/new", "/recordlabels/{^[0-9]+}/edit").hasRole(ADMIN)
                .antMatchers("/recordlabels/{^[0-9]+}/delete").hasRole(ADMIN)
                .antMatchers("/recordlabels/{^[0-9]+}").permitAll()
                .antMatchers("/recordlabels").permitAll()

                .antMatchers("/artists/new", "/artists/{^[0-9]+}/edit").hasRole(ADMIN)
                .antMatchers("/artists/{^[0-9]+}/delete").hasRole(ADMIN)
                .antMatchers("/artists/{^[0-9]+}").hasAnyRole(ADMIN, MANAGER)
                .antMatchers("/artists").permitAll()

                .antMatchers("/songs/new", "/songs/{^[0-9]+}/edit").hasRole(ADMIN)
                .antMatchers("/songs/{^[0-9]+}/delete").hasRole(ADMIN)
                .antMatchers("/songs/{^[0-9]+}").permitAll()
                .antMatchers("/songs").permitAll()

                .antMatchers("/managers/{^[0-9]+}/edit").hasAnyRole(MANAGER, ADMIN)
                .antMatchers("/managers/my-profile").hasRole(MANAGER)
                .antMatchers("/managers/{^[0-9]+}/delete").hasRole(ADMIN)
                .antMatchers("/managers/{^[0-9]+}").permitAll()
                .antMatchers("/managers").permitAll()

                .antMatchers("/deals/new", "/deals/{^[0-9]+}/edit").hasRole(ADMIN)
                .antMatchers("/deals/{^[0-9]+}/delete").hasRole(ADMIN)
                .antMatchers("/deals/{^[0-9]+}").permitAll()
                .antMatchers("/deals").permitAll()

                .antMatchers("/consults/{^[0-9]+}/edit").hasAnyRole(MANAGER, ADMIN)
                .antMatchers("/consults/{^[0-9]+}/delete").hasRole(ADMIN)
                .antMatchers("/consults/new").hasAnyRole(MANAGER, ADMIN)
                .antMatchers("/consults/{^[0-9]+}").hasAnyRole(MANAGER, ADMIN)
                .antMatchers("/consults/my-consults").hasRole(MANAGER)
                .antMatchers("/consults").hasAnyRole(MANAGER, ADMIN)

                .antMatchers("/**/bootstrap/**").permitAll()

                .and()

                .headers().frameOptions().disable()
                .and()
                .csrf().disable()

                .formLogin().loginPage("/login")
                .loginProcessingUrl("/authUser")
                .permitAll()
                .and()
                .logout().permitAll()
                .logoutSuccessUrl("/index")
                .invalidateHttpSession(true)
                .and()
                .exceptionHandling().accessDeniedPage("/access-denied");

    }
}

