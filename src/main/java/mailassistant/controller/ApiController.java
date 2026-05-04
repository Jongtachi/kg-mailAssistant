package mailassistant.controller;

import mailassistant.dto.MailRequestDto;
import mailassistant.service.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final OllamaService ollamaService;

    // 화면에서 /api/chat 으로 데이터를 보내면 여기서 받습니다.
    @PostMapping("/chat")
    public String chatWithAi(@RequestBody MailRequestDto request) {

        // DTO 바구니에 담겨온 데이터 중 '본문(body)'만 꺼내서 AI에게 넘겨줍니다.
        return ollamaService.getAiResponse(request.getBody());
    }
}
