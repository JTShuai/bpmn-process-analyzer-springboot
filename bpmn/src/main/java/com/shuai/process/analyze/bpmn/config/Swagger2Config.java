package com.shuai.process.analyze.bpmn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * swagger配置
 */

@EnableSwagger2
@Configuration
public class Swagger2Config {
    @Bean
    public Docket swagger() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.prothentic.template.controller"))
                .build()
	              .directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
	              .directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class)
                ;
//                .securitySchemes(securitySchemes())
//                .securityContexts(securityContexts());
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("平台相关API文档")
                .description("平台相关接口说明文档")
                .termsOfServiceUrl("")
                .contact(new Contact("test", "", ""))
                .version("1.0")
                .build();
    }
    


}
