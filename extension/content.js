console.log("🚀 [불도저 탐색 모드] AI 버튼 로딩 완료");

// --- 1. 버튼 만들기 ---
const aiButton = document.createElement("button");
aiButton.innerHTML = "🤖 AI 교정";
aiButton.style.position = "fixed";
aiButton.style.bottom = "30px";
aiButton.style.right = "30px";
aiButton.style.padding = "15px 20px";
aiButton.style.backgroundColor = "#0056b3";
aiButton.style.color = "white";
aiButton.style.border = "none";
aiButton.style.borderRadius = "50px";
aiButton.style.boxShadow = "0 4px 10px rgba(0,0,0,0.3)";
aiButton.style.fontSize = "16px";
aiButton.style.fontWeight = "bold";
aiButton.style.cursor = "pointer";
aiButton.style.zIndex = "9999";
document.body.appendChild(aiButton);

// --- 2. iframe의 바닥까지 파고드는 불도저 함수 ---
function findLetterDiv(doc) {
    // 1단계: 지금 쳐다보고 있는 문서(doc)에 letter가 있는지 확인합니다.
    let letter = doc.querySelector('div[name="letter"]');
    if (letter) return letter; // 찾으면 바로 반환!

    // 2단계: 없으면? 현재 문서에 있는 모든 iframe의 문을 강제로 엽니다.
    let iframes = doc.querySelectorAll("iframe");
    for (let i = 0; i < iframes.length; i++) {
        try {
            let iframeDoc = iframes[i].contentDocument || iframes[i].contentWindow.document;
            if (iframeDoc) {
                // 3단계: 문을 연 iframe 안쪽으로 불도저 함수를 다시 투입합니다! (재귀 호출)
                let foundInside = findLetterDiv(iframeDoc);
                if (foundInside) return foundInside; // 안쪽에서 찾았으면 반환!
            }
        } catch (error) {
            // 권한이 없어서 못 여는 광고성 iframe 등은 조용히 무시하고 넘어갑니다.
            continue;
        }
    }

    return null; // 바닥까지 다 뒤졌는데 없으면 null 반환
}

// --- 3. 버튼 클릭 시 불도저 출발 ---
// --- 3. 버튼 클릭 시 불도저 출발 및 서버 전송 ---
aiButton.addEventListener("click", async () => {
    // 1. 에디터 본문 탐색
    const resultDiv = findLetterDiv(document);

    if (resultDiv) {
        const mailText = resultDiv.innerText;

        // 2. 사용자에게 처리 중임을 알림 (버튼 텍스트 변경 및 비활성화)
        const originalBtnText = aiButton.innerHTML;
        aiButton.innerHTML = "⏳ AI 교정 중...";
        aiButton.disabled = true;
        aiButton.style.backgroundColor = "#6c757d"; // 회색으로 변경

        try {
            // 3. 내 로컬 스프링 부트 서버로 데이터를 쏩니다!
            const response = await fetch("http://localhost:8080/api/mail/correct", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ text: mailText })
            });

            // 4. 서버로부터 응답이 성공적으로 오면
            if (response.ok) {
                const resultText = await response.text(); // 서버가 보낸 가짜 응답 텍스트

                // ✨ 핵심: 뽑아왔던 에디터 본문(letter) 영역의 HTML을 서버 응답으로 통째로 교체합니다!
                // (일반 텍스트의 엔터(\n)를 웹 에디터가 인식할 수 있는 줄바꿈(<br>) 태그로 바꿔서 넣습니다.)
                resultDiv.innerHTML = resultText.replace(/\n/g, "<br>");

                // 에디터 배경색을 살짝 깜빡이게 해서 바뀌었다는 시각적 효과 주기 (선택사항)
                const originalBg = resultDiv.style.backgroundColor;
                resultDiv.style.backgroundColor = "#e8f0fe"; // 연한 파란색
                setTimeout(() => {
                    resultDiv.style.backgroundColor = originalBg;
                }, 500);

            } else {
                alert("🚨 서버에서 에러가 발생했습니다. 상태 코드: " + response.status);
            }

        } catch (error) {
            alert("🚨 서버 통신 실패! 스프링 부트가 켜져 있는지 확인해 주세요.\n(에러: " + error.message + ")");
        } finally {
            // 5. 작업이 끝나면 버튼을 다시 원래대로 복구합니다.
            aiButton.innerHTML = originalBtnText;
            aiButton.disabled = false;
            aiButton.style.backgroundColor = "#0056b3";
        }

    } else {
        alert("🚨 본문(letter) 영역을 찾을 수 없습니다.");
    }
});