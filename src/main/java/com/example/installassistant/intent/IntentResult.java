package com.example.installassistant.intent;

import java.util.Map;

/**
 * 意图识别结果
 */
public record IntentResult(
        IntentType type,
        double confidence,
        Map<String, String> parameters,
        String originalInput
) {
    public static IntentResult chitchat(String input) {
        return new IntentResult(IntentType.CHITCHAT, 1.0, Map.of(), input);
    }
}
