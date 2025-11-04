import java.time.LocalDate;

public class TestCalc {
    public static void main(String[] args) {
        // args: dob(yyyyMMdd) reg(yyyyMMdd) satRace sunRace
        if (args.length < 4) {
            System.err.println("Usage: java TestCalc dob reg satRace sunRace");
            System.exit(2);
        }
        String dobStr = args[0];
        String regStr = args[1];
        String satRace = args[2];
        String sunRace = args[3];
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate dob = LocalDate.parse(dobStr, f);
        LocalDate regDate = LocalDate.parse(regStr, f);
        int satRaceAge = req12.calculateAge(dob, req11.computeTDay(regDate.getYear() + (regDate.getMonthValue() >= 6 ? 1 : 0)).plusDays(2));
        int sunRaceAge = req12.calculateAge(dob, req11.computeTDay(regDate.getYear() + (regDate.getMonthValue() >= 6 ? 1 : 0)).plusDays(3));
        int ageOnRegister = req12.calculateAge(dob, regDate);
        int satCost = !satRace.isEmpty() ? req31.determineFee(satRace, req11.determineRacePeriod(regDate)) : 0;
        int sunCost = !sunRace.isEmpty() ? req31.determineFee(sunRace, req11.determineRacePeriod(regDate)) : 0;
        double cost = (satCost != 0 && sunCost != 0) ? (satCost + sunCost) * 0.8 - (ageOnRegister > 64 ? 10 : 0)
                                                    : satCost + sunCost - (ageOnRegister > 64 ? 5 : 0);
        System.out.println((int)Math.round(cost));
    }
}
