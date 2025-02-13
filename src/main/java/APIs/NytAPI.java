package APIs;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;

public class NytAPI {
    private static final String API_KEY = "ZhkyIwT4ekIfpWU3BWVQgAvfiEJgijc5";
    private static final String BASE_URL = "https://api.nytimes.com/svc/news/v3/content/all/all.json";

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?api-key=" + API_KEY))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        System.out.println("Raw JSON Response from NYT API:");
        System.out.println(responseBody);
        System.out.println("\n*----- Processed Articles Info -----*\n");

        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray articles = jsonResponse.getJSONArray("results");

        System.out.println("Number of articles fetched: " + articles.length());

        String[] bitcoinKeywords = {"bitcoin", "economic", "politic", "politics", "battlefield", "crisis", "pact", "war", "btc", "crypto", "cryptocurrency", "blockchain", "digital currency", "ethereum"};
        String[] proBitcoinKeywords = {"bullish", "positive", "optimistic", "increase", "rise", "growth", "adoption", "innovative", "decentralized", "investment", "to the moon"};
        String[] counterBitcoinKeywords = {"bearish", "negative", "pessimistic", "decrease", "fall", "decline", "crash", "bubble", "regulation", "scam", "fraud", "risk", "volatile"};


        for (int i = 0; i < articles.length(); i++) {
            JSONObject articleJson = articles.getJSONObject(i);
            String title = articleJson.getString("title");
            String abstractArticle = articleJson.getString("abstract");
            String articleText = title + " " + abstractArticle;
            String author = articleJson.getString("byline");
            String articleUrl = articleJson.getString("url");

            String theme = determineTheme(articleText, bitcoinKeywords);
            String sentiment = analyzeSentiment(articleText, proBitcoinKeywords, counterBitcoinKeywords);

            System.out.println("Article Title: " + title);
            System.out.println("Author: " + author);
            System.out.println("Article Link: " + articleUrl);
            System.out.println("Theme: " + theme);
            System.out.println("Sentiment: " + sentiment);
            System.out.println("\n*-------------------------------------------*\n");
        }
    }

    private static String determineTheme(String articleText, String[] keywords) {
        articleText = articleText.toLowerCase();
        for (String keyword : keywords) {
            if (articleText.contains(keyword)) {
                return "Bitcoin-related";
            }
        }
        return "General News";
    }

    private static String analyzeSentiment(String articleText, String[] proKeywords, String[] counterKeywords) {
        articleText = articleText.toLowerCase();

        boolean foundKeyword = false;

        for (String keyword : proKeywords) {
            if (articleText.contains(keyword)) {
                foundKeyword = true;
                break;
            }
        }
        if (foundKeyword) {
            return "Bitcoin-related Sentiment";
        }

        if (!foundKeyword) {
            for (String keyword : counterKeywords) {
                if (articleText.contains(keyword)) {
                    foundKeyword = true;
                    break;
                }
            }
        }
        if (foundKeyword) {
            return "Bitcoin-related Sentiment";
        }

        return "Neutral Sentiment";
    }
}
