package sk.kubena.fakenews.security;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;

    @Autowired
    public SecurityConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Enable jdbc authentication
    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery(
                        "SELECT email, password, enabled FROM user WHERE email=?")
                .authoritiesByUsernameQuery(
                        "SELECT email, name FROM user INNER JOIN role ON (role.id=user.role_id) WHERE email=?");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers(/*"public/**", */"/authenticate", "/request-ratings", "/api", "/resources/**");
        web.ignoring().antMatchers(/*"public/**", */"/authenticate", "/request-ratings", "/api", "/css/**", "/js/**");
//        web.ignoring().antMatchers(/*"public/**", */"/authenticate", "/request-ratings", "/api", "/resources/**", "/static/**", "/css/**", "/js/**");
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
//                .csrf().disable()
//                .csrf()
//                    .ignoringAntMatchers("/authenticate", "/request-ratings", "/api")
//                .and()
                .authorizeRequests()
                    .antMatchers("/registration", "/user/registration")
                    .permitAll()
                    .antMatchers("/", "/ratings", "/users")
                    .authenticated()
                    .anyRequest()
                    .authenticated()
                .and()
                    .formLogin()
                    .loginPage("/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .permitAll()
                .and()
                    .logout()
                    .logoutSuccessUrl("/login")
                    .permitAll();
    }
}
