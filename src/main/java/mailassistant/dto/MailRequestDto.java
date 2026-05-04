package mailassistant.dto;

import lombok.Data;

@Data
public class MailRequestDto {
    private String to;      // 받는 사람
    private String cc;      // 참조
    private String body;    // 메일 본문
}
