package hexlet.code.service;

import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LabelServiceImpl implements LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public Label createLabel(LabelDto labelDto) {
        final Label label = new Label();
        label.setName(labelDto.getName());
        return labelRepository.save(label);
    }

    @Override
    public Label updateLabel(Long id, LabelDto labelDto) {
        final Label label = labelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Label not found"));
        label.setName(labelDto.getName());
        return labelRepository.save(label);
    }

    @Override
    public void deleteLabel(Long id) {
        final Label label = labelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Label not found"));
        List<Task> tasks = taskRepository.findByLabels(label);
        if (tasks.isEmpty()) {
            labelRepository.delete(label);
        } else {
            throw new DataIntegrityViolationException("Can't delete label with existing task(s)");
        }
    }
}
