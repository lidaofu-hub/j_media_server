//
package com.ldf.media.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
@EnableSwagger2WebMvc
public class SwaggerConfiguration implements WebMvcConfigurer {
    @Value("${swagger.enable:true}")
    private Boolean enable;

    @Bean(value = "defaultApi")
    public Docket defaultApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .enable(enable)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("JMediaServer接口文档")
                .description("# JMediaServer接口文档")
                .termsOfServiceUrl("http://127.0.0.1:8899/doc.html")
                .version("v1.0.0")
                .contact(new Contact("李道甫", "", "746101210@qq.com"))
                .build();
    }

}
