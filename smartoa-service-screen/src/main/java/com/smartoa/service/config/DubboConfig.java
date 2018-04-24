package com.smartoa.service.config;

import com.alibaba.dubbo.config.ProviderConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * DubboConfig
 *
 * @Author Jez
 * @Date 2016/11/8
 */
@Configuration
@ImportResource("classpath:config/dubbo-service-screen-${spring.profiles.active}.xml")
public class DubboConfig {
    @Bean
    public ProviderConfig provider() {
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setPayload(1073741824);
        return providerConfig;
    }
}
