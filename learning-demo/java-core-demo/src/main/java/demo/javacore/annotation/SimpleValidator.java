package demo.javacore.annotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads {@link Required} and {@link MaxLength} with reflection and returns the violations.
 *
 * <p>A mini version of what Bean Validation does for {@code @NotBlank @Size(max = 200)}
 * on CreateEventRequest in ticketbox-api: the annotations only describe the rule,
 * this class looks them up at runtime and enforces it.
 */
public final class SimpleValidator {

    private SimpleValidator() {
    }

    public static List<String> validate(Object target) {
        List<String> violations = new ArrayList<>();
        for (Field field : target.getClass().getDeclaredFields()) {
            Object value = readField(field, target);

            Required required = field.getAnnotation(Required.class); // null if the field has no @Required
            if (required != null && isBlank(value)) {
                violations.add(field.getName() + ": " + required.message());
            }

            MaxLength maxLength = field.getAnnotation(MaxLength.class);
            if (maxLength != null && value instanceof String text && text.length() > maxLength.value()) {
                violations.add(field.getName() + ": length must be <= " + maxLength.value());
            }
        }
        return violations;
    }

    private static Object readField(Field field, Object target) {
        try {
            field.setAccessible(true); // the field is private; reflection may still read it
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read field " + field.getName(), e);
        }
    }

    private static boolean isBlank(Object value) {
        return value == null || (value instanceof String text && text.isBlank());
    }
}
