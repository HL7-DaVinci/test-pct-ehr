package com.lantanagroup.servers.uscoreclient;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.lantanagroup.common.ServerProperties;

@Configuration
@ConfigurationProperties(prefix = "uscoreclient")
public class UsCoreClientProperties extends ServerProperties {
  
}
