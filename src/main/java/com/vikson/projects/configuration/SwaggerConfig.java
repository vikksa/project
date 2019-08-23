package com.vikson.projects.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
@PropertySource("classpath:application.yml")
public class SwaggerConfig {

    public static final String DT_HOMEPAGE = "";

    @Value("${springfox.documentation.swagger.v2.path}")
    private String swagger2Endpoint;

    @Bean
    @Autowired
    public Docket api(ServletContext servletContext) {
        List<ResponseMessage> errorResponseList = new ArrayList<ResponseMessage>(){{
            add(new ResponseMessageBuilder().code(500).message("Server Error - Something went wrong on the server.").build());
            add(new ResponseMessageBuilder().code(404).message("Not Found - Item could not be found").build());
            add(new ResponseMessageBuilder().code(403).message("Forbidden - User is not allowed to access this resource").build());
            add(new ResponseMessageBuilder().code(401).message("Unauthorized - User is not authorized (OAuth2)").build());
            add(new ResponseMessageBuilder().code(400).message("Bad Request - Invalid Request").build());
        }};

        List<ResponseMessage> extendedResponseList = new ArrayList<ResponseMessage>(errorResponseList) {{
            add(new ResponseMessageBuilder().code(409).message("Conflict - Item with the given ID already exists").build());
        }};

        return new Docket(DocumentationType.SWAGGER_2)
                .host("staging.docu.solutions")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(regex("(/api/v2.*)|(/api/v3.*)|(/plans-api/v3.*)"))
                .build()
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, errorResponseList)
                .globalResponseMessage(RequestMethod.POST, extendedResponseList)
                .globalResponseMessage(RequestMethod.PUT, errorResponseList)
                .globalResponseMessage(RequestMethod.PATCH, errorResponseList)
                .globalResponseMessage(RequestMethod.DELETE, errorResponseList)
                .globalOperationParameters(Collections.singletonList(
                        new ParameterBuilder()
                                .name("Authorization")
                                .description("OAuth2 Token")
                                .parameterType("header")
                                .modelRef(new ModelRef("string"))
                                .required(true)
                                .build()
                ));
    }


    private ApiInfo apiInfo() {
        return new ApiInfo(
                "docu tools Project API",
                "This Web Service offers Endpoints for managing projects, plans and their respective folders in docu tools. \n" +
                        "The required parameters for each API will be listed in the API description. \n" +
                        "The possible values for query parameters can be seen if you press 'Try it out' and check the list.",
                "API V2",
                DT_HOMEPAGE,
                new Contact("vikson", DT_HOMEPAGE, "email"),
                "Commercial", DT_HOMEPAGE, Collections.emptyList());
    }

}
