package com.railse.hiring.workforcemgmt.repository;

import com.railse.hiring.workforcemgmt.model.TaskComment;
	import org.springframework.stereotype.Repository;

	import java.util.*;
	import java.util.concurrent.ConcurrentHashMap;
	import java.util.concurrent.atomic.AtomicLong;
	import java.util.stream.Collectors;

	@Repository
	public class InMemoryCommentRepository {
		private final Map<Long, TaskComment> commentStore = new ConcurrentHashMap<>();
		private final AtomicLong idGenerator = new AtomicLong(1);

		public TaskComment save(TaskComment comment) {
			if (comment.getId() == null) {
				comment.setId(idGenerator.getAndIncrement());
			}
			commentStore.put(comment.getId(), comment);
			return comment;
		}

		public List<TaskComment> findByTaskId(Long taskId) {
			return commentStore.values().stream()
					.filter(c -> c.getTaskId().equals(taskId))
					.sorted(Comparator.comparingLong(TaskComment::getTimestamp))
					.collect(Collectors.toList());
		}
	}
