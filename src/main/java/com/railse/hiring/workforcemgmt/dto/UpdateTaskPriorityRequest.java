package com.railse.hiring.workforcemgmt.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
	import com.fasterxml.jackson.databind.annotation.JsonNaming;
	import com.railse.hiring.workforcemgmt.model.enums.Priority;
	import lombok.Data;

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public class UpdateTaskPriorityRequest {
		private Long taskId;
		private Priority priority;
	}

