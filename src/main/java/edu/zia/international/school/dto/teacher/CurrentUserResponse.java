package edu.zia.international.school.dto.teacher;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CurrentUserResponse{
        private String employeeId;
        private String fullName;
}

