package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikiPhilosophy {

    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf = new WikiFetcher();
    final static String domain = "https://en.wikipedia.org";

    /**
     * Tests a conjecture about Wikipedia and Philosophy.
     *
     * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
     *
     * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 100);
        System.out.println("Visited Links");
        visited.forEach(System.out::println);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws IOException {
        visited.add(source);

        if (limit < 0) {
            System.out.println("failure limit reached");
            return;
        }

        if (destination.equalsIgnoreCase(source)) {
            System.out.println("success");
            return;
        }

        WikiFetcher fetcher = new WikiFetcher();
        Elements paragraphs = fetcher.fetchWikipedia(source);
        for (Element paragraph : paragraphs) {
            Elements links = paragraph.select("a");
            for (Element link : links) {
                if (isValidLink(paragraph, link)) {
                    testConjecture(destination, domain + link.attr("href"), limit - 1);
                    return;
                }
            }
        }

        System.out.println("failure");
    }

    public static boolean isValidLink(Element aParagraph, Element aLink) {
        if (isInItalics(aParagraph, aLink)) {
            return false;
        }

        if (isInsideParenthesis(aParagraph, aLink)) {
            return false;
        }

        return isWikiLink(aLink);
    }

    public static boolean isInItalics(Element root, Element current) {
        Element parent = current.parent();
        while (parent != root) {
            if (parent.nodeName().equalsIgnoreCase("i") || parent.nodeName().equalsIgnoreCase("em")) {
                return true;
            }
            parent = parent.parent();
        }
        return false;
    }

    private static boolean isInsideParenthesis(Element aParagraph, Element aLink) {
        String href = aLink.attr("href");
        String paragraph = aParagraph.toString();
        Deque<String> parenthesis = new ArrayDeque<>();
        String text = "";
        for (int i = 0; i < paragraph.length(); ++i) {
            if (paragraph.charAt(i) == '(') {
                if (parenthesis.isEmpty()) {
                    text = "";
                }
                parenthesis.add("(");
                continue;
            }
            if (paragraph.charAt(i) == ')') {
                if (text.contains(href)) {
                    return true;
                }
                parenthesis.pop();
                continue;
            }
            text += paragraph.charAt(i);
        }

        return false;
    }

    private static boolean isWikiLink(Element aLink) {
        String url = aLink.attr("href");
        return (url.startsWith("/wiki") && !visited.contains(domain + url));
    }
}
