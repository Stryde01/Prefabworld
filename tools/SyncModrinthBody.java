import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Patches the Modrinth project body from a markdown file.
 * Usage: java SyncModrinthBody.java <projectId> <tokenFile> <markdownFile>
 * The token file is the user-level Gradle properties holding modrinthToken=...
 */
public class SyncModrinthBody {
    public static void main(String[] args) throws Exception {
        String projectId = args[0];
        String token = readToken(Path.of(args[1]));
        String body = Files.readString(Path.of(args[2]), StandardCharsets.UTF_8);

        String payload = "{\"body\":" + jsonString(body) + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.modrinth.com/v2/project/" + projectId))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .header("User-Agent", "PrefabWorld-Release/2.0.0")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("HTTP " + response.statusCode());
        if (!response.body().isEmpty()) {
            System.out.println(response.body());
        }
    }

    private static String readToken(Path gradleProperties) throws Exception {
        Matcher matcher = Pattern.compile("^modrinthToken=(.*)$", Pattern.MULTILINE)
                .matcher(Files.readString(gradleProperties));
        if (!matcher.find()) {
            throw new IllegalStateException("modrinthToken not found in " + gradleProperties);
        }
        return matcher.group(1).trim();
    }

    private static String jsonString(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
