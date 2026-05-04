package mailassistant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @GetMapping("/")
    public String home(Model model) {
        // 화면(HTML)으로 보낼 데이터를 세팅합니다.
        model.addAttribute("message", "AI 메일 어시스턴트 서버 구동 완료! 🎉");

        // templates 폴더 아래에 있는 index.html을 화면에 띄우라는 의미입니다.
        return "index";
    }
}
