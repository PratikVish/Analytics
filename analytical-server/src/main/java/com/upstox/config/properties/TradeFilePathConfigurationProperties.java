package com.upstox.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tfp")
@Data
public class TradeFilePathConfigurationProperties {
    private String tradeFilePath;
}
