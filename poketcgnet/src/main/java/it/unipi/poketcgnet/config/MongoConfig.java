package it.unipi.poketcgnet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class MongoConfig {

    // Il dataset è caricato via mongoimport e ha TUTTE le date come stringhe ISO
    // "yyyy-MM-dd". Di default Spring scriverebbe LocalDate come BSON Date: nello
    // stesso campo convivrebbero due tipi, e in MongoDB stringhe e Date non sono
    // mai confrontabili tra loro
    // Questi converter mantengono le scritture identiche al formato del dataset.
    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(List.of(
                new LocalDateToStringConverter(),
                new StringToLocalDateConverter()));
    }

    @WritingConverter
    static class LocalDateToStringConverter implements Converter<LocalDate, String> {
        @Override
        public String convert(LocalDate source) {
            return source.toString(); // ISO-8601: "yyyy-MM-dd"
        }
    }

    @ReadingConverter
    static class StringToLocalDateConverter implements Converter<String, LocalDate> {
        @Override
        public LocalDate convert(String source) {
            return LocalDate.parse(source);
        }
    }
}
