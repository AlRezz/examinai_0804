package com.examinai.app.integration.ai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * Parses model output that begins with score lines and Markdown sections per {@link AiDraftAssessmentService}.
 */
final class AiDraftAssessmentResponseParser {

	private static final Pattern QUALITY = Pattern.compile("(?i)^\\s*Quality\\s*:\\s*([1-5])\\s*$");

	private static final Pattern READABILITY = Pattern.compile("(?i)^\\s*Readability\\s*:\\s*([1-5])\\s*$");

	private static final Pattern CORRECTNESS = Pattern.compile("(?i)^\\s*Correctness\\s*:\\s*([1-5])\\s*$");

	private static final Pattern TWO_BLOCKS = Pattern.compile(
			"(?is)##\\s*Feedback on the code\\s*(.*?)##\\s*Suggestions to improve\\s*(.*)");

	private AiDraftAssessmentResponseParser() {
	}

	static AiDraftAssessmentResult parse(String raw) {
		if (!StringUtils.hasText(raw)) {
			return new AiDraftAssessmentResult(raw == null ? "" : raw, null, null, null, "");
		}
		String trimmed = raw.trim();
		String[] lines = trimmed.split("\\R", -1);
		Integer quality = null;
		Integer readability = null;
		Integer correctness = null;
		int i = 0;
		while (i < lines.length && lines[i].isBlank()) {
			i++;
		}
		int maxScoreScan = Math.min(lines.length, i + 16);
		int scoresParsed = 0;
		while (i < maxScoreScan && scoresParsed < 3) {
			String line = lines[i];
			if (line.isBlank()) {
				i++;
				continue;
			}
			Integer v = tryScore(line, QUALITY);
			if (v != null) {
				quality = v;
				scoresParsed++;
				i++;
				continue;
			}
			v = tryScore(line, READABILITY);
			if (v != null) {
				readability = v;
				scoresParsed++;
				i++;
				continue;
			}
			v = tryScore(line, CORRECTNESS);
			if (v != null) {
				correctness = v;
				scoresParsed++;
				i++;
				continue;
			}
			break;
		}
		while (i < lines.length && lines[i].isBlank()) {
			i++;
		}
		String remainder = String.join("\n", java.util.Arrays.copyOfRange(lines, i, lines.length)).trim();
		String narrative = extractNarrative(remainder);
		if (!StringUtils.hasText(narrative) && StringUtils.hasText(remainder)) {
			narrative = remainder;
		}
		return new AiDraftAssessmentResult(trimmed, quality, readability, correctness, narrative);
	}

	private static Integer tryScore(String line, Pattern p) {
		Matcher m = p.matcher(line);
		return m.matches() ? Integer.valueOf(m.group(1)) : null;
	}

	private static String extractNarrative(String remainder) {
		if (!StringUtils.hasText(remainder)) {
			return "";
		}
		Matcher m = TWO_BLOCKS.matcher(remainder);
		if (m.find()) {
			String feedback = m.group(1) == null ? "" : m.group(1).trim();
			String suggestions = m.group(2) == null ? "" : m.group(2).trim();
			if (!StringUtils.hasText(feedback) && !StringUtils.hasText(suggestions)) {
				return "";
			}
			if (!StringUtils.hasText(feedback)) {
				return suggestions;
			}
			if (!StringUtils.hasText(suggestions)) {
				return feedback;
			}
			return feedback + "\n\n" + suggestions;
		}
		return "";
	}
}
