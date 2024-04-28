package com.lantanagroup.servers.uscoreserver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.lantanagroup.common.ServerProperties;

@Configuration
@ConfigurationProperties(prefix = "uscoreserver")
public class UsCoreServerProperties extends ServerProperties {
  
}
