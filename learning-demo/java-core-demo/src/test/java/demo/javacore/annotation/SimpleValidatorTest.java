package demo.javacore.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleValidatorTest {

    /** Annotations on record components end up on the generated private fields (they target FIELD). */
    record CreateEventForm(
            @Required @MaxLength(20) String name,
            @Required(message = "venue is required") String venue,
            int vipRows) {
    }

    @Test
    void validForm_hasNoViolations() {
        assertThat(SimpleValidator.validate(new CreateEventForm("Rock Night", "Hall 1", 2))).isEmpty();
    }

    @Test
    void blankAndMissingValues_areReported_withTheirMessages() {
        assertThat(SimpleValidator.validate(new CreateEventForm("   ", null, 0)))
                .containsExactlyInAnyOrder("name: must not be blank", "venue: venue is required");
    }

    @Test
    void tooLongText_isReported() {
        assertThat(SimpleValidator.validate(new CreateEventForm("A very very long event name", "Hall 1", 0)))
                .containsExactly("name: length must be <= 20");
    }

    @Test
    void annotationsCanBeReadWithReflection() throws NoSuchFieldException {
        Field name = CreateEventForm.class.getDeclaredField("name");
        Field vipRows = CreateEventForm.class.getDeclaredField("vipRows");

        assertThat(name.isAnnotationPresent(Required.class)).isTrue();
        assertThat(name.getAnnotation(MaxLength.class).value()).isEqualTo(20);
        assertThat(vipRows.getDeclaredAnnotations()).isEmpty();
    }
}
