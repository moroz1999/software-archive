package com.archive.backend.importer.infra;

import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/** The text a page of a source shows, as the import takes it. */
final class HtmlText {

    private static final Pattern SPACES = Pattern.compile("[ \\t\\u00A0]+");

    private HtmlText() {}

    /**
     * The text of HTML with its line breaks: every line trimmed, its spaces joined, at most one empty line in a row.
     */
    static String lines(Element html) {
        StringBuilder raw = new StringBuilder();
        collect(html, raw);
        StringBuilder text = new StringBuilder();
        boolean emptyBefore = false;
        for (String line : raw.toString().split("\n", -1)) {
            String clean = SPACES.matcher(line).replaceAll(" ").trim();
            if (clean.isEmpty()) {
                emptyBefore = text.length() > 0;
                continue;
            }
            if (text.length() > 0) {
                text.append(emptyBefore ? "\n\n" : "\n");
            }
            text.append(clean);
            emptyBefore = false;
        }
        return text.toString();
    }

    private static void collect(Node node, StringBuilder text) {
        for (Node child : node.childNodes()) {
            if (child instanceof TextNode textNode) {
                text.append(textNode.getWholeText().replaceAll("[\\r\\n]+", " "));
            } else if (child instanceof Element element) {
                if (element.normalName().equals("br")) {
                    text.append('\n');
                } else {
                    boolean block = element.isBlock();
                    if (block) {
                        text.append('\n');
                    }
                    collect(element, text);
                    if (block) {
                        text.append('\n');
                    }
                }
            }
        }
    }

    /** One line of text with its spaces joined, non-breaking ones too, and trimmed. */
    static String line(Element html) {
        return SPACES.matcher(html.text()).replaceAll(" ").trim();
    }
}
