package hexlet.code.repository;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, QuerydslPredicateExecutor<Task> {
    void deleteById(long id);
    Optional<Task> findFirstByAuthorIdOrExecutorId(long authorId, long executorId);
    Optional<Task> findByTaskStatusId(long id);
    List<Task> findByLabels(Label label);
}
