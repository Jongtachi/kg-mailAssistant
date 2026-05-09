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
        // 프론트에서 모드 값을 안 보냈다면 기본값은 'formal(정중하게)'로 세팅
        String mode = request.mode() != null ? request.mode() : "formal";
        System.out.println("📬 [AI 서버] 모드 [" + mode + "] 로 교정 요청 도착!");

        String systemPrompt = "";

        // 💡 선택된 모드에 따라 AI의 '자아(프롬프트)'를 바꿉니다!
        if ("concise".equals(mode)) {
            systemPrompt = "당신은 한국 대기업의 비즈니스 텍스트 요약 전문가입니다.\n"
                    + "입력된 HTML 문서의 태그 구조를 유지한 채, 텍스트 내용을 핵심만 남기고 아주 간결하게 줄이세요.\n"
                    + "[필수 지침]\n"
                    + "1. 불필요한 수식어나 긴 인사말은 과감히 삭제하고 요점만 '하십시오체'로 전달할 것.\n"
                    + "2. 항목이 많을 경우 1. 2. 3. 같은 개조식(Bullet point)으로 정리해도 좋음.\n"
                    + "3. 🚨절대 금지🚨: '요약 내용:', '수정 사항:' 등 AI가 자의적으로 작성하는 요약, 보고, 부연 설명은 절대 금지합니다.\n"
                    + "4. 오직(ONLY) 교정된 HTML 텍스트 결과물 하나만 출력하세요.";
        }
        else if ("english".equals(mode)) {
            systemPrompt = "당신은 실리콘밸리 IT 기업의 비즈니스 영문 번역가입니다.\n"
                    + "입력된 HTML 문서의 태그 구조를 100% 유지한 채, 텍스트 내용을 세련되고 프로페셔널한 비즈니스 영어로 번역하세요.\n"
                    + "[필수 지침]\n"
                    + "1. 단순 직역이 아닌, 네이티브가 자주 쓰는 정중한 비즈니스 이메일 표현을 사용할 것.\n"
                    + "2. 🚨절대 금지🚨: 'Here is the translation:', 'Note:', '번역 내용:' 등 번역 전/후의 AI 인사말이나 부연 설명은 절대 금지합니다.\n"
                    + "3. 오직(ONLY) 번역된 HTML 텍스트 결과물 하나만 출력하세요.";
        }
        else {
            // 기본 모드 (formal)
            systemPrompt = "당신은 한국 대기업의 비즈니스 텍스트 교정 전문가입니다.\n"
                    + "입력된 HTML 문서의 태그 구조를 100% 유지한 채, 텍스트 내용만 격식 있는 '비즈니스 하십시오체(다나까체)'로 교정하세요.\n\n"
                    + "[필수 지침]\n"
                    + "1. 어조 및 인사말: '~요', '~구려' 등 구어체/비격식체는 절대 금지. 인사말은 삭제하지 말고 반드시 '안녕하십니까' 등의 정중한 표현으로 '수정'하여 유지할 것.\n"
                    + "2. 비속어/은어 순화: '존나게' 등 업무에 부적절한 단어는 '언제든지', '편하게', '대단히' 등으로 완벽하게 순화하거나 삭제할 것.\n"
                    + "3. 용어: 오늘->금일, 내일->명일, 어제->작일, 보내다->송부하다/전달하다.\n"
                    + "4. 🚨절대 금지🚨: '교정 내용:', '수정 사항:', '참고:' 등 AI가 자의적으로 작성하는 요약, 보고, 부연 설명은 절대 금지합니다.\n"
                    + "5. 오직(ONLY) 교정된 HTML 텍스트 결과물 하나만 출력하세요.\n\n"
                    + "[교정 예시]\n"
                    + "입력: <p>안녕하시구료, 존나게 고생 많으십니다. <b>오늘</b> 무슨 날인지 알려줄수 있습니까?</p>\n"
                    + "출력: <p>안녕하세요, 노고가 많으십니다. <b>금일</b> 무슨 날인지 공유 부탁드립니다.</p>";
        }

        String prompt = request.text();

        Map<String, Object> ollamaRequest = new HashMap<>();
        ollamaRequest.put("model", OLLAMA_MODEL); // "llama3.1"
        ollamaRequest.put("system", systemPrompt);
        ollamaRequest.put("prompt", prompt);
        ollamaRequest.put("stream", false);

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