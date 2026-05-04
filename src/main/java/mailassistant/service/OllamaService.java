package mailassistant.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class OllamaService {

    // Spring Boot 3에서 제공하는 최신 HTTP 클라이언트 (RestClient)
    private final RestClient restClient = RestClient.create();

    public String getAiResponse(String promptText) {

        // ✨ 군더더기 없이 결과만 출력하도록 강력하게 통제된 프롬프트
        String systemPrompt = """
        다음 [원본] 텍스트를 완벽한 비즈니스 메일 형식으로 교정하시오.
          - 예의 없는 어투나 비속어는 정중하게 순화할 것.
          - 맞춤법 및 띄어쓰기를 교정할 것.
          - 가독성을 위해 적절히 단락(줄바꿈)을 나눌 것.
          - 부가 설명, 인사말, 사과, 교정 이유, 요약 등은 절대 작성하지 말 것.
          - 오직 교정된 메일 본문 내용만 출력할 것.
    
          [원본]
          %s
    
          [교정본]
        """ + promptText;

        Map<String, Object> requestBody = Map.of(
                "model", "llama3.1",
                "prompt", systemPrompt,
                "stream", false
        );

        try {
            Map response = restClient.post()
                    .uri("http://localhost:11434/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return response != null ? (String) response.get("response") : "AI 응답 오류";

        } catch (Exception e) {
            System.err.println("Ollama 연결 에러: " + e.getMessage());
            return "오류 발생: Ollama 서버가 켜져 있는지 확인해 주세요.";
        }
    }
}
