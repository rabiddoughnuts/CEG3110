import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class req1 {
	private static final DateTimeFormatter INPUT_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

	public static void main(String[] args) {
		String input;
        System.out.print("Enter date (YYYYMMDD): ");
        Scanner sc = new Scanner(System.in);
        input = sc.next().trim();
        sc.close();

		LocalDate date;
		date = LocalDate.parse(input, INPUT_FMT);

		String period = determineRacePeriod(date);
		System.out.println(period);
	}

	public static String determineRacePeriod(LocalDate d) {
		int raceYear = d.getYear() + (d.getMonthValue() >= 6 ? 1 : 0);

		LocalDate superEarly = LocalDate.of(raceYear - 1, Month.OCTOBER, 1);
		LocalDate early = LocalDate.of(raceYear - 1, Month.NOVEMBER, 1);
		LocalDate baseline = LocalDate.of(raceYear, Month.MARCH, 1);
		LocalDate late = LocalDate.of(raceYear, Month.APRIL, 2);
		LocalDate registrationEnd = LocalDate.of(raceYear, Month.MAY, 31);

		if(d.isBefore(superEarly) || d.isAfter(registrationEnd)){
			return "Registration Not Open";
		}else if(!d.isBefore(superEarly) && d.isBefore(early)){
			return "Super Early";
		}else if(!d.isBefore(early) && d.isBefore(baseline)){
			return "Early";
		}else if(!d.isBefore(baseline) && d.isBefore(late)){
			return "Baseline";
		}else {
			return "Late";
		}
	}

	public static LocalDate computeTDay(int year) {
		LocalDate firstOfMay = LocalDate.of(year, Month.MAY, 1);
		DayOfWeek dow = firstOfMay.getDayOfWeek();
		int daysToAdd = (DayOfWeek.SATURDAY.getValue() - dow.getValue() + 7) % 7;
		LocalDate firstSaturday = firstOfMay.plusDays(daysToAdd);
		return firstSaturday.minusDays(2);
	}
}