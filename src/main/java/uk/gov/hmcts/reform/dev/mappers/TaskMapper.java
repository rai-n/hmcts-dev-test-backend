package uk.gov.hmcts.reform.dev.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.models.Task;

/**
 * MapStruct mapper for Task entity/DTO conversions.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TaskMapper {

    /**
     * Converts CreateTaskRequest to Task entity.
     * */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(CreateTaskRequest request);

    /**
     * Converts Task entity to TaskResponse.
     * Links populated by controller.
     */
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "type", ignore = true)
    TaskResponse toResponse(Task task);

    /**
     * Updates task status from UpdateTaskRequest.
     * Only status can be updated.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "dueDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateTaskRequest request, @MappingTarget Task task);
}
