package com.enhinck.swagger;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>swagger配置
 *
 * @author xiaomi（huenbin）
 * @since 2021-05-18 17:44
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Value("${spring.application.name}")
    public String applicationName;

    private static final List<String> DEFAULT_EXCLUDE_PATH = Arrays.asList("/error","/rpc/**");
    private static final String BASE_PATH = "/**";

    /**
     * 对前端暴露接口
     *
     * @return
     */
    @Bean
    public Docket postsApi() {
        List<Parameter> pars = new ArrayList<>();
        return new Docket(DocumentationType.SWAGGER_2).groupName(applicationName).apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.basePackage("com.enhinck"))
                .paths(postPaths()).build()
                .globalOperationParameters(pars);
    }


    private Predicate<String> postPaths() {
        List<Predicate<String>> excludePath = new ArrayList<>();
        DEFAULT_EXCLUDE_PATH.forEach(path -> excludePath.add(PathSelectors.ant(path)));
        List<Predicate<String>> basePath = new ArrayList();
        basePath.add(PathSelectors.ant(BASE_PATH));
        return Predicates.and(Predicates.not(Predicates.or(excludePath)), Predicates.or(basePath));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("demo API")
                .description("spring cloud API reference for developers").termsOfServiceUrl("http://github.com")
                .contact(new Contact("xiaomi", "http://github.com", "enhinck@163.com"))
                .license("demo License").licenseUrl("enhinck@163.com").version("1.0").build();
    }

}