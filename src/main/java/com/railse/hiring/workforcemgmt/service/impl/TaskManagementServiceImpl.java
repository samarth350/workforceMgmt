package com.railse.hiring.workforcemgmt.service.impl;


import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.InMemoryCommentRepository;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TaskManagementServiceImpl implements TaskManagementService {


   private final TaskRepository taskRepository;
   private final ITaskManagementMapper taskMapper;
   private final InMemoryCommentRepository commentRepository;


    public TaskManagementServiceImpl(TaskRepository taskRepository,
                                    ITaskManagementMapper taskMapper,
                                    InMemoryCommentRepository commentRepository) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.commentRepository = commentRepository;
    }

    private void logActivity(Long taskId, String message, Long userId) {
        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setCommentText(message);
        comment.setTimestamp(System.currentTimeMillis());
        comment.setUserId(userId != null ? userId : 0L); // 0 = system
        comment.setSystemGenerated(true);
        commentRepository.save(comment);
    }



   @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        TaskManagementDto dto = taskMapper.modelToDto(task);
        List<TaskComment> comments = commentRepository.findByTaskId(id);

        // Sort by timestamp
        comments.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        dto.setComments(comments);

        return dto;
    }


   @Override
   public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
       List<TaskManagement> createdTasks = new ArrayList<>();
       for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
           TaskManagement newTask = new TaskManagement();
           newTask.setReferenceId(item.getReferenceId());
           newTask.setReferenceType(item.getReferenceType());
           newTask.setTask(item.getTask());
           newTask.setAssigneeId(item.getAssigneeId());
           newTask.setPriority(item.getPriority() != null ? item.getPriority() : Priority.MEDIUM);
           newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
           newTask.setStatus(TaskStatus.ASSIGNED);
           newTask.setDescription("New task created.");
           TaskManagement savedTask = taskRepository.save(newTask);
            createdTasks.add(savedTask);
            logActivity(savedTask.getId(), "Task created", null);

       }
       return taskMapper.modelListToDtoList(createdTasks);
   }


   @Override
   public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
       List<TaskManagement> updatedTasks = new ArrayList<>();
       for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
           TaskManagement task = taskRepository.findById(item.getTaskId())
                   .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));


           if (item.getTaskStatus() != null) {
               task.setStatus(item.getTaskStatus());
               logActivity(task.getId(), "Status changed to " + item.getTaskStatus(), task.getAssigneeId());
           }
           if (item.getDescription() != null) {
               task.setDescription(item.getDescription());
                logActivity(task.getId(), "Description updated", task.getAssigneeId());
           }
           updatedTasks.add(taskRepository.save(task));
       }
       return taskMapper.modelListToDtoList(updatedTasks);
   }


   @Override
   public String assignByReference(AssignByReferenceRequest request) {
       List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
       List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(request.getReferenceId(), request.getReferenceType());


       for (Task taskType : applicableTasks) {
            List<TaskManagement> tasksOfType = existingTasks.stream()
                    .filter(t -> t.getTask() == taskType && t.getStatus() != TaskStatus.COMPLETED)
                    .collect(Collectors.toList());

            if (!tasksOfType.isEmpty()) {
                // Keep one, reassign it
                TaskManagement activeTask = tasksOfType.get(0);
                activeTask.setAssigneeId(request.getAssigneeId());
                taskRepository.save(activeTask);

                // Cancel the rest
                for (int i = 1; i < tasksOfType.size(); i++) {
                    TaskManagement duplicate = tasksOfType.get(i);
                    duplicate.setStatus(TaskStatus.CANCELLED);
                    taskRepository.save(duplicate);
                }
            } else {
                // No task exists: create a new one
                TaskManagement newTask = new TaskManagement();
                newTask.setReferenceId(request.getReferenceId());
                newTask.setReferenceType(request.getReferenceType());
                newTask.setTask(taskType);
                newTask.setAssigneeId(request.getAssigneeId());
                newTask.setStatus(TaskStatus.ASSIGNED);
                taskRepository.save(newTask);
            }
        }
       return "Tasks assigned successfully for reference " + request.getReferenceId();
   }


   @Override
   public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
       List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());


       // BUG #2 is here. It should filter out CANCELLED tasks but doesn't.
       List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
                .filter(task ->
                    // Include active tasks with deadline within the range
                    (task.getTaskDeadlineTime() >= request.getStartDate()
                    && task.getTaskDeadlineTime() <= request.getEndDate() 
                    && task.getStatus() != TaskStatus.COMPLETED)
                    ||
                    // Or include tasks before the range that are still active
                    (task.getTaskDeadlineTime() < request.getStartDate()
                    && task.getStatus() != TaskStatus.COMPLETED)
                )
               .collect(Collectors.toList());


       return taskMapper.modelListToDtoList(filteredTasks);
   }
   @Override
    public TaskManagementDto updateTaskPriority(UpdateTaskPriorityRequest request) {
        TaskManagement task = taskRepository.findById(request.getTaskId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));

        task.setPriority(request.getPriority());
        logActivity(task.getId(), "Priority changed to " + request.getPriority(), task.getAssigneeId());
        taskRepository.save(task);

        return taskMapper.modelToDto(task);
    }

    @Override
    public void addComment(TaskCommentRequest request) {
        TaskManagement task = taskRepository.findById(request.getTaskId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));

        TaskComment comment = new TaskComment();
        comment.setTaskId(request.getTaskId());
        comment.setCommentText(request.getCommentText());
        comment.setTimestamp(System.currentTimeMillis());
        comment.setUserId(request.getUserId());
        comment.setSystemGenerated(false);

        commentRepository.save(comment);
    }

    @Override
    public List<TaskManagementDto> getTasksByPriority(Priority priority) {
        List<TaskManagement> allTasks = taskRepository.findAll();

        return taskMapper.modelListToDtoList(
            allTasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList())
        );
    }

}