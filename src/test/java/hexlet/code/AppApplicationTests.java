package hexlet.code;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled("now disabled")
class AppApplicationTests {

    @Test
    void testInit() {
        assertThat(true).isTrue();
    }
}
