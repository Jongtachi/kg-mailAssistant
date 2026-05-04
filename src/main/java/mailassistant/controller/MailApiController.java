package mailassistant.controller;

import mailassistant.dto.MailRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = "https://gw.kggroup.co.kr")
public class MailApiController {

    // Ollama가 설치된 PC의 기본 주소 (보통 11434 포트를 사용합니다)
    private final String OLLAMA_API_URL = "http://localhost:11434/api/generate";

    // 사용할 모델 이름 (현재 다운로드 받아두신 모델명으로 변경하세요! 예: llama3, qwen2, eeve 등)
    private final String OLLAMA_MODEL = "llama3.1";

    @PostMapping("/correct")
    public ResponseEntity<String> correctMail(@RequestBody MailRequest request) {
        System.out.println("📬 [AI 서버] 익스텐션 본문 도착! Ollama로 교정 요청을 보냅니다...");

        // 1. 시스템 프롬프트: AI의 자아를 없애고 엄격한 규칙을 부여합니다.
        String systemPrompt = "당신은 한국어 비즈니스 이메일 교정 전문가입니다.\n"
                + "사용자의 메일 초안을 회사에서 쓰는 정중하고 격식 있는 존댓말(비즈니스 어조)로 수정하세요.\n"
                + "[절대 지켜야 할 규칙]\n"
                + "1. '안녕하시구려', '자네' 등의 비격식체나 반말은 '안녕하십니까', '요청하신' 등으로 완벽하게 수정할 것.\n"
                + "2. '네, 알겠습니다.', '여기 교정본입니다.', '죄송합니다.' 같은 너의 대답이나 인사말은 절대(Never) 출력하지 말 것.\n"
                + "3. 오직 교정이 완료된 메일 본문만 텍스트로 출력할 것.";

        // 2. 실제 데이터 (사용자 입력)
        String prompt = request.text();

        // 3. Ollama 상세 설정
        Map<String, Object> ollamaRequest = new HashMap<>();
        ollamaRequest.put("model", OLLAMA_MODEL);
        ollamaRequest.put("system", systemPrompt); // 시스템 역할 부여
        ollamaRequest.put("prompt", prompt);       // 순수 데이터만 전달
        ollamaRequest.put("stream", false);

        // ✨ 핵심: 창의성(temperature)을 0.1로 낮춰서 소설 쓰는 것을 원천 차단! (기본값은 0.8)
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.1);
        ollamaRequest.put("options", options);

        // HTTP 통신 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ollamaRequest, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            // 4. Ollama 서버로 요청 쏘기
            ResponseEntity<Map> response = restTemplate.exchange(
                    OLLAMA_API_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // 5. Ollama가 보내준 응답 텍스트만 쏙 뽑아내기
            String correctedText = (String) response.getBody().get("response");

            System.out.println("✨ [Ollama 응답 완료] \n" + correctedText);

            return ResponseEntity.ok(correctedText);

        } catch (Exception e) {
            System.err.println("🚨 Ollama 통신 중 에러 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Ollama 서버와 통신할 수 없습니다. 모델이 실행 중인지 확인해 주세요.");
        }
    }
}