package com.example.chat.DTO.ConnectionRequestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendRequestDTO {
    @NotNull
    private Long targetId;
}
