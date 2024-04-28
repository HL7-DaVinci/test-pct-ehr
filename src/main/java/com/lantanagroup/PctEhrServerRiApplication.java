package com.lantanagroup;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.lantanagroup.common.CommonConfig;
//import com.lantanagroup.servers.uscoreclient.UsCoreClientConfig;
import com.lantanagroup.servers.uscoreserver.UsCoreServerConfig;


@SpringBootApplication(exclude = {ElasticsearchRestClientAutoConfiguration.class, ThymeleafAutoConfiguration.class})
public class PctEhrServerRiApplication {

  public static void main(String[] args) {
		new SpringApplicationBuilder().sources(PctEhrServerRiApplication.class)
			.parent(CommonConfig.class).web(WebApplicationType.NONE)
			.child(UsCoreServerConfig.class).web(WebApplicationType.SERVLET)
			.run(args);
	}

}