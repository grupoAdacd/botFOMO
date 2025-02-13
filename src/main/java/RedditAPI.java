import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.Map;

public class RedditAPI {

    private static final String REDDIT_BASE_URL = "https://www.reddit.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"; // Important for Reddit API requests

    private static final Map<String, String> SUBREDDIT_CATEGORIES = new HashMap<>();

    static {
        SUBREDDIT_CATEGORIES.put("economy", "economy");
        SUBREDDIT_CATEGORIES.put("politics", "politics");
        SUBREDDIT_CATEGORIES.put("cryptos", "Cryptos");
        SUBREDDIT_CATEGORIES.put("bitcoin", "Bitcoin");
        SUBREDDIT_CATEGORIES.put("cryptocurrencies", "CryptoCurrency");
    }

    public static void main(String[] args) throws Exception {
        RedditAPI redditAPI = new RedditAPI();

        System.out.println("Reddit Data for Different Categories:\n");

        for (Map.Entry<String, String> categoryEntry : SUBREDDIT_CATEGORIES.entrySet()) {
            String categoryName = categoryEntry.getKey();
            String subredditName = categoryEntry.getValue();

            System.out.println("Category: " + categoryName.toUpperCase() + " (r/" + subredditName + ")");
            JSONArray posts = redditAPI.fetchRedditPosts(subredditName);

            if (posts != null && posts.length() > 0) {
                System.out.println("""
                                
                                Latest Reddit Posts:
                          """);
                for (int i = 0; i < Math.min(5, posts.length()); i++) {
                    JSONObject post = posts.getJSONObject(i);
                    String title = post.getJSONObject("data").getString("title");
                    String author = post.getJSONObject("data").getString("author");
                    String postLink = REDDIT_BASE_URL + post.getJSONObject("data").getString("permalink");

                    System.out.println("   - Title: " + title);
                    System.out.println("     Author: " + author);
                    System.out.println("     Link: " + postLink);
                    System.out.println("""
                            
                            *---------------------------------*
                            """);
                }
            } else {
                System.out.println("  Could not retrieve Reddit posts for r/" + subredditName + " or no posts found.");
            }
            System.out.println("Next Category ->\n");
        }
    }

    public JSONArray fetchRedditPosts(String subreddit) throws Exception {
        String apiUrl = String.format("%s/r/%s/new.json", REDDIT_BASE_URL, subreddit);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", USER_AGENT)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        JSONObject jsonResponse = new JSONObject(responseBody);
        if (jsonResponse.has("data") && jsonResponse.getJSONObject("data").has("children")) {
            return jsonResponse.getJSONObject("data").getJSONArray("children");
        } else {
            System.err.println("Error fetching Reddit posts for r/" + subreddit + ": No 'data.children' found or other API error.");
            System.err.println("Raw response body for debugging:\n" + responseBody);
            return null;
        }
    }
}