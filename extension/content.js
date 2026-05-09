console.log("🚀 [v1.0 배포 준비 완료] AI 패널 및 팝업 기능 로딩");

// --- 1. 버튼 패널 만들기 ---
const aiPanel = document.createElement("div");
aiPanel.style.position = "fixed";
aiPanel.style.bottom = "30px";
aiPanel.style.right = "30px";
aiPanel.style.display = "flex";
aiPanel.style.gap = "10px";
aiPanel.style.zIndex = "9999";
document.body.appendChild(aiPanel);

function createAiButton(text, bgColor) {
    const btn = document.createElement("button");
    btn.innerHTML = text;
    btn.style.padding = "12px 18px";
    btn.style.backgroundColor = bgColor;
    btn.style.color = "white";
    btn.style.border = "none";
    btn.style.borderRadius = "30px";
    btn.style.boxShadow = "0 4px 8px rgba(0,0,0,0.2)";
    btn.style.fontSize = "14px";
    btn.style.fontWeight = "bold";
    btn.style.cursor = "pointer";
    btn.style.transition = "all 0.2s";

    btn.onmouseover = () => btn.style.transform = "scale(1.05)";
    btn.onmouseout = () => btn.style.transform = "scale(1.0)";
    return btn;
}

const btnFormal = createAiButton("👔 정중하게", "#0056b3");
const btnConcise = createAiButton("⚡ 간결하게", "#28a745");
const btnEnglish = createAiButton("🔤 영문번역", "#6f42c1");

aiPanel.appendChild(btnFormal);
aiPanel.appendChild(btnConcise);
aiPanel.appendChild(btnEnglish);

// 불도저 탐색 함수
function findLetterDiv(doc) {
    let letter = doc.querySelector('div[name="letter"]');
    if (letter) return letter;
    let iframes = doc.querySelectorAll("iframe");
    for (let i = 0; i < iframes.length; i++) {
        try {
            let iframeDoc = iframes[i].contentDocument || iframes[i].contentWindow.document;
            if (iframeDoc) {
                let foundInside = findLetterDiv(iframeDoc);
                if (foundInside) return foundInside;
            }
        } catch (error) { continue; }
    }
    return null;
}

// --- ✨ 2. 미리보기 팝업창 생성 함수 (NEW!) ---
function showPreviewModal(newHtml, applyCallback) {
    // 뒷배경 어둡게 (Overlay)
    const overlay = document.createElement("div");
    overlay.style.position = "fixed";
    overlay.style.top = "0";
    overlay.style.left = "0";
    overlay.style.width = "100%";
    overlay.style.height = "100%";
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)";
    overlay.style.zIndex = "10000";
    overlay.style.display = "flex";
    overlay.style.justifyContent = "center";
    overlay.style.alignItems = "center";

    // 팝업 본체 (Modal Box)
    const modal = document.createElement("div");
    modal.style.backgroundColor = "white";
    modal.style.padding = "25px";
    modal.style.borderRadius = "12px";
    modal.style.width = "600px";
    modal.style.maxWidth = "90%";
    modal.style.boxShadow = "0 10px 30px rgba(0,0,0,0.3)";
    modal.style.fontFamily = "'Malgun Gothic', sans-serif";

    // 팝업 제목
    const title = document.createElement("h2");
    title.innerText = "🤖 AI 교정 미리보기";
    title.style.marginTop = "0";
    title.style.marginBottom = "15px";
    title.style.fontSize = "20px";
    title.style.color = "#333";

    // 교정된 내용을 보여줄 박스 (서식 적용됨)
    const previewBox = document.createElement("div");
    previewBox.innerHTML = newHtml;
    previewBox.style.border = "1px solid #ddd";
    previewBox.style.padding = "15px";
    previewBox.style.borderRadius = "8px";
    previewBox.style.minHeight = "150px";
    previewBox.style.maxHeight = "400px";
    previewBox.style.overflowY = "auto";
    previewBox.style.backgroundColor = "#f9f9f9";
    previewBox.style.marginBottom = "20px";
    previewBox.style.fontSize = "14px";
    previewBox.style.lineHeight = "1.6";

    // 하단 버튼 영역
    const btnContainer = document.createElement("div");
    btnContainer.style.display = "flex";
    btnContainer.style.justifyContent = "flex-end";
    btnContainer.style.gap = "10px";

    // 취소 버튼
    const cancelBtn = document.createElement("button");
    cancelBtn.innerText = "취소";
    cancelBtn.style.padding = "10px 20px";
    cancelBtn.style.border = "1px solid #ccc";
    cancelBtn.style.backgroundColor = "white";
    cancelBtn.style.cursor = "pointer";
    cancelBtn.style.borderRadius = "6px";
    cancelBtn.onclick = () => document.body.removeChild(overlay); // 팝업 닫기

    // 적용 버튼
    const applyBtn = document.createElement("button");
    applyBtn.innerText = "✨ 본문에 적용하기";
    applyBtn.style.padding = "10px 20px";
    applyBtn.style.border = "none";
    applyBtn.style.backgroundColor = "#0056b3";
    applyBtn.style.color = "white";
    applyBtn.style.cursor = "pointer";
    applyBtn.style.borderRadius = "6px";
    applyBtn.style.fontWeight = "bold";
    applyBtn.onclick = () => {
        applyCallback(); // 에디터에 덮어쓰는 함수 실행
        document.body.removeChild(overlay); // 팝업 닫기
    };

    btnContainer.appendChild(cancelBtn);
    btnContainer.appendChild(applyBtn);

    modal.appendChild(title);
    modal.appendChild(previewBox);
    modal.appendChild(btnContainer);
    overlay.appendChild(modal);

    document.body.appendChild(overlay);
}

// --- 3. 통신 로직 ---
async function requestAiCorrection(mode, buttonElement) {
    const resultDiv = findLetterDiv(document);
    if (!resultDiv) {
        alert("🚨 본문(letter) 영역을 찾을 수 없습니다.");
        return;
    }

    const mailHtml = resultDiv.innerHTML;
    const originalBtnText = buttonElement.innerHTML;
    const originalBtnBg = buttonElement.style.backgroundColor;

    buttonElement.innerHTML = "⏳ 처리 중...";
    buttonElement.disabled = true;
    buttonElement.style.backgroundColor = "#6c757d";

    try {
        const response = await fetch("http://localhost:8080/api/mail/correct", {
        //const response = await fetch("http://172.19.18.4:8080/api/mail/correct", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ text: mailHtml, mode: mode })
        });

        if (response.ok) {
            const resultText = await response.text();

            // ✨ 핵심: 바로 덮어쓰지 않고, 미리보기 팝업을 먼저 띄웁니다!
            // 사용자가 [적용하기]를 누르면 실행될 콜백 함수를 같이 넘겨줍니다.
            showPreviewModal(resultText, () => {
                resultDiv.innerHTML = resultText;

                const originalBg = resultDiv.style.backgroundColor;
                resultDiv.style.backgroundColor = "#e8f0fe";
                setTimeout(() => resultDiv.style.backgroundColor = originalBg, 500);
            });

        } else {
            alert("🚨 서버 에러: " + response.status);
        }
    } catch (error) {
        alert("🚨 서버 통신 실패! 스프링 부트 확인 부탁드립니다.\n" + error.message);
    } finally {
        buttonElement.innerHTML = originalBtnText;
        buttonElement.disabled = false;
        buttonElement.style.backgroundColor = originalBtnBg;
    }
}

// --- 4. 이벤트 연결 ---
btnFormal.addEventListener("click", () => requestAiCorrection("formal", btnFormal));
btnConcise.addEventListener("click", () => requestAiCorrection("concise", btnConcise));
btnEnglish.addEventListener("click", () => requestAiCorrection("english", btnEnglish));