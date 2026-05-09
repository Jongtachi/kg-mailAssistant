package mailassistant.dto;

// Java 17의 record를 사용하면 불필요한 Getter/Setter 없이 깔끔하게 DTO 생성 가능합니다.
public record MailRequest(String text, String mode) {
}
