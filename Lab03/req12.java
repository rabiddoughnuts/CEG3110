import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class req12 {
	private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

	public static void main(String[] args) {
	Scanner sc = new Scanner(System.in);
	LocalDate dob = LocalDate.parse(sc.next().trim(), DOB_FMT);
	sc.close();

	LocalDate today = LocalDate.now();
	int raceYear = today.getYear() + ((today.getMonthValue() >= 10 && today.getDayOfMonth() >= 1) ? 1 : 0);
		sc.close();

	    LocalDate tday = req11.computeTDay(raceYear);
		LocalDate raceDayShort = tday.plusDays(2);
		LocalDate raceDayLong = tday.plusDays(3);

		int ageShort = calculateAge(dob, raceDayShort);
		int ageLong = calculateAge(dob, raceDayLong);

		System.out.println(ageShort);
		System.out.println(ageLong);
	}

	public static int calculateAge(LocalDate dob, LocalDate onDate) {
		return Period.between(dob, onDate).getYears();
	}
}