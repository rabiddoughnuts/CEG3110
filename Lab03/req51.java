import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;

public class req51 {
    private static final String _5K_ROSTER_FILE = "5k_RaceRoster";
    private static final String _10K_ROSTER_FILE = "10k_RaceRoster";
    private static final String HALF_ROSTER_FILE = "Half_RaceRoster";
    private static final String FULL_ROSTER_FILE = "Full_RaceRoster";

    public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);

        System.out.print("Enter roster to print :");
		String roster = sc.next().trim();
        sc.close();

        String ROSTER_FILE = setRoster(roster);

        if (ROSTER_FILE != null) {
            RosterPrinter.printRoster(ROSTER_FILE);
        } else {
            System.out.println("Invalid roster type entered.");
        }
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
                            LocalDate regDate = req42.parseToLocalDate(regTs);
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

                    // Format registration date using existing parse helper (req42.parseToLocalDate)
                    String regDateDisplay = regTs;
                    if (!regTs.isEmpty()) {
                        try {
                            LocalDate rd = req42.parseToLocalDate(regTs);
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
}