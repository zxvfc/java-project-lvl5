package hexlet.code.service;

import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Override
    public Task createTask(TaskDto taskDto) {
        final Task task = new Task();

        final User author = userService.getCurrentUser();
        final TaskStatus taskStatus = taskStatusRepository.findById(taskDto.getTaskStatusId()).get();

        task.setName(taskDto.getName());
        task.setDescription(taskDto.getDescription());
        task.setAuthor(author);
        task.setTaskStatus(taskStatus);
        task.setLabels(labelRepository.findAllById(taskDto.getLabelIds()));
        final Long executorId = taskDto.getExecutorId();
        if (executorId != null) {
            task.setExecutor(userRepository.findById(executorId).get());
        }
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(Long id, TaskDto taskDto) {
        final Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));

        final TaskStatus taskStatus = taskStatusRepository.findById(taskDto.getTaskStatusId()).get();

        task.setName(taskDto.getName());
        task.setDescription(taskDto.getDescription());
        task.setTaskStatus(taskStatus);
        task.setLabels(labelRepository.findAllById(taskDto.getLabelIds()));
        final Long executorId = taskDto.getExecutorId();
        if (executorId != null) {
            task.setExecutor(userRepository.findById(executorId).get());
        }
        return taskRepository.save(task);
    }
}
