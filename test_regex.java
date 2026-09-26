import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test_regex {
    public static void main(String[] args) {
        String plainText = "[Level 1] Slime Abnormal: 20 (-549.02)  ".replaceAll("§.", "").trim();
        Matcher m = Pattern.compile("(?i)\\[Level \\d+\\] (.+?):\\s*([0-9,.]+)\\s*\\(-([0-9,.]+)\\)").matcher(plainText);
        if (m.find()) {
            System.out.println("Match found!");
            System.out.println("G1: " + m.group(1));
            System.out.println("G2: " + m.group(2));
            System.out.println("G3: " + m.group(3));
        } else {
            System.out.println("No match.");
        }
    }
}
