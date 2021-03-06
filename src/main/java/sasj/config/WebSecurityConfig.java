package sasj.config;

import sasj.controller.Extensions;
import sasj.abconj2html.WebController;
import sasj.web.RedirectingAuthenticationSuccessHandler;
import sasj.web.RedirectingLoginUrlAuthenticationEntryPoint;
import sasj.controller.SecuredController;
import sasj.data.user.JournalUserDetailsService;
import sasj.data.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.ForwardLogoutSuccessHandler;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter  {
    private Map<String, SecuredController> securedControllerMap;
    private Map<String, WebController> webControllerMap = Collections.emptyMap();

    @Autowired
    private JournalUserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Extensions extensions;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/favicon.ico", "/img/**", "/css/**", "/js/**");
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry =
            httpSecurity
                .csrf()
                    .disable()
                .exceptionHandling()
                    .authenticationEntryPoint(
                        new RedirectingLoginUrlAuthenticationEntryPoint("/sign-in"))
                    .and()
                .authorizeRequests();
        for (SecuredController securedController : securedControllerMap.values()) {
            securedController.configure(registry);
        }
        registry
            .anyRequest()
                .authenticated()
                .and()
            .formLogin()
                .loginPage(WebMvcConfig.localePathParam + "/sign-in")
                .permitAll()
                .successHandler(successHandler())
                .and()
            .logout()
                .logoutUrl(WebMvcConfig.localePathParam + "/sign-out")
                .permitAll()
                .logoutSuccessHandler(new ForwardLogoutSuccessHandler("/") {
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        String targetUrl = extensions.getURLs().users().signIn(request);
                        response.sendRedirect(targetUrl);
                    }
                })
                .and()
            .headers()
                .frameOptions()
                .sameOrigin();
    }

    @Bean
    public RedirectingAuthenticationSuccessHandler successHandler() {
        RedirectingAuthenticationSuccessHandler successHandler =
            new RedirectingAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("requested");
        return successHandler;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(User.Role.createHierarchy(
            new User.Role[]{User.Role.principal, User.Role.admin},
            new User.Role[]{User.Role.admin, User.Role.teacher},
            new User.Role[]{User.Role.teacher, User.Role.student},
            new User.Role[]{User.Role.student, User.Role.user}
        ));
        return roleHierarchy;
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Autowired
    public void configureAuthenticationManager(AuthenticationManagerBuilder authenticationMgr, PasswordEncoder passwordEncoder) throws Exception {
        authenticationMgr
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
    }

    @Autowired
    public void findSecuredControllers(ApplicationContext applicationContext) {
        securedControllerMap =
            applicationContext.getBeansOfType(SecuredController.class);
    }

//    @Autowired // a switch for controllers based on AbstractController
    public void findSecuredUrlControllers(ApplicationContext applicationContext) {
        webControllerMap = applicationContext.getBeansOfType(WebController.class);
        System.out.println(webControllerMap);
    }

    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping(ApplicationContext applicationContext) {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        for (WebController<?> controller : webControllerMap.values()) {
            String urlPattern = controller.getUrlPattern().getAntMatcherPattern();
            simpleUrlHandlerMapping.setUrlMap(
                Collections.singletonMap(urlPattern, controller)
            );
        }
        return simpleUrlHandlerMapping;
    }
}
