package net.ruj.cloudfuse.gdrive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableOAuth2Sso
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    public WebSecurityConfiguration() {
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**").authorizeRequests()
                .antMatchers(
                        "/",
                        "/login**"
                ).permitAll();
    }
}
