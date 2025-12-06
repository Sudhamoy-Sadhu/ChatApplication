package com.example.chat.DTO.ConnectionRequestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllConnectionDTO {
    private Long requestId;
    private Long requesterId;
    private Long targetId;
    private String requesterName;
    private String requesterEmail;
    private String requesterProfilePic;

    private String targetName;
    private String targetEmail;
    private String targetProfilePic;

    private String status;
}
