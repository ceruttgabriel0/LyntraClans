package com.lyntra.lyntraclans.util;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Filtro simples de palavrao por lista de substrings (config.yml). Normaliza acentos e caixa
 * antes de comparar, pra pegar variacoes obvias tipo "PALAVRÃO" vs "palavrao". Nao e um sistema
 * sofisticado (sem leetspeak/fuzzy match), so a primeira linha de defesa pro nome/tag/descricao
 * do cla, que aparece publicamente pra todo mundo no servidor.
 */
public final class ProfanityFilter {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private ProfanityFilter() {
    }

    public static boolean containsBannedWord(String text, List<String> bannedWords) {
        if (text == null || text.isBlank() || bannedWords.isEmpty()) {
            return false;
        }
        String normalized = normalize(text);
        for (String banned : bannedWords) {
            String normalizedBanned = normalize(banned);
            if (!normalizedBanned.isBlank() && normalized.contains(normalizedBanned)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String text) {
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("").toLowerCase(Locale.ROOT);
    }
}
