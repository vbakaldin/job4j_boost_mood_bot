package ru.job4j.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class FakeTelegramCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var mode = context.getEnvironment().getProperty("telegram.mode", "real");
        return "fake".equals(mode);
    }
}