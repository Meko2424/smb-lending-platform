package com.lending.platform.mapper;

import com.lending.platform.dto.response.ApplicationStatusHistoryResponse;
import com.lending.platform.entity.ApplicationStatusHistory;
import com.lending.platform.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStatusHistoryMapper {

    public ApplicationStatusHistoryResponse toResponse(
            ApplicationStatusHistory history
    ) {

        User user = history.getChangedByUser();

        return new ApplicationStatusHistoryResponse(
                history.getId(),
                history.getApplication().getId(),
                history.getFromStatus(),
                history.getToStatus(),
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                history.getComment(),
                history.getChangedAt()
        );
    }
}
