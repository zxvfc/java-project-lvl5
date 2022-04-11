package hexlet.code.dto;

import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public final class TaskDto {
    private @NotBlank @Size(min = 3, max = 1000) String name;

    private String description;

    private Long executorId;

    private @NotNull Long taskStatusId;

    private Set<Long> labelIds;

}
