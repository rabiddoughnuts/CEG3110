import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class req21 {
	private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final String ROSTER_FILE = "RaceRoster";

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
		sc.close();

		LocalDate dob = LocalDate.parse(dobStr, DOB_FMT);
		LocalDate regDate = LocalDate.parse(regTs.substring(0, 8), DOB_FMT);

		int raceYear = regDate.getYear() + (regDate.getMonthValue() >= 6 ? 1 : 0);
		LocalDate tday = req11.computeTDay(raceYear);
		LocalDate raceDayShort = tday.plusDays(2);

		int ageOnRaceDay = req12.calculateAge(dob, raceDayShort);

		String entry = firstName + "\n" + lastName + "\n" + ageOnRaceDay + "\n" + gender + "\n" + email + "\n" + regTs;

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
		} else {
			System.out.println("Duplicate entry. Not added to roster.");
		}

		System.out.println(firstName);
		System.out.println(lastName);
		System.out.println(ageOnRaceDay);
		System.out.println(gender);
		System.out.println(email);
		System.out.println(regTs);
	}

	private static boolean isDuplicate(String entry) throws IOException {
		File file = new File(ROSTER_FILE);
		if (!file.exists()) {
			return false;
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

		return false;
	}
}
