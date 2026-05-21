package com.english.learning.config;

import com.english.learning.shiro.JakartaAuthFilter;
import com.english.learning.shiro.UserRealm;
import jakarta.servlet.Filter;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    public DefaultWebSecurityManager securityManager(UserRealm userRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userRealm);
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean filter = new ShiroFilterFactoryBean();
        filter.setSecurityManager(securityManager);
        
        // 注册自定义过滤器
        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("authc", new JakartaAuthFilter());
        filter.setFilters(filters);
        
        Map<String, String> filterMap = new LinkedHashMap<>();
        // 开放登录、注册接口及上传资源
        filterMap.put("/api/user/login", "anon");
        filterMap.put("/api/user/register", "anon");
        filterMap.put("/uploads/**", "anon");
        // 文献、词汇、语法等暂设为需要认证，由上面的 JakartaAuthFilter 处理并返回 401
        filterMap.put("/**", "authc");
        
        filter.setFilterChainDefinitionMap(filterMap);
        return filter;
    }
}
