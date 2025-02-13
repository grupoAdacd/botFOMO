import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BinanceAPI {

    private static final String BASE_URL = "https://api.binance.com";
    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        BinanceAPI binanceAPI = new BinanceAPI();

        System.out.println("Choose data to fetch:");
        System.out.println("1. Bitcoin Price Fluctuation Today");
        System.out.println("2. Bitcoin Price Data for a Time Range");
        System.out.print("Enter your choice (1 or 2): ");
        String choice = scanner.nextLine();

        String symbol = "BTCUSDT";

        if (choice.equals("1")) {
            System.out.println("Searching todays Bitcoin fluctuation..."); // Message 1: Start searching
            JSONObject fluctuationData = binanceAPI.fetchTodaysPriceFluctuation(symbol);
            if (fluctuationData != null) {
                System.out.println("Done!");
                System.out.println("\nBitcoin Price Fluctuation Today (" + symbol + ")\n:");
                System.out.println("  Price Change Percentage (24hr): " + fluctuationData.getString("priceChangePercent") + "%");
                System.out.println("  High Price (24hr): " + fluctuationData.getString("highPrice"));
                System.out.println("  Low Price (24hr): " + fluctuationData.getString("lowPrice"));
                System.out.println("  Current Price: " + fluctuationData.getString("lastPrice"));
            } else {
                System.out.println("Could not fetch today's price fluctuation.");
            }
        } else if (choice.equals("2")) {
            System.out.print("Enter time interval (e.g., 1m, 5m, 1h, 1d - see Binance API docs): ");
            String interval = scanner.nextLine();
            System.out.print("Enter start date and time (YYYY-MM-DD HH:mm, e.g., 2023-01-01 00:00): ");
            String startDateTimeStr = scanner.nextLine();
            System.out.print("Enter end date and time (YYYY-MM-DD HH:mm, e.g., 2023-01-02 00:00): ");
            String endDateTimeStr = scanner.nextLine();
            System.out.println("--- Debug Input Strings ---");
            System.out.println("Start Date/Time String: \"" + startDateTimeStr + "\"");
            System.out.println("End Date/Time String: \"" + endDateTimeStr + "\"");
            System.out.println("--- End Debug Input Strings ---");

            try {
                LocalDateTime startLocalDateTime = LocalDateTime.parse(startDateTimeStr, INPUT_DATE_FORMAT);
                LocalDateTime endLocalDateTime = LocalDateTime.parse(endDateTimeStr, INPUT_DATE_FORMAT);

                long startTimeMillis = startLocalDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
                long endTimeMillis = endLocalDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();

                JSONArray timeRangeData = binanceAPI.fetchPriceDataByTimeRange(symbol, interval, startTimeMillis, endTimeMillis);

                if (timeRangeData != null && timeRangeData.length() > 0) {
                    System.out.println("\nBitcoin Price Data for Time Range (" + symbol + ", Interval: " + interval + "):");
                    for (int i = 0; i < timeRangeData.length(); i++) {
                        JSONArray candle = timeRangeData.getJSONArray(i);
                        //Candle format: [Open time, Open, High, Low, Close, Volume, Close time, Quote asset volume, Number of trades, Taker buy base asset volume, Taker buy quote asset volume, Ignore]
                        System.out.println("  Open Time (UTC): " + LocalDateTime.ofEpochSecond(candle.getLong(0) / 1000, 0, ZoneOffset.UTC).format(INPUT_DATE_FORMAT)); // Open time (UTC datetime)
                        System.out.println("  Open Price: " + candle.getString(1));
                        System.out.println("  Close Price: " + candle.getString(4));
                        System.out.println("  Volume: " + candle.getString(5));
                        System.out.println("---");
                    }
                } else {
                    System.out.println("Could not fetch price data for the specified time range.");
                }

            } catch (DateTimeParseException e) {
                System.err.println("Invalid date/time format. Please use YYYY-MM-DD HH:mm format.");
                System.err.println("Error details: " + e.getMessage()); // Print detailed error message
            }

        } else {
            System.out.println("Invalid choice.");
        }

        scanner.close();
    }

    public JSONObject fetchTodaysPriceFluctuation(String symbol) throws Exception {
        String apiUrl = BASE_URL + "/api/v3/ticker/24hr?symbol=" + symbol;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        return new JSONObject(responseBody); // Parse the JSON response
    }

    public JSONArray fetchPriceDataByTimeRange(String symbol, String interval, long startTime, long endTime) throws Exception {
        String apiUrl = String.format("%s/api/v3/klines?symbol=%s&interval=%s&startTime=%d&endTime=%d&limit=1000", BASE_URL, symbol, interval, startTime, endTime); // Limit to 1000 candles
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        if (responseBody.startsWith("[")) { // Check if response is a JSON array (for klines endpoint)
            return new JSONArray(responseBody); // Parse as JSONArray for klines (candlestick data)
        } else {
            System.err.println("Unexpected response format for time range data. Raw response:\n" + responseBody);
            return null;
        }
    }
}