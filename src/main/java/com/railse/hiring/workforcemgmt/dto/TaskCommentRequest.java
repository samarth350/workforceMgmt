package com.railse.hiring.workforcemgmt.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
	import com.fasterxml.jackson.databind.annotation.JsonNaming;
	import lombok.Data;

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public class TaskCommentRequest {
		private Long taskId;
		private String commentText;
		private Long userId;
	}

