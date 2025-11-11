import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class project3 {
    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter REG_TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final String _5K_ROSTER_FILE = "5k_RaceRoster";
    private static final String _10K_ROSTER_FILE = "10k_RaceRoster";
    private static final String HALF_ROSTER_FILE = "Half_RaceRoster";
    private static final String FULL_ROSTER_FILE = "Full_RaceRoster";

    // Simple per-file cached counts initialized at class load.
    public static int fiveK_count;
    public static int tenK_count;
    public static int half_count;
    public static int full_count;

    // Which field indices (0-based) should be compared to determine duplicates.
    // Default: compare firstName(0), lastName(1), gender(3), email(4), regTs(5)
    private static final int[] FIELDS_TO_COMPARE = {0, 1, 3, 4, 5};

    private static final String DELIM = "|";
    private static final String DELIM_REGEX = "\\|";
    
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

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter command: ");
        String input = sc.next().trim();
        while (!input.equalsIgnoreCase("exit")) {
            if (input.equalsIgnoreCase("register")) {
                new project3().registerRunner(sc);
            } else if (input.equalsIgnoreCase("display")) {
                new project3().displayRoster(sc);
            } else if (!input.equalsIgnoreCase("exit")) {
                System.out.println("Unknown command. Valid commands are: register, display, exit.");
            }
            System.out.print("Enter command: ");
            input = sc.next().trim();
        }
        sc.close();
    }

    private void registerRunner(Scanner scan) throws IOException {
        System.out.print("Enter first name :");
        String firstName = scan.next().trim();
        System.out.print("Enter last name :");
        String lastName = scan.next().trim();
        System.out.print("Enter date of birth (YYYYMMDD) :");
        String dobStr = scan.next().trim();
        System.out.print("Enter gender :");
        String gender = scan.next().trim();
        System.out.print("Enter email :");
        String email = scan.next().trim();
        System.out.print("Enter registration timestamp (YYYYMMDD or YYYYMMDDHHMMSS) :");
        String regTs = scan.next().trim();
        scan.nextLine();
        System.out.print("Enter Saturday race category (5k or 10k) :");
        String satRace = scan.nextLine().trim().toLowerCase();
        System.out.print("Enter Sunday race category (half or full) :");
        String sunRace = scan.nextLine().trim().toLowerCase();

        LocalDate dob = parseToLocalDate(dobStr);
        LocalDate regDate = parseToLocalDate(regTs);
        String SATURDAY_ROSTER = setRoster(satRace);
        String SUNDAY_ROSTER = setRoster(sunRace);

        int raceYear = regDate.getYear() + (regDate.getMonthValue() >= 6 ? 1 : 0);
        LocalDate tday = computeTDay(raceYear);
        LocalDate satRaceDate = tday.plusDays(2);
        LocalDate sunRaceDate = tday.plusDays(3);

        int satRaceAge = calculateAge(dob, satRaceDate);
        int sunRaceAge = calculateAge(dob, sunRaceDate);
        int ageOnRegister = calculateAge(dob, regDate);
        int satCost = !satRace.isEmpty() ? determineFee(satRace, determineRacePeriod(regDate)) : 0;
        int sunCost = !sunRace.isEmpty() ? determineFee(sunRace, determineRacePeriod(regDate)) : 0;
        double cost = (satCost != 0 && sunCost != 0) ? (satCost + sunCost) * 0.8 - (ageOnRegister > 64 ? 10 : 0)
                                                    : satCost + sunCost - (ageOnRegister > 64 ? 5 : 0);
        
        int satSeq = satRace.equals("5k") ? fiveK_count + 1 : tenK_count + 1;
        int sunSeq = sunRace.equals("half") ? half_count + 1 : full_count + 1;

        // Build single-line, delimited entries to make records resilient to file corruption
        String satEntry = !satRace.isEmpty() ? String.join(DELIM,
            Integer.toString(satSeq),
            firstName,
            lastName,
            Integer.toString(satRaceAge),
            gender,
            email,
            regTs,
            Double.toString(cost)) : null;

        String sunEntry = !sunRace.isEmpty() ? String.join(DELIM,
            Integer.toString(sunSeq),
            firstName,
            lastName,
            Integer.toString(sunRaceAge),
            gender,
            email,
            regTs,
            Double.toString(cost)) : null;
        if (!determineRacePeriod(regDate).equals("Registration Closed") && !determineRacePeriod(regDate).equals("Registration Not Open")) {
            if (satEntry != null) {
                if(isDuplicate(satEntry, true)){
                    System.out.println("Already registered for a saturday race.");
                } else if(fiveK_count + tenK_count < 100){
                    writeRecord(SATURDAY_ROSTER, satEntry);
                    if (satRace.equals("5k")) {
                        fiveK_count++;
                    } else {
                        tenK_count++;
                    }
                    System.out.println("Adding entry to " + SATURDAY_ROSTER);
                } else {
                    System.out.println("Saturday race is full. Cannot add entry.");
                }
            }
            if (sunEntry != null) {
                if(isDuplicate(sunEntry, false)){
                    System.out.println("Already registered for a sunday race.");
                } else if(half_count + full_count < 100){
                    writeRecord(SUNDAY_ROSTER, sunEntry);
                    if (sunRace.equals("half")) {
                        half_count++;
                    } else {
                        full_count++;
                    }
                    System.out.println("Adding entry to " + SUNDAY_ROSTER);
                } else {
                    System.out.println("Sunday race is full. Cannot add entry.");
                }
            }
        } else {
            System.out.println(determineRacePeriod(regDate));
        }
    }

    private void displayRoster(Scanner sc) {
        System.out.print("Enter roster to print :");
		String roster = sc.next().trim();

        String ROSTER_FILE = setRoster(roster);

        if (ROSTER_FILE != null) {
            RosterPrinter.printRoster(ROSTER_FILE);
        } else {
            System.out.println("Invalid roster type entered.");
        }
    }

    public static String determineRacePeriod(LocalDate d) {
		int raceYear = d.getYear() + (d.getMonthValue() >= 6 ? 1 : 0);

		LocalDate superEarly = LocalDate.of(raceYear - 1, Month.OCTOBER, 1);
		LocalDate early = LocalDate.of(raceYear - 1, Month.NOVEMBER, 1);
		LocalDate baseline = LocalDate.of(raceYear, Month.MARCH, 1);
		LocalDate late = LocalDate.of(raceYear, Month.APRIL, 2);
		LocalDate registrationEnd = LocalDate.of(raceYear, Month.MAY, 31);

		if(d.isBefore(superEarly)){
			return "Registration Not Open";
		}else if(d.isBefore(early)){
			return "Super Early";
		}else if(d.isBefore(baseline)){
			return "Early";
		}else if(d.isBefore(late)){
			return "Baseline";
		}else if(d.isBefore(registrationEnd) || d.isEqual(registrationEnd)){
			return "Late";
		} else {
            return "Registration Closed";
        }
	}

    public static LocalDate computeTDay(int year) {
		LocalDate firstOfMay = LocalDate.of(year, Month.MAY, 1);
		DayOfWeek dow = firstOfMay.getDayOfWeek();
		int daysToAdd = (DayOfWeek.SATURDAY.getValue() - dow.getValue() + 7) % 7;
		LocalDate firstSaturday = firstOfMay.plusDays(daysToAdd);
		return firstSaturday.minusDays(2);
	}

    public static int calculateAge(LocalDate dob, LocalDate onDate) {
		return Period.between(dob, onDate).getYears();
	}

    public static int determineFee(String cat, String per) {
        cat = cat.trim().toLowerCase();
        per = per.trim().toLowerCase();
        if (cat == null || per == null) return 0;

        Map<String, Integer> catMap = FEE_MAP.get(cat);
        if (catMap == null) return 0;
        return catMap.getOrDefault(per, 0);
    }

    public static String setRoster(String raceCat) {
        switch (raceCat.toLowerCase()) {
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

    // Static initializer: initialize the four counters by reading each file's last line.
    static {
        try {
            fiveK_count = readLastLineLeadingInt(_5K_ROSTER_FILE);
        } catch (Exception e) {
            fiveK_count = 0;
        }
        try {
            tenK_count = readLastLineLeadingInt(_10K_ROSTER_FILE);
        } catch (Exception e) {
            tenK_count = 0;
        }
        try {
            half_count = readLastLineLeadingInt(HALF_ROSTER_FILE);
        } catch (Exception e) {
            half_count = 0;
        }
        try {
            full_count = readLastLineLeadingInt(FULL_ROSTER_FILE);
        } catch (Exception e) {
            full_count = 0;
        }
    }

    /**
     * Read only the last logical line of the given file and return the leading integer token.
     * The file is expected to have delimited records whose first token is an integer sequence
     * or count. If the file does not exist or is empty, 0 is returned. If the last line's
     * leading token is not a parsable integer, a NumberFormatException is thrown.
     */
    public static int readLastLineLeadingInt(String filename) throws IOException {
        File f = new File(filename);
        if (!f.exists()) return 0;

        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(f, "r")) {
            long length = raf.length();
            if (length == 0) return 0;

            long pos = length - 1;
            // Skip trailing newlines/carriage returns
            while (pos >= 0) {
                raf.seek(pos);
                int b = raf.read();
                if (b == '\n' || b == '\r') { pos--; continue; }
                break;
            }

            if (pos < 0) return 0;

            // Read bytes backwards until previous newline or start of file
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            while (pos >= 0) {
                raf.seek(pos);
                int b = raf.read();
                if (b == '\n') break;
                baos.write(b);
                pos--;
            }

            byte[] rev = baos.toByteArray();
            // reverse to get correct order
            for (int i = 0, j = rev.length - 1; i < j; i++, j--) {
                byte t = rev[i]; rev[i] = rev[j]; rev[j] = t;
            }

            String lastLine = new String(rev, java.nio.charset.StandardCharsets.UTF_8).trim();
            if (lastLine.endsWith("\r")) lastLine = lastLine.substring(0, lastLine.length() - 1);
            if (lastLine.isEmpty()) return 0;

            // first token before the file delimiter
            String firstToken = lastLine.split(DELIM_REGEX, 2)[0].trim();
            return Integer.parseInt(firstToken);
        }
    }

    public static int writeRecord(String rosterFile, String entry) throws IOException {
        try (FileWriter fw = new FileWriter(rosterFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(entry);
        }
        return 0;
    }

    public static class RosterPrinter {
        private static final String DELIM_REGEX = "\\|";

        public static void printRoster(String rosterFile) {
            if (rosterFile == null) {
                System.out.println("No roster file specified.");
                return;
            }

            File f = new File(rosterFile);
            if (!f.exists()) {
                System.out.println("Roster file not found: " + rosterFile);
                return;
            }

            int count = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                // Print header row for table: Last, First, Age & Sex, Email, Reg Date, Cost
                System.out.printf("%-15s %-12s %-9s %-32s %-12s %8s%n", "Last", "First", "Age/Sex", "Email", "Reg Date", "Cost");
                System.out.println(new String(new char[90]).replace('\0', '-'));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] fields = line.split(DELIM_REGEX, -1);

                    String first = fields.length > 1 ? fields[1].trim() : "";
                    String last = fields.length > 2 ? fields[2].trim() : "";
                    String age = fields.length > 3 ? fields[3].trim() : "";
                    String email = fields.length > 5 ? fields[5].trim() : "";
                    String regTs = fields.length > 6 ? fields[6].trim() : "";
                    String amt = fields.length > 7 ? fields[7].trim() : "";

                    // Try to find a DOB-like field (yyyyMMdd). Prefer any 8-digit field that is not the regTs field.
                    String dobField = "";
                    for (int i = 0; i < fields.length; i++) {
                        String fld = fields[i].trim();
                        if (fld.matches("\\d{8}") && (regTs.isEmpty() || !fld.equals(regTs.substring(0, Math.min(8, regTs.length()))))) {
                            dobField = fld;
                            break;
                        }
                    }

                    // If we didn't find an explicit DOB, try to approximate using regTs and age (regDate - age years)
                    if (dobField.isEmpty() && !regTs.isEmpty() && !age.isEmpty()) {
                        try {
                            LocalDate regDate = parseToLocalDate(regTs);
                            int a = Integer.parseInt(age);
                            LocalDate approxDob = regDate.minusYears(a);
                            dobField = String.format("%04d%02d%02d", approxDob.getYear(), approxDob.getMonthValue(), approxDob.getDayOfMonth());
                        } catch (Exception e) {
                            // leave dobField empty if parse fails
                        }
                    }

                    // We will show Age & Sex together
                    String sex = fields.length > 4 ? fields[4].trim() : "";
                    String ageSex = (age.isEmpty() ? "" : age) + (sex.isEmpty() ? "" : " " + sex);

                    String regDateDisplay = regTs;
                    if (!regTs.isEmpty()) {
                        try {
                            LocalDate rd = parseToLocalDate(regTs);
                            regDateDisplay = rd.toString();
                        } catch (Exception e) {
                            // leave regTs as-is if parsing fails
                        }
                    }

                    // Format amount as currency with two decimals
                    String costDisplay = amt == null || amt.isEmpty() ? "0.00" : amt;
                    try {
                        double v = Double.parseDouble(costDisplay);
                        costDisplay = String.format("$%.2f", v);
                    } catch (NumberFormatException e) {
                        // keep as-is
                    }

                    // Print table row
                    System.out.printf("%-15s %-12s %-9s %-32s %-12s %8s%n",
                        last, first, ageSex, email, regDateDisplay, costDisplay);
                    count++;
                }

                // Footer line like sample
                System.out.println();
                System.out.println("There are " + count + " runners registered for this race.");

            } catch (IOException e) {
                System.out.println("Error reading roster file: " + e.getMessage());
            }
        }
    }
}
