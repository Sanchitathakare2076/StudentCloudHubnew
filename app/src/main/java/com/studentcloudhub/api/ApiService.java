package com.studentcloudhub.api;

import android.os.Build;
import android.text.Html;
import android.util.Log;

import com.studentcloudhub.model.Question;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiService {

    private static final String TAG = "ApiService";
    private static final String BASE_URL = "https://opentdb.com/api.php";

    public interface QuestionsCallback {
        void onSuccess(List<Question> questions);
        void onError(String errorMessage);
    }

    public static void fetchQuestions(String subject, int amount, QuestionsCallback callback) {
        ApiClient client = new ApiClient();
        int category = getCategoryForSubject(subject);

        String url = BASE_URL + "?amount=" + amount + "&type=multiple";
        if (category > 0) {
            url += "&category=" + category;
        }

        Log.d(TAG, "Fetching questions from URL: " + url);

        client.fetchUrl(url, new ApiClient.ApiResponseListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int responseCode = jsonObject.optInt("response_code", -1);

                    JSONArray results = jsonObject.optJSONArray("results");

                    if (results == null || results.length() == 0) {
                        Log.w(TAG, "API returned empty response (response_code: " + responseCode + "), falling back to curated questions.");
                        callback.onSuccess(getFallbackQuestions(subject, amount));
                        return;
                    }

                    List<Question> questions = new ArrayList<>();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject obj = results.getJSONObject(i);

                        String rawQuestion = obj.optString("question", "");
                        String rawCorrect = obj.optString("correct_answer", "");
                        JSONArray rawIncorrect = obj.optJSONArray("incorrect_answers");

                        String cleanQuestion = decodeHtml(rawQuestion);
                        String cleanCorrect = decodeHtml(rawCorrect);

                        List<String> options = new ArrayList<>();
                        options.add(cleanCorrect);

                        if (rawIncorrect != null) {
                            for (int j = 0; j < rawIncorrect.length(); j++) {
                                options.add(decodeHtml(rawIncorrect.getString(j)));
                            }
                        }

                        // Shuffle options while keeping track of correct answer
                        Collections.shuffle(options);
                        int correctIndex = options.indexOf(cleanCorrect);
                        if (correctIndex == -1) {
                            correctIndex = 0;
                        }

                        Question question = new Question(cleanQuestion, options, correctIndex);
                        questions.add(question);
                    }

                    if (questions.isEmpty()) {
                        callback.onSuccess(getFallbackQuestions(subject, amount));
                    } else {
                        callback.onSuccess(questions);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing API response", e);
                    // Fallback to offline questions on parse error
                    callback.onSuccess(getFallbackQuestions(subject, amount));
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "API call failed: " + errorMessage);
                // Return fallback questions if internet or API fails
                callback.onSuccess(getFallbackQuestions(subject, amount));
            }
        });
    }

    private static int getCategoryForSubject(String subject) {
        if (subject == null) return 18; // Default Computer Science
        String s = subject.toLowerCase().trim();

        if (s.contains("math")) return 19;
        if (s.contains("science") || s.contains("physics") || s.contains("chemistry")) return 17;
        if (s.contains("general") || s.contains("gk")) return 9;
        if (s.contains("history")) return 23;
        if (s.contains("geography")) return 22;
        if (s.contains("computer") || s.contains("java") || s.contains("code") || s.contains("it")) return 18;

        return 18; // Default to CS
    }

    @SuppressWarnings("deprecation")
    private static String decodeHtml(String text) {
        if (text == null) return "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                return Html.fromHtml(text).toString();
            }
        } catch (Exception e) {
            return text.replace("&quot;", "\"")
                    .replace("&#039;", "'")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">");
        }
    }

    public static List<Question> getFallbackQuestions(String subject, int requestedAmount) {
        List<Question> list = new ArrayList<>();

        if (subject != null && subject.toLowerCase().contains("math")) {
            list.add(createQuestion("What is the square root of 144?", "12", "10", "14", "16"));
            list.add(createQuestion("What is 15 multiplied by 8?", "120", "110", "130", "100"));
            list.add(createQuestion("What is the value of Pi rounded to 2 decimal places?", "3.14", "3.16", "3.12", "3.18"));
            list.add(createQuestion("Solve for x: 2x + 6 = 14", "4", "3", "5", "6"));
            list.add(createQuestion("What is 7 cubed (7^3)?", "343", "243", "49", "512"));
            list.add(createQuestion("What is the sum of angles in a triangle?", "180 degrees", "90 degrees", "360 degrees", "270 degrees"));
            list.add(createQuestion("What is 50% of 250?", "125", "100", "150", "175"));
            list.add(createQuestion("What is the smallest prime number?", "2", "1", "3", "0"));
            list.add(createQuestion("What is 2^10?", "1024", "512", "2048", "256"));
            list.add(createQuestion("What is 100 divided by 4?", "25", "20", "30", "40"));
        } else {
            // General / CS default questions
            list.add(createQuestion("What does CPU stand for in computer systems?", "Central Processing Unit", "Central Process Unit", "Computer Personal Unit", "Central Processor Unit"));
            list.add(createQuestion("Which programming language is primarily used for Android app development in native Java?", "Java", "Python", "C#", "Ruby"));
            list.add(createQuestion("What is the main database used in Google Cloud Firebase for document storage?", "Firestore", "MongoDB", "MySQL", "PostgreSQL"));
            list.add(createQuestion("What does RAM stand for?", "Random Access Memory", "Read Access Memory", "Rapid Action Memory", "Run Access Memory"));
            list.add(createQuestion("Which data structure operates on a First In First Out (FIFO) basis?", "Queue", "Stack", "Tree", "Graph"));
            list.add(createQuestion("What is the default port for HTTP traffic?", "80", "443", "8080", "21"));
            list.add(createQuestion("Which of these is NOT an Object-Oriented Programming pillar?", "Compilation", "Encapsulation", "Inheritance", "Polymorphism"));
            list.add(createQuestion("What does HTML stand for?", "HyperText Markup Language", "HighText Machine Language", "HyperTransfer Mark Language", "Home Tool Markup Language"));
            list.add(createQuestion("Which protocol is secure for web browsing?", "HTTPS", "HTTP", "FTP", "SMTP"));
            list.add(createQuestion("In Java, which keyword is used to prevent method overriding?", "final", "static", "private", "abstract"));
        }

        Collections.shuffle(list);
        if (list.size() > requestedAmount) {
            return list.subList(0, requestedAmount);
        }
        return list;
    }

    private static Question createQuestion(String text, String correct, String wrong1, String wrong2, String wrong3) {
        List<String> options = new ArrayList<>();
        options.add(correct);
        options.add(wrong1);
        options.add(wrong2);
        options.add(wrong3);
        Collections.shuffle(options);
        int correctIndex = options.indexOf(correct);
        return new Question(text, options, correctIndex);
    }
}
