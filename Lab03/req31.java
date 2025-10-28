import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class req31 {
    private static final DateTimeFormatter INPUT_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Map<String, Map<String, Integer>> FEE_MAP = new HashMap<>();
    static {
        FEE_MAP.put("5k", Map.of(
                "super early", 30,
                "early", 40,
                "baseline", 50,
                "late", 64
        ));
        FEE_MAP.put("10k", Map.of(
                "super early", 50,
                "early", 55,
                "baseline", 70,
                "late", 89
        ));
        FEE_MAP.put("half", Map.of(
                "super early", 65,
                "early", 70,
                "baseline", 85,
                "late", 99
        ));
        FEE_MAP.put("full", Map.of(
                "super early", 75,
                "early", 80,
                "baseline", 95,
                "late", 109
        ));
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter date of registration (YYYYMMDD): ");
        LocalDate dor = LocalDate.parse(sc.next().trim(), INPUT_FMT);
        System.out.print("Enter race category: ");
        String raceCategory = sc.next().trim().toLowerCase();
        sc.close();

        String period = req11.determineRacePeriod(dor).trim().toLowerCase();
        int fee = determineFee(raceCategory, period);
        System.out.println("Registration period: " + period + ", Fee: $" + fee);

    }

    public static int determineFee(String cat, String per) {
        cat = cat.trim().toLowerCase();
        per = per.trim().toLowerCase();
        if (cat == null || per == null) return 0;

        Map<String, Integer> catMap = FEE_MAP.get(cat);
        if (catMap == null) return 0;
        return catMap.getOrDefault(per, 0);
    }
}
