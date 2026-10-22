package us.laderman.blackjack.persistence;

import java.util.Set;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

public class DatabasePersistenceEnabledCondition implements Condition {
    private static final Set<String> PERSISTENT_MODES = Set.of("mysql", "h2");

    @Override
    public boolean matches(
            @NonNull ConditionContext context,
            @NonNull AnnotatedTypeMetadata metadata) {
        String mode = context.getEnvironment().getProperty("blackjack.database.mode", "mysql");
        return PERSISTENT_MODES.contains(mode.toLowerCase());
    }
}
