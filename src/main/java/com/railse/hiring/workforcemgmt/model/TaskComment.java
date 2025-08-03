package com.railse.hiring.workforcemgmt.model;

import lombok.Data;

	@Data
	public class TaskComment {
		private Long id;
		private Long taskId;
		private String commentText;
		private Long userId;
		private Long timestamp;
		private boolean systemGenerated;
	}

