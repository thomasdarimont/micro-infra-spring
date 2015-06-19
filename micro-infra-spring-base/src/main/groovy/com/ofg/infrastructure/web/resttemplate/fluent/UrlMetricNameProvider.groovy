package com.ofg.infrastructure.web.resttemplate.fluent

import groovy.transform.CompileStatic

import java.util.regex.Pattern

import static com.google.common.base.Preconditions.checkArgument
import static org.springframework.util.StringUtils.hasText
import static org.springframework.util.StringUtils.trimLeadingCharacter
import static org.springframework.util.StringUtils.trimTrailingCharacter

@CompileStatic
class UrlMetricNameProvider {

    private LinkedHashMap<Pattern, String> pathPatternReplacements = [:]

    UrlMetricNameProvider(LinkedHashMap<String, String> pathPatternReplacements = [:]) {
        pathPatternReplacements.each { pattern, replacement ->
            checkArgument(hasText(replacement), 'Replacements must not be null nor empty nor all-whitespace')
            this.pathPatternReplacements.put(Pattern.compile(pattern), replacement)
        }
    }

    String metricName(URI uri) {
        String pathPart = getPathPart(uri)
        int port = (uri.port > 0) ? uri.port : 80
        String host = uri.host?.replaceAll(/[\[\]]/, "")?.replaceAll(/\./,"_")?.replaceAll(":+", "_")
        return trimDots("${host}.${port}.${pathPart}")
    }

    private String getPathPart(URI uri) {
        def originalPathTrimmed = uri.path.replaceFirst('^/', '').replaceFirst('/$', '')
        Map.Entry<Pattern, String> matchingPatternReplacement = pathPatternReplacements.find { pattern, replacement ->
            pattern.matcher(originalPathTrimmed).matches()
        }
        def path = matchingPatternReplacement?.getValue() ?: elideSubsequentPathParts(originalPathTrimmed)
        return fixSpecialCharacters(path)
    }

    private String elideSubsequentPathParts(String path) {
        int firstSlash = path.indexOf('/');
        boolean pathIsMultipart = firstSlash != -1;
        if (pathIsMultipart) {
            String firstPart = path.substring(0, firstSlash);
            String subsequentParts = path.substring(firstSlash);
            String subsequentPartsElided = subsequentParts.replaceAll("[^/]+", "_");
            path = firstPart + subsequentPartsElided;
        }
        return path;
    }

    private static String fixSpecialCharacters(String value) {
        final String result = value
                .replaceAll(/\./, "_")
                .replaceAll("/", ".")
        return trimDots(result)
    }

    private static String trimDots(String str) {
        return trimTrailingCharacter(
                trimLeadingCharacter(str, '.' as Character), '.' as Character)
    }
}
