package emsbj.config;

import emsbj.UrlLocaleInterceptor;
import emsbj.admin.AdminGradeController;
import emsbj.controller.LocalizedController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    public static final String[] supportedLocalesArray = { "en", "bg" };
    public static final Collection<String> supportedLocales = Arrays.asList(supportedLocalesArray);
    public static final Locale defaultLocale = Locale.forLanguageTag(supportedLocalesArray[0]);
    public static final String defaultLocalePath = "/" + defaultLocale.toLanguageTag();
    public static final String localePathParam = "/{locale:en|bg}";
    public static final String addPath = "/add";
    public static final String listName = "list";
    public static final String addName = "add";
    public static final String detailsName = "details";
    public static final Map<String, Predicate<Class<?>>> pathPrefixes;
    public static final Map<Predicate<Class<?>>, Supplier<Optional<?>>> pathPrefixValueSuppliers;
    static {
        Predicate<Class<?>> isLocalizedController =
            LocalizedController.class::isAssignableFrom;

        pathPrefixes = new LinkedHashMap<>();
        pathPrefixes.put(localePathParam, isLocalizedController);

        pathPrefixValueSuppliers = new LinkedHashMap<>();
        pathPrefixValueSuppliers.put(isLocalizedController, () ->
            Optional.of(LocaleContextHolder.getLocale().toLanguageTag()));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        UrlLocaleInterceptor urlLocaleInterceptor = new UrlLocaleInterceptor();
        registry.addInterceptor(urlLocaleInterceptor)
            .addPathPatterns(urlLocaleInterceptor.getPathPatterns());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("/WEB-INF/static/");
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver(ApplicationContext applicationContext) {
        // SpringResourceTemplateResolver automatically integrates with Spring's own
        // resource resolution infrastructure, which is highly recommended.
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        // HTML is the default value, added here for the sake of clarity.
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // Template cache is true by default. Set to false if you want
        // templates to be automatically updated when modified.
        templateResolver.setCacheable(true);
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver templateResolver) {
        // SpringTemplateEngine automatically applies SpringStandardDialect and
        // enables Spring's own MessageSource message resolution mechanisms.
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        // Enabling the SpringEL compiler with Spring 4.2.4 or newer can
        // speed up execution in most scenarios, but might be incompatible
        // with specific cases when expressions in one template are reused
        // across different data types, so this flag is "false" by default
        // for safer backwards compatibility.
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        for (Map.Entry<String, Predicate<Class<?>>> entry : pathPrefixes.entrySet()) {
            configurer.addPathPrefix(entry.getKey(), entry.getValue());
        }
    }

    @Autowired
    public void setHandlerMapping(RequestMappingHandlerMapping mapping)
        throws NoSuchMethodException {

        RequestMappingInfo info = RequestMappingInfo
            .paths("/admin/grade-names").methods(RequestMethod.GET).build();

        Method method = AdminGradeController.class.getMethod("list", Model.class);

//        mapping.registerMapping(info, "adminGradeController", method);
    }
}
