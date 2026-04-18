package com.jaycong.boot.modules.mail.service;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TemplateRenderService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_\\.]+)\\s*}}");

    public String render(String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template)) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = resolveVariable(variables, key);
            String replacement = value == null ? "" : Matcher.quoteReplacement(Objects.toString(value, ""));
            matcher.appendReplacement(output, replacement);
        }
        matcher.appendTail(output);
        return output.toString();
    }

    @SuppressWarnings("unchecked")
    private Object resolveVariable(Map<String, Object> variables, String key) {
        if (!key.contains(".")) {
            return variables.get(key);
        }
        String[] segments = key.split("\\.");
        Object current = variables;
        for (String segment : segments) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                return null;
            }
            current = ((Map<String, Object>) currentMap).get(segment);
            if (current == null) {
                return null;
            }
        }
        return current;
    }
}

