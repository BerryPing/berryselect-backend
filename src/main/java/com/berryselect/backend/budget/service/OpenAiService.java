package com.berryselect.backend.budget.service;

import com.berryselect.backend.budget.domain.MonthlyCategorySummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    @Value("${openai.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    /**
     * OpenAI를 통한 소비 패턴 분석
     */
    public String generateConsumptionAnalysis(String yearMonth, List<MonthlyCategorySummary> summaries) {
        if (apiKey.isEmpty()) {
            log.warn("OpenAI API 키가 설정되지 않았습니다. 기본 분석을 반환합니다.");
            return generateDefaultAnalysis(yearMonth, summaries);
        }

        try {
            String prompt = buildAnalysisPrompt(yearMonth, summaries);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-3.5-turbo",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 400,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    "https://api.openai.com/v1/chat/completions", request, Map.class);

            return extractContentFromResponse(response);

        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage(), e);
            return generateDefaultAnalysis(yearMonth, summaries);
        }
    }

    /**
     * OpenAI용 프롬프트 생성
     */
    private String buildAnalysisPrompt(String yearMonth, List<MonthlyCategorySummary> summaries) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("사용자의 %s 월별 소비 패턴을 분석해주세요.\n\n", yearMonth));
        prompt.append("카테고리별 지출 내역:\n");

        Long totalSpent = 0L;
        Long totalSaved = 0L;

        for (MonthlyCategorySummary summary : summaries) {
            prompt.append(String.format("- 카테고리 %d: %,d원 지출, %,d원 절약, %d건 거래\n",
                    summary.getCategoryId(), summary.getAmountSpent(),
                    summary.getAmountSaved(), summary.getTxCount()));
            totalSpent += summary.getAmountSpent();
            totalSaved += summary.getAmountSaved();
        }

        prompt.append(String.format("\n총 지출: %,d원\n", totalSpent));
        prompt.append(String.format("총 절약: %,d원\n\n", totalSaved));

        prompt.append("다음 내용을 포함하여 분석해주세요:\n");
        prompt.append("1. 카테고리별 지출 패턴 (어떤 카테고리에 가장 많이/적게 지출했는지)\n");
        prompt.append("2. 절약 성과 평가\n");
        prompt.append("3. 개선할 수 있는 절약 방법 제안\n\n");
        prompt.append("친근하고 도움이 되는 톤으로 250자 이내로 작성해주세요.");
        prompt.append("단, 카테고리 1은 '카페', 카테고리 2는 '편의점', 카테고리 3은 '교통', 카테고리 4는 '쇼핑', 카테고리 5는 '음식', 카테고리 6은 '기타'로 변경해서 작성해주세요.");

        return prompt.toString();
    }

    /**
     * OpenAI 응답에서 내용 추출
     */
    @SuppressWarnings("unchecked")
    private String extractContentFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                String content = (String) message.get("content");
                return content != null ? content.trim() : "AI 분석 결과를 가져오는데 실패했습니다.";
            }
        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 실패: {}", e.getMessage());
        }

        return "AI 분석 결과를 처리하는데 실패했습니다.";
    }

    /**
     * OpenAI 실패시 기본 분석 생성
     */
    private String generateDefaultAnalysis(String yearMonth, List<MonthlyCategorySummary> summaries) {
        if (summaries.isEmpty()) {
            return String.format("%s에는 거래 내역이 없습니다.", yearMonth);
        }

        Long totalSpent = summaries.stream().mapToLong(MonthlyCategorySummary::getAmountSpent).sum();
        Long totalSaved = summaries.stream().mapToLong(MonthlyCategorySummary::getAmountSaved).sum();
        MonthlyCategorySummary topCategory = summaries.get(0);

        return String.format(
                "%s 소비 분석 결과입니다.\n\n" +
                        "총 지출액: %,d원\n" +
                        "총 절약액: %,d원\n" +
                        "주요 지출: 카테고리 %d\n\n" +
                        "이번 달은 카테고리 %d에서 가장 많이 지출하셨네요. " +
                        "절약률을 높이기 위해 해당 카테고리의 혜택을 더 적극 활용해보세요!",
                yearMonth, totalSpent, totalSaved, topCategory.getCategoryId(), topCategory.getCategoryId()
        );
    }
}
