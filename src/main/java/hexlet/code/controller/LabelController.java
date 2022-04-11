package hexlet.code.controller;

import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.service.LabelServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;

import static hexlet.code.controller.LabelController.LABEL_PATH;
import static hexlet.code.controller.UserController.ID;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${base-url}" + LABEL_PATH)
@PreAuthorize("isAuthenticated()")
public class LabelController {

    public static final String LABEL_PATH = "/labels";

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelServiceImpl labelService;

    @Operation(summary = "Get Label by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label found", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Label.class))),
            @ApiResponse(responseCode = "401", description = "User is unauthorized"),
            @ApiResponse(responseCode = "404", description = "Label not found")
    })
    @GetMapping(ID)
    public Label getLabel(
            @Parameter(description = "Id of Label to be found")
            @PathVariable Long id) {
        return labelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Label not found"));
    }

    @Operation(summary = "Get list of all Labels")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all Labels", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Label.class))),
            @ApiResponse(responseCode = "401", description = "User is unauthorized")
    })
    @GetMapping
    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }

    @Operation(summary = "Create new Label")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label created", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Label.class))),
            @ApiResponse(responseCode = "401", description = "User is unauthorized"),
            @ApiResponse(responseCode = "422", description = "Data validation failed")
    })
    @PostMapping
    @ResponseStatus(CREATED)
    public Label createLabel(
            @Parameter(description = "Data for creating new Label", required = true)
            @RequestBody @Valid LabelDto labelDto) {
        return labelService.createLabel(labelDto);
    }

    @Operation(summary = "Update Label")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label updated", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Label.class))),
            @ApiResponse(responseCode = "401", description = "User is unauthorized"),
            @ApiResponse(responseCode = "404", description = "Label not found"),
            @ApiResponse(responseCode = "422", description = "Data validation failed")
    })
    @PutMapping(ID)
    public Label updateLabel(
            @Parameter(description = "Id of Label to be updated", required = true)
            @PathVariable Long id,
            @Parameter(description = "Data for updating Label", required = true)
            @RequestBody @Valid LabelDto labelDto
    ) {
        return labelService.updateLabel(id, labelDto);
    }

    @Operation(summary = "Delete Label")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label deleted", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Label.class))),
            @ApiResponse(responseCode = "401", description = "User is unauthorized"),
            @ApiResponse(responseCode = "404", description = "Label not found"),
            @ApiResponse(responseCode = "422", description = "Can't delete label with existing task(s)"),
    })
    @DeleteMapping(ID)
    public void deleteLabel(
            @Parameter(description = "Id of Label to be deleted")
            @PathVariable Long id) {
        labelService.deleteLabel(id);
    }
}
