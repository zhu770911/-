package com.smartparking.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 灵活解析 LocalDateTime，支持多种前端格式：
 *   "2026-07-11T14:30:00" (ISO)
 *   "2026-07-11 14:30"   (小程序 picker)
 */
public class SmartLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    };

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String text = p.getText().trim();
        if (text.isEmpty()) return null;

        for (DateTimeFormatter fmt : FORMATTERS) {
            try {
                return LocalDateTime.parse(text, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }

        // 最后兜底：T 分隔但缺秒，补 :00
        if (text.contains("T") && text.length() == 16) {
            return LocalDateTime.parse(text + ":00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        throw new IOException("无法解析日期时间: " + text + "，支持的格式: yyyy-MM-ddTHH:mm:ss / yyyy-MM-dd HH:mm");
    }
}
