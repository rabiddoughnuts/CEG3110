import java.util.Scanner;

public class Yahtzee {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Input what function to test, 0 to exit: ");
        int choice = sc.nextInt();
        System.out.println("Input the 5 dice values (1-6): ");
        int[] dice = new int[5];
        for (int i = 0; i < 5; i++) {
            dice[i] = sc.nextInt();
        }
        while (choice != 0) {
            switch (choice) {
                case 1:
                    System.out.println("Score for Ones: " + scoreOnes(dice));
                    break;
                case 2:
                    System.out.println("Score for Twos: " + scoreTwos(dice));
                    break;
                case 3:
                    System.out.println("Score for Threes: " + scoreThrees(dice));
                    break;
                case 4:
                    System.out.println("Score for Fours: " + scoreFours(dice));
                    break;
                case 5:
                    System.out.println("Score for Fives: " + scoreFives(dice));
                    break;
                case 6:
                    System.out.println("Score for Sixes: " + scoreSixes(dice));
                    break;
                case 7:
                    System.out.println("Score for Three of a Kind: " + scoreThreeOfAKind(dice));
                    break;
                case 8:
                    System.out.println("Score for Four of a Kind: " + scoreFourOfAKind(dice));
                    break;
                case 9:
                    System.out.println("Score for Full House: " + scoreFullHouse(dice));
                    break;
                case 10:
                    System.out.println("Score for Small Straight: " + scoreSmallStraight(dice));
                    break;
                case 11:
                    System.out.println("Score for Large Straight: " + scoreLargeStraight(dice));
                    break;
                case 12:
                    System.out.println("Score for Yahtzee: " + scoreYahtzee(dice));
                    break;
                case 13:
                    System.out.println("Score for Chance: " + scoreChance(dice));
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
            System.out.println("Input what function to test, 0 to exit: ");
            choice = sc.nextInt();
            System.out.println("Input the 5 dice values (1-6): ");
            for (int i = 0; i < 5; i++) {
                dice[i] = sc.nextInt();
            }
        }
    }

    public static int scoreOnes(int[] dice) {
        int score = 0;
        for (int die : dice) {
            if (die == 1) {
                score += 1;
            }
        }
        return score;
    }

    public static int scoreTwos(int[] dice) {
        int score = 0;
        for (int die : dice) {
            if (die == 2) {
                score += 2;
            }
        }
        return score;
    }

    public static int scoreThrees(int[] dice) {
        int score = 0;
        for (int die : dice) {
            if (die == 3) {
                score += 3;
            }
        }
        return score;
    }

    public static int scoreFours(int[] dice) {
        int score = 0;
        for (int die : dice) {
            if (die == 4) {
                score += 4;
            }
        }
        return score;
    }

    public static int scoreFives(int[] dice) {
        int score = 0;
        for (int die : dice) {
            if (die == 5) {
                score += 5;
            }
        }
        return score;
    }

    public static int scoreSixes(int[] dice) {
        int score = 0;
        for (int die : dice) {
            if (die == 6) {
                score += 6;
            }
        }
        return score;
    }

    public static int scoreThreeOfAKind(int[] dice) {
        int[] counts = new int[7];
        for (int die : dice) {
            counts[die]++;
        }
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 3) {
                int total = 0;
                for (int die : dice) {
                    total += die;
                }
                return total;
            }
        }
        return 0;
    }

    public static int scoreFourOfAKind(int[] dice) {
        int[] counts = new int[7];
        for (int die : dice) {
            counts[die]++;
        }
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 4) {
                int total = 0;
                for (int die : dice) {
                    total += die;
                }
                return total;
            }
        }
        return 0;
    }

    public static int scoreFullHouse(int[] dice) {
        int[] counts = new int[7];
        for (int die : dice) {
            counts[die]++;
        }
        boolean hasThree = false;
        boolean hasTwo = false;
        for (int count : counts) {
            if (count == 3) {
                hasThree = true;
            } else if (count == 2) {
                hasTwo = true;
            }
        }
        return (hasThree && hasTwo) ? 25 : 0;
    }

    public static int scoreSmallStraight(int[] dice) {
        boolean[] seen = new boolean[7];
        for (int die : dice) {
            seen[die] = true;
        }
        if ((seen[1] && seen[2] && seen[3] && seen[4]) ||
            (seen[2] && seen[3] && seen[4] && seen[5]) ||
            (seen[3] && seen[4] && seen[5] && seen[6])) {
            return 30;
        }
        return 0;
    }

    public static int scoreLargeStraight(int[] dice) {
        boolean[] seen = new boolean[7];
        for (int die : dice) {
            seen[die] = true;
        }
        if ((seen[1] && seen[2] && seen[3] && seen[4] && seen[5]) ||
            (seen[2] && seen[3] && seen[4] && seen[5] && seen[6])) {
            return 40;
        }
        return 0;
    }

    public static int scoreYahtzee(int[] dice) {
        int firstDie = dice[0];
        for (int die : dice) {
            if (die != firstDie) {
                return 0;
            }
        }
        return 50;
    }

    public static int scoreChance(int[] dice) {
        int total = 0;
        for (int die : dice) {
            total += die;
        }
        return total;
    }
}
