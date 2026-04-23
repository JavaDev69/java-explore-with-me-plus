package ru.practicum.stats.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class JacksonConfiguration implements WebMvcConfigurer {

    @Bean
    public ObjectMapper objectMapper() {
        System.out.println("=== НАЧАЛО ИНИЦИАЛИЗАЦИИ CUSTOM OBJECTMAPPER ===");
        System.out.println("Формат даты: yyyy-MM-dd HH:mm:ss");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

        mapper.registerModule(module);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        System.out.println("=== CUSTOM OBJECTMAPPER УСПЕШНО СОЗДАН ===");
        return mapper;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Кастомный конвертер для LocalDateTime прямо в конфигурации
        Converter<String, LocalDateTime> localDateTimeConverter = new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                try {
                    // Сначала пробуем парсить с пробелом (формат по спецификации)
                    return LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                                ("Invalid date format. Expected: 'yyyy-MM-dd HH:mm:ss'. Got: " + source), e);
                    }
                }
        };

        registry.addConverter(localDateTimeConverter);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        converters.add(0, converter);
        System.out.println("=== HTTP CONVERTER С НАШИМ OBJECTMAPPER ЗАРЕГИСТРИРОВАН ===");
    }
}
