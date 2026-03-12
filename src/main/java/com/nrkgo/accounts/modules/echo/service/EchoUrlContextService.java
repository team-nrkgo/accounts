package com.nrkgo.accounts.modules.echo.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EchoUrlContextService {

    public String fetchAndConvertToMarkdown(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .get();

        // 1. Remove unwanted elements and css blocks
        doc.select("script, style, link, meta, noscript, header, footer, nav, aside, iframe, svg, form, button, img, " +
                "video, audio, source, track, canvas, map, area, object, embed, picture, input, select, textarea, label, fieldset, legend")
                .remove();
        doc.select("[style]").removeAttr("style");
        doc.select("[class]").removeAttr("class");
        doc.select("[id]").removeAttr("id");

        // 2. Extract the main article/body
        Element contentElement = doc.selectFirst("article, main, [role=main]");
        if (contentElement == null) {
            contentElement = doc.body();
        }

        // 3. Convert HTML to Markdown format to optimize token usage
        return convertToMarkdown(contentElement);
    }

    private String convertToMarkdown(Element element) {
        StringBuilder sb = new StringBuilder();
        convertNode(element, sb);

        // Post-process cleanup (remove excessive newlines)
        String markdown = sb.toString().replaceAll("\\n{3,}", "\n\n").trim();
        return markdown;
    }

    private void convertNode(Node node, StringBuilder sb) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text();
            if (!text.isBlank()) {
                sb.append(text).append(" ");
            }
        } else if (node instanceof Element) {
            Element el = (Element) node;
            String tag = el.tagName().toLowerCase();

            switch (tag) {
                case "h1":
                    sb.append("\n\n# ");
                    break;
                case "h2":
                    sb.append("\n\n## ");
                    break;
                case "h3":
                    sb.append("\n\n### ");
                    break;
                case "h4":
                case "h5":
                case "h6":
                    sb.append("\n\n#### ");
                    break;
                case "p":
                case "div":
                case "article":
                case "section":
                case "blockquote":
                    sb.append("\n\n");
                    break;
                case "br":
                    sb.append("\n");
                    break;
                case "li":
                    sb.append("\n- ");
                    break;
                case "strong":
                case "b":
                    sb.append("**");
                    break;
                case "em":
                case "i":
                    sb.append("*");
                    break;
                case "a":
                    sb.append("[");
                    break;
                case "img":
                    // skip images to save token
                    return;
            }

            for (Node child : el.childNodes()) {
                convertNode(child, sb);
            }

            switch (tag) {
                case "h1":
                case "h2":
                case "h3":
                case "h4":
                case "h5":
                case "h6":
                case "p":
                case "div":
                case "article":
                case "section":
                case "blockquote":
                case "li":
                    sb.append("\n");
                    break;
                case "strong":
                case "b":
                    sb.append("** ");
                    break;
                case "em":
                case "i":
                    sb.append("* ");
                    break;
                case "a":
                    String href = el.attr("href");
                    if (href != null && !href.isEmpty()) {
                        if (href.startsWith("/")) {
                            sb.append("](").append(el.baseUri() != null ? el.baseUri() : "").append(href).append(") ");
                        } else {
                            sb.append("](").append(href).append(") ");
                        }
                    } else {
                        sb.append("] ");
                    }
                    break;
            }
        }
    }
}
