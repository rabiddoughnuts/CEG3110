import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class req22 {
	private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final String _5K_ROSTER_FILE = "5k_RaceRoster";
    private static final String _10K_ROSTER_FILE = "10k_RaceRoster";
    private static final String HALF_ROSTER_FILE = "Half_RaceRoster";
    private static final String FULL_ROSTER_FILE = "Full_RaceRoster";

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
		System.out.print("Enter registration timestamp (YYYYMMDDHHMMSS) :");
		String regTs = sc.next().trim();
        System.out.println("Enter race category :");
		String raceCategory = sc.next().trim();
		sc.close();

		LocalDate dob = LocalDate.parse(dobStr, DOB_FMT);
		LocalDate regDate = LocalDate.parse(regTs.substring(0, 8), DOB_FMT);

		int raceYear = regDate.getYear() + (regDate.getMonthValue() >= 6 ? 1 : 0);
		LocalDate tday = req11.computeTDay(raceYear);

        //  TODO this will be adjusted later to determine age on the day of the different races
		LocalDate raceDayShort = tday.plusDays(2);

		int ageOnRaceDay = req12.calculateAge(dob, raceDayShort);

		String entry = firstName + "\n" + lastName + "\n" + ageOnRaceDay + "\n" + gender + "\n" + email + "\n" + regTs;

        String ROSTER_FILE;
        switch (raceCategory.toLowerCase()) {
            case "5k":
                ROSTER_FILE = _5K_ROSTER_FILE;
                break;
            case "10k":
                ROSTER_FILE = _10K_ROSTER_FILE;
                break;
            case "half":
                ROSTER_FILE = HALF_ROSTER_FILE;
                break;
            case "full":
                ROSTER_FILE = FULL_ROSTER_FILE;
                break;
            default:
                System.out.println("Invalid race category.");
                return;
        }

		if (!isDuplicate(entry)) {
			try (FileWriter fw = new FileWriter(ROSTER_FILE, true);
				 PrintWriter pw = new PrintWriter(fw)) {
				pw.println(firstName);
				pw.println(lastName);
				pw.println(ageOnRaceDay);
				pw.println(gender);
				pw.println(email);
				pw.println(regTs);
			}
			System.out.println(firstName);
			System.out.println(lastName);
			System.out.println(ageOnRaceDay);
			System.out.println(gender);
			System.out.println(email);
			System.out.println(regTs);
			System.out.println(raceCategory);
		} else {
			System.out.println("Duplicate entry. Not added to roster.");
		}
	}

	private static boolean isDuplicate(String entry) throws IOException {
        File[] files = {new File(_5K_ROSTER_FILE), new File(_10K_ROSTER_FILE), new File(HALF_ROSTER_FILE), new File(FULL_ROSTER_FILE)};

        for (File file : files) {
            if (!file.exists()) {
                continue;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder existingEntry = new StringBuilder();
                String line;
                int lineCount = 0;

                while ((line = br.readLine()) != null) {
                    existingEntry.append(line);
                    lineCount++;

                    if (lineCount == 6) {
                        if (existingEntry.toString().equals(entry.replace("\n", ""))) {
                            return true;
                        }
                        existingEntry.setLength(0);
                        lineCount = 0;
                    }
                }
            }
        }

		return false;
	}
}

