package ru.kata.spring.boot_security.demo.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/user").setViewName("user");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/admin/**") // Путь, для которого применяются настройки CORS
                .allowedOriginPatterns("*") // Разрешить все домены (или укажите конкретные)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // Разрешенные HTTP-методы
                .allowedOrigins("classpath:/static/")
                .allowedHeaders("*") // Разрешить все заголовки
                .allowCredentials(true) // Разрешить учетные данные
                .maxAge(3600); // Время кэширования CORS-предзапросов (в секундах)
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}
