package hexlet.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {

    @NotNull
    @NotBlank
    private String name;

    private String description;

    private long executorId;

    @NotNull
    private long taskStatusId;

    private Set<Long> labelIds;
}
