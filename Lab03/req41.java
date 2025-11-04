import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class req41 {
    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter REG_TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final String _5K_ROSTER_FILE = "5k_RaceRoster";
    private static final String _10K_ROSTER_FILE = "10k_RaceRoster";
    private static final String HALF_ROSTER_FILE = "Half_RaceRoster";
    private static final String FULL_ROSTER_FILE = "Full_RaceRoster";
    
    // Which field indices (0-based) should be compared to determine duplicates.
    // Default: compare firstName(0), lastName(1), gender(3), email(4), regTs(5)
    private static final int[] FIELDS_TO_COMPARE = {0, 1, 3, 4, 5};

    private static final String DELIM = "|";
    private static final String DELIM_REGEX = "\\|";

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);

        System.out.print("Enter first name :");
		String firstName = sc.next().trim();
		System.out.print("Enter last name :");
		String lastName = sc.next().trim();
		System.out.print("Enter date of birth (YYYYMMDD) :");
		String dobStr = sc.next().trim();
		System.out.print("Enter gender :");
		String gender = sc.next().trim();
		System.out.print("Enter email :");
		String email = sc.next().trim();
        System.out.print("Enter registration timestamp (YYYYMMDD or YYYYMMDDHHMMSS) :");
        String regTs = sc.next().trim();
        System.out.print("Enter Saturday race category (5k or 10k) :");
        String satRace = sc.next().trim().toLowerCase();
        System.out.print("Enter Sunday race category (half or full) :");
        String sunRace = sc.next().trim().toLowerCase();
		sc.close();

		LocalDate dob = parseToLocalDate(dobStr);
        LocalDate regDate = parseToLocalDate(regTs);
        String SATURDAY_ROSTER = setRoster(satRace);
        String SUNDAY_ROSTER = setRoster(sunRace);

		int raceYear = regDate.getYear() + (regDate.getMonthValue() >= 6 ? 1 : 0);
		LocalDate tday = req11.computeTDay(raceYear);
		LocalDate satRaceDate = tday.plusDays(2);
        LocalDate sunRaceDate = tday.plusDays(3);

		int satRaceAge = req12.calculateAge(dob, satRaceDate);
        int sunRaceAge = req12.calculateAge(dob, sunRaceDate);
        int ageOnRegister = req12.calculateAge(dob, regDate);
        int satCost = !satRace.isEmpty() ? req31.determineFee(satRace, req11.determineRacePeriod(regDate)) : 0;
        int sunCost = !sunRace.isEmpty() ? req31.determineFee(sunRace, req11.determineRacePeriod(regDate)) : 0;
        double cost = (satCost != 0 && sunCost != 0) ? (satCost + sunCost) * 0.8 - (ageOnRegister > 64 ? 10 : 0)
                                                    : satCost + sunCost - (ageOnRegister > 64 ? 5 : 0);

        // Build single-line, delimited entries to make records resilient to file corruption
        String satEntry = !satRace.isEmpty() ? String.join(DELIM,
            firstName,
            lastName,
            Integer.toString(satRaceAge),
            gender,
            email,
            regTs,
            Double.toString(cost)) : null;

        String sunEntry = !sunRace.isEmpty() ? String.join(DELIM,
            firstName,
            lastName,
            Integer.toString(sunRaceAge),
            gender,
            email,
            regTs,
            Double.toString(cost)) : null;

		if (satEntry != null && !isDuplicate(satEntry, true)) {
            writeRecord(SATURDAY_ROSTER, satEntry);
            System.out.println("Adding entry to " + SATURDAY_ROSTER);
		} else {
			System.out.println("Already registered for a saturday race.");
		}
        if (sunEntry != null && !isDuplicate(sunEntry, false)) {
            writeRecord(SUNDAY_ROSTER, sunEntry);
            System.out.println("Adding entry to " + SUNDAY_ROSTER);
        } else {
            System.out.println("Already registered for a sunday race.");
        }
	}

    public static String setRoster(String raceCat) {
        switch (raceCat) {
            case "5k":
                return _5K_ROSTER_FILE;
            case "10k":
                return _10K_ROSTER_FILE;
            case "half":
                return HALF_ROSTER_FILE;
            case "full":
                return FULL_ROSTER_FILE;
            default:
                return null;
        }
    }

    /**
     * Parse an input string that may be either a date (yyyyMMdd) or a
     * timestamp (yyyyMMddHHmmss) and return the corresponding LocalDate.
     * If parsing both patterns fails but the input contains at least 8 digits,
     * the first 8 digits are used as the date portion.
     *
     * Throws DateTimeParseException if no valid date can be extracted.
     */
    public static LocalDate parseToLocalDate(String input) {
        String s = input == null ? "" : input.trim();
        try {
            return LocalDate.parse(s, DOB_FMT);
        } catch (DateTimeParseException e) {
            // Fall through and try timestamp
        }

        // Try yyyyMMddHHmmss as LocalDateTime then convert
        try {
            LocalDateTime dt = LocalDateTime.parse(s, REG_TS_FMT);
            return dt.toLocalDate();
        } catch (DateTimeParseException e) {
            // Fall through to heuristic
        }

        String digits = s.replaceAll("\\D", "");
        if (digits.length() >= 8) {
            String datePart = digits.substring(0, 8);
            return LocalDate.parse(datePart, DOB_FMT);
        }

        throw new DateTimeParseException("Unparseable date: " + input, input, 0);
    }

    public static int writeRecord(String rosterFile, String entry) throws IOException {
        try (FileWriter fw = new FileWriter(rosterFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(entry);
        }
        return 0;
    }
    
    /*
     * Newest version of isDuplicate
     */
	private static boolean isDuplicate(String entry, boolean sat) throws IOException {
        String[] entryFields = entry.split(DELIM_REGEX, -1);

        // Build the target values using the configured indices.
        String[] target = new String[FIELDS_TO_COMPARE.length];
        for (int i = 0; i < FIELDS_TO_COMPARE.length; i++) {
            target[i] = entryFields[FIELDS_TO_COMPARE[i]].trim();
        }

        File[] files;
        if (sat) {
            files = new File[]{new File(_5K_ROSTER_FILE), new File(_10K_ROSTER_FILE)};
        } else {
            files = new File[]{new File(HALF_ROSTER_FILE), new File(FULL_ROSTER_FILE)};
        }

        // Process each roster file individually.
        for (File file : files) {
            if (!file.exists()) {   continue; }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = br.readLine()) != null) {
                    String[] recordFields = line.split(DELIM_REGEX, -1);
                    boolean matches = true;
                    for (int i = 0; i < FIELDS_TO_COMPARE.length; i++) {
                        int fieldIndex = FIELDS_TO_COMPARE[i];
                        if (fieldIndex >= recordFields.length) { 
                            matches = false;
                            break;
                        }
                        if (!recordFields[fieldIndex].trim().equals(target[i])) { 
                            matches = false;
                            break;
                        }
                    }
                    if (matches) { return true; }
                }
            }
        }
        return false;
	}
}