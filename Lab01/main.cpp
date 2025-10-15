// main.cpp
// This file simulates rolling dice, analyzes the results for fairness and independence, and prints detailed statistics.
#include <iostream>
#include <thread>
#include <chrono>
#include <vector>
#include <iomanip>
#include <random>
#include <algorithm>
#include <tuple>
#include <future>
#include <cmath>

#include "DiceRoller.h"

using namespace std;

double chiSquareDist(int observed[], int totalRolls) {
// Checks if each die face (1-6) appears about as often as expected (fairness test).
    double expected = totalRolls / 6.0;
    double chiSquared = 0.0;
    for(int i = 0; i < 6; ++i) {
        double diff = observed[i] - expected;
        chiSquared += (diff * diff) / expected;
    }
    return chiSquared;
}

// Helper to count ways to get each sum for n dice
std::vector<int> diceSumWays(int numDice) {
// Calculates how many ways you can get each possible sum when rolling numDice dice.
    int minSum = numDice;
    int maxSum = numDice * 6;
    std::vector<int> ways(maxSum + 1, 0);
    // Dynamic programming: ways[s][d] = ways to get sum s with d dice
    std::vector<std::vector<int>> dp(numDice + 1, std::vector<int>(maxSum + 1, 0));
    dp[0][0] = 1;
    for (int d = 1; d <= numDice; ++d) {
        for (int s = 0; s <= maxSum; ++s) {
            for (int face = 1; face <= 6; ++face) {
                if (s - face >= 0)
                    dp[d][s] += dp[d-1][s-face];
            }
        }
    }
    for (int s = minSum; s <= maxSum; ++s) {
        ways[s] = dp[numDice][s];
    }
    return ways;
}

// Chi-squared test for dice sums (non-uniform expected)
double chiSquareSum(const std::vector<int>& observed, int numDice, int numRolls) {
// Checks if the distribution of dice sums matches what you'd expect from random dice.
    int minSum = numDice;
    int maxSum = numDice * 6;
    std::vector<double> expectedCounts(maxSum + 1, 0.0);
    std::vector<int> ways = diceSumWays(numDice);
    int totalWays = 0;
    for (int s = minSum; s <= maxSum; ++s) totalWays += ways[s];
    for (int s = minSum; s <= maxSum; ++s) {
        expectedCounts[s] = static_cast<double>(ways[s]) / totalWays * numRolls;
    }
    double chiSquared = 0.0;
    for (int s = minSum; s <= maxSum; ++s) {
        double expected = expectedCounts[s];
        double obs = observed[s];
        if (expected > 0) {
            double diff = obs - expected;
            chiSquared += (diff * diff) / expected;
        }
    }
    return chiSquared;
}

double chiSquareInd(const vector<vector<int>>& observed, int totalTransitions) {
// Tests if the transitions between die faces (from one roll to the next) are independent.
    vector<int> rowTotals(6, 0);
    vector<int> colTotals(6, 0);
    int grandTotal = 0;
    for (int i = 0; i < 6; ++i) {
        for (int j = 0; j < 6; ++j) {
            rowTotals[i] += observed[i][j];
            colTotals[j] += observed[i][j];
            grandTotal += observed[i][j];
        }
    }

    double chiSquared = 0.0;
    for (int i = 0; i < 6; ++i) {
        for (int j = 0; j < 6; ++j) {
            double expected_ij = (double)rowTotals[i] * colTotals[j] / grandTotal;
            if (expected_ij > 0) {
                double diff = observed[i][j] - expected_ij;
                chiSquared += (diff * diff) / expected_ij;
            }
        }
    }
    return chiSquared;
}

// double chiSquareIndSum(const std::vector<std::vector<int>>& transMatrix) {
// // Tests if the transitions between sums (from one roll to the next) are independent.
//     int n = transMatrix.size();
//     std::vector<int> rowTotals(n, 0);
//     std::vector<int> colTotals(n, 0);
//     int grandTotal = 0;

//     // Calculate row, column, and grand totals
//     for (int i = 0; i < n; ++i) {
//         for (int j = 0; j < n; ++j) {
//             rowTotals[i] += transMatrix[i][j];
//             colTotals[j] += transMatrix[i][j];
//             grandTotal += transMatrix[i][j];
//         }
//     }

//     double chiSquared = 0.0;
//     for (int i = 0; i < n; ++i) {
//         for (int j = 0; j < n; ++j) {
//             double expected = (double)rowTotals[i] * colTotals[j] / grandTotal;
//             if (expected > 0) {
//                 double diff = transMatrix[i][j] - expected;
//                 chiSquared += (diff * diff) / expected;
//             }
//         }
//     }
//     return chiSquared;
// }

// return contributors list: tuple(contribution, i, j, observed, expected, residual)
std::vector<std::tuple<double,int,int,int,double,double>>
chiSquareIndSumWithDiagnostics(const std::vector<std::vector<int>>& transMatrix,
                               double &chiOut) {
// Like chiSquareIndSum, but also records which transitions contribute most to the test statistic.
    int n = (int)transMatrix.size();
    std::vector<int> rowTotals(n, 0);
    std::vector<int> colTotals(n, 0);
    int grandTotal = 0;
    for (int i = 0; i < n; ++i)
        for (int j = 0; j < n; ++j) {
            rowTotals[i] += transMatrix[i][j];
            colTotals[j] += transMatrix[i][j];
            grandTotal += transMatrix[i][j];
        }

    chiOut = 0.0;
    std::vector<std::tuple<double,int,int,int,double,double>> contribs;
    contribs.reserve(static_cast<size_t>(n) * static_cast<size_t>(n));
    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < n; ++j) {
            double expected = 0.0;
            if (grandTotal > 0)
                expected = (double)rowTotals[i] * (double)colTotals[j] / (double)grandTotal;
            if (expected > 0.0) {
                double diff = transMatrix[i][j] - expected;
                double c = (diff*diff) / expected;
                double resid = diff / sqrt(expected);
                chiOut += c;
                contribs.emplace_back(c, i, j, transMatrix[i][j], expected, resid);
            }
        }
    }
    // sort contributions descending
    std::sort(contribs.begin(), contribs.end(),
              [](auto &a, auto &b){ return std::get<0>(a) > std::get<0>(b); });
    return contribs;
}

// compute lag-1 autocorrelation of sums vector
double lag1_autocorr(const std::vector<int>& sums) {
// Measures how much each sum is correlated with the previous sum (lag-1 autocorrelation).
    if (sums.size() < 2) return 0.0;
    double n = (double)sums.size();
    double mean = 0.0;
    for (int v : sums) mean += v;
    mean /= n;
    double num = 0.0, den = 0.0;
    for (size_t i = 0; i+1 < sums.size(); ++i)
        num += (sums[i] - mean) * (sums[i+1] - mean);
    for (size_t i = 0; i < sums.size(); ++i)
        den += (sums[i] - mean) * (sums[i] - mean);
    if (den == 0.0) return 0.0;
    return num / den;
}

// This version was a test with minor changes to reduce memory usage and improve speed
// permutation test: permute prior sums and recompute chi-square many times
// double permutation_pvalue_chi_parallel2(const std::vector<int>& prevSums, const std::vector<int>& currSums, int maxSum, int nperms, double observedChi, mt19937_64& rng, int nthreads=0) {
//     // Shuffles the previous sums many times to see how often you get a chi-squared value as extreme as observed (permutation test for independence).
//     // build original contingency as counts

//     const size_t N = prevSums.size();
//     if (N == 0 || nperms <= 0) return 1.0;
//     // Determine actual sum range [base..maxVal]; base should equal numDice in your setup
//     int base = std::numeric_limits<int>::max();
//     int maxVal = std::numeric_limits<int>::min();
//     for (int v : prevSums) { base = std::min(base, v); maxVal = std::max(maxVal, v); }
//     for (int v : currSums) { base = std::min(base, v); maxVal = std::max(maxVal, v); }
//     // Fall back to provided maxSum if needed
//     maxVal = std::max(maxVal, maxSum);
//     int S = maxVal - base + 1;

//     // Precompute row/col totals once (constant across permutations)
//     std::vector<int> rowCounts(S, 0), colCounts(S, 0);
//     for (int v : prevSums) rowCounts[v - base]++;
//     for (int v : currSums) colCounts[v - base]++;

//     // Decide thread count and partition work
//     unsigned hw = std::thread::hardware_concurrency();
//     int T = nthreads > 0 ? nthreads : (hw ? (int)hw : 2);
//     T = std::max(1, std::min(T, nperms));
//     std::vector<int> chunk(T, nperms / T);
//     for (int r = 0; r < nperms % T; ++r) ++chunk[r];

//     // Derive per-thread seeds from the provided rng (single-threaded draw)
//     std::vector<uint64_t> seeds(T);
//     for (int t = 0; t < T; ++t) seeds[t] = rng();

//     std::vector<int> ge_counts(T, 0);
//     std::vector<std::thread> threads;
//     threads.reserve(T);

//     for (int ti = 0; ti < T; ++ti) {
//         threads.emplace_back([&, ti]{
//             std::mt19937_64 trng(seeds[ti]);
//             // Local working buffers
//             std::vector<int> prior = prevSums;
//             std::vector<std::vector<int>> Tp(S, std::vector<int>(S, 0));

//             int ge = 0;
//             for (int p = 0; p < chunk[ti]; ++p) {
//                 // Reset
//                 for (int i = 0; i < S; ++i) {
//                     std::fill(Tp[i].begin(), Tp[i].end(), 0);
//                 }
//                 // Permute and build contingency in compact index space
//                 std::shuffle(prior.begin(), prior.end(), trng);
//                 for (size_t k = 0; k < N; ++k) {
//                     int r = prior[k] - base;
//                     int c = currSums[k] - base;
//                     Tp[r][c]++;
//                 }
//                 // Compute chi using precomputed marginals
//                 double chi = 0.0;
//                 for (int i = 0; i < S; ++i) {
//                     for (int j = 0; j < S; ++j) {
//                         double exp = (N > 0) ? (double)rowCounts[i] * colCounts[j] / (double)N : 0.0;
//                         if (exp > 0.0) {
//                             double d = Tp[i][j] - exp;
//                             chi += (d * d) / exp;
//                         }
//                     }
//                 }
//                 if (chi >= observedChi) ++ge;
//             }
//             ge_counts[ti] = ge;
//         });
//     }
//     for (auto& th : threads) th.join();

//     long long ge_total = 0;
//     for (int v : ge_counts) ge_total += v;
//     return (double)(ge_total + 1) / (double)(nperms + 1); // add-one correction
// }

// permutation test: permute prior sums and recompute chi-square many times
double permutation_pvalue_chi_parallel(const std::vector<int>& prevSums, const std::vector<int>& currSums, int maxSum, int nperms, double observedChi, mt19937_64& rng, int nthreads=0) {
    // Shuffles the previous sums many times to see how often you get a chi-squared value as extreme as observed (permutation test for independence).
    // build original contingency as counts

    const size_t N = prevSums.size();
    if (N == 0 || nperms <= 0) return 1.0;
    // Decide thread count
    unsigned hw = std::thread::hardware_concurrency();
    int T = nthreads > 0 ? nthreads : (hw ? (int)hw : 2);
    T = std::max(1, std::min(T, nperms));
    
    // Derive per-thread seeds from the provided rng (single-threaded) to avoid sharing rng
    std::vector<uint64_t> seeds(T);
    for (int t = 0; t < T; ++t) {
        seeds[t] = rng();
    }
    
    // Partition work across threads
    std::vector<int> chunk(T, nperms / T);
    for (int r = 0; r < nperms % T; ++r) ++chunk[r];
    
    std::vector<int> ge_counts(T, 0);
    std::vector<std::thread> threads;
    threads.reserve(T);
    
    for (int ti = 0; ti < T; ++ti) {
        threads.emplace_back([&, ti]{
            std::mt19937_64 trng(seeds[ti]);
    
            // Local working buffers to avoid contention
            std::vector<int> prior = prevSums;
            std::vector<std::vector<int>> Tp(maxSum + 1, std::vector<int>(maxSum + 1, 0));
            std::vector<int> rtot(maxSum + 1, 0), ctot(maxSum + 1, 0);
    
            int ge = 0;
            for (int p = 0; p < chunk[ti]; ++p) {
                // Reset
                for (int i = 0; i <= maxSum; ++i) {
                    std::fill(Tp[i].begin(), Tp[i].end(), 0);
                }
                std::fill(rtot.begin(), rtot.end(), 0);
                std::fill(ctot.begin(), ctot.end(), 0);
    
                // Permute and build contingency
                std::shuffle(prior.begin(), prior.end(), trng);
                for (size_t k = 0; k < N; ++k) Tp[ prior[k] ][ currSums[k] ]++;
    
                // Compute chi-square
                double chi = 0.0;
                int grand = 0;
                for (int i = 0; i <= maxSum; ++i) for (int j = 0; j <= maxSum; ++j) {
                    rtot[i] += Tp[i][j];
                    ctot[j] += Tp[i][j];
                    grand += Tp[i][j];
                }
                for (int i = 0; i <= maxSum; ++i) for (int j = 0; j <= maxSum; ++j) {
                    double exp = (grand > 0) ? (double)rtot[i] * ctot[j] / (double)grand : 0.0;
                    if (exp > 0.0) {
                        double d = Tp[i][j] - exp;
                        chi += (d * d) / exp;
                    }
                }
                if (chi >= observedChi) ++ge;
            }
            ge_counts[ti] = ge;
        });
    }
    
    for (auto& th : threads) th.join();
    long long ge_total = 0;
    for (int v : ge_counts) ge_total += v;
    
    return (double)(ge_total + 1) / (double)(nperms + 1); // add-one correction
}

// Old slow single-threaded version
// double permutation_pvalue_chi(const std::vector<int>& prevSums, const std::vector<int>& currSums, int maxSum, int nperms, double observedChi, mt19937_64& rng) {
//     std::vector<std::vector<int>> T(maxSum+1, std::vector<int>(maxSum+1,0));
//     size_t N = prevSums.size();
//     for (size_t k = 0; k < N; ++k) {
//         T[prevSums[k]][currSums[k]]++;
//     }

//     int ge = 0;
//     // make a vector of prior-sum values to shuffle
//     std::vector<int> prior = prevSums;
//     std::vector<std::vector<int>> Tp(maxSum+1, std::vector<int>(maxSum+1,0));
//     std::vector<int> rtot(maxSum+1,0), ctot(maxSum+1,0);
//     for (int p = 0; p < nperms; ++p) {
//         for (int i=0;i<=maxSum;++i) { std::fill(Tp[i].begin(), Tp[i].end(), 0); }
//         std::fill(rtot.begin(), rtot.end(), 0);
//         std::fill(ctot.begin(), ctot.end(), 0);
//         std::shuffle(prior.begin(), prior.end(), rng);
//         for (size_t k = 0; k < N; ++k) Tp[ prior[k] ][ currSums[k] ]++;
//         double chi = 0.0;
//         int grand=0;
//         for (int i=0;i<=maxSum;++i) for (int j=0;j<=maxSum;++j) {
//             rtot[i]+=Tp[i][j];
//             ctot[j]+=Tp[i][j];
//             grand+=Tp[i][j];
//         }
//         for (int i=0;i<=maxSum;++i) for (int j=0;j<=maxSum;++j) {
//             double exp = (grand>0) ? (double)rtot[i]*ctot[j]/(double)grand : 0.0;
//             if (exp>0.0) {
//                 double d = Tp[i][j]-exp;
//                 chi += (d*d)/exp;
//             }
//         }
//         if (chi >= observedChi) ++ge;
//     }
//     return (double)(ge+1) / (double)(nperms+1); // add-one correction
// }

// Old slow single-threaded version with changes to reduce memory usage and improve speed
// double permutation_pvalue_chi(const std::vector<int>& prevSums, const std::vector<int>& currSums, int maxSum, int nperms, double observedChi, mt19937_64& rng) {
//     // Determine actual sum range [base..maxVal]; base should equal numDice in your setup
//     int base = std::numeric_limits<int>::max();
//     int maxVal = std::numeric_limits<int>::min();
//     for (int v : prevSums) { base = std::min(base, v); maxVal = std::max(maxVal, v); }
//     for (int v : currSums) { base = std::min(base, v); maxVal = std::max(maxVal, v); }
//     // Fall back to provided maxSum if needed
//     maxVal = std::max(maxVal, maxSum);
//     int S = maxVal - base + 1;

//     // Precompute row/col totals once (constant across permutations)
//     std::vector<int> rowCounts(S, 0), colCounts(S, 0);
//     for (int v : prevSums) rowCounts[v - base]++;
//     for (int v : currSums) colCounts[v - base]++;

//     // Local buffers
//     std::vector<int> prior = prevSums;
//     std::vector<std::vector<int>> Tp(S, std::vector<int>(S, 0));

//     int ge = 0;
//     for (int p = 0; p < nperms; ++p) {
//         // Reset
//         for (int i = 0; i < S; ++i) {
//             std::fill(Tp[i].begin(), Tp[i].end(), 0);
//         }
//         std::shuffle(prior.begin(), prior.end(), rng);

//         // Build contingency in compact index space
//         for (size_t k = 0; k < N; ++k) {
//             int r = prior[k] - base;
//             int c = currSums[k] - base;
//             Tp[r][c]++;
//         }

//         // Compute chi using precomputed marginals (expected is constant per cell)
//         double chi = 0.0;
//         for (int i = 0; i < S; ++i) {
//             for (int j = 0; j < S; ++j) {
//                 double exp = (N > 0) ? (double)rowCounts[i] * colCounts[j] / (double)N : 0.0;
//                 if (exp > 0.0) {
//                     double d = Tp[i][j] - exp;
//                     chi += (d * d) / exp;
//                 }
//             }
//         }
//         if (chi >= observedChi) ++ge;
//     }

//     return (double)(ge + 1) / (double)(nperms + 1); // add-one correction
// }

struct RollTestResults {
// Holds all the results and statistics from a dice rolling experiment.
    std::vector<int> rollCount;
    double chiDist;
    std::vector<int> rollSum;
    double chiSums;
    std::vector<std::vector<int>> horizTrans;
    double chiHorInd;
    std::vector<std::vector<int>> vertTrans;
    double chiVertInd;
    std::vector<std::vector<int>> sumTrans;
    std::vector<int> prevSumsList;
    std::vector<int> currSumsList;
    double chiObserved;
    std::vector<std::tuple<double,int,int,int,double,double>> topContribs;
    double lag1Autocorr;
    double permPValue;
};

RollTestResults rollTest(int numRolls, int numDice){
// Simulates rolling numDice dice numRolls times, collects statistics, and runs all tests in parallel threads.
    using clock = std::chrono::high_resolution_clock;
    DiceRoller roller(numDice);
    std::vector<int> rollCount(6, 0);
    int minSum = numDice;
    int maxSum = numDice * 6;
    std::vector<int> rollSum(maxSum + 1, 0);
    std::vector<std::vector<int>> sumTrans(maxSum + 1, std::vector<int>(maxSum + 1, 0));
    std::vector<std::vector<int>> vertTrans(6, std::vector<int>(6, 0));
    std::vector<std::vector<int>> horizTrans(6, std::vector<int>(6, 0));
    std::vector<int> prevRoll(numDice, 0);
    //std::vector<std::vector<int>> results;
    std::vector<int> prevSumsList;
    std::vector<int> currSumsList;
    prevSumsList.reserve(std::max(0, numRolls-1));
    currSumsList.reserve(std::max(0, numRolls-1));

    for(int i = 0; i < numRolls; ++i) {
        vector<int> roll = roller.rollDice();
        //results.push_back(roll);
        int sum = 0;
        int prevSum = 0;

        // Sequential transitions within a roll
        for(int j = 0; j < numDice; ++j) {
            rollCount[roll[j] - 1]++;
            sum += roll[j];
            // Compare die j to die (j+1)%numDice
            int nextIdx = (j + 1) % numDice;
            int curr = roll[j];
            int next = roll[nextIdx];
            vertTrans[curr - 1][next - 1]++;
            if(i > 0) {
                int prev = prevRoll[j];
                int curr = roll[j];
                horizTrans[prev - 1][curr - 1]++;
                prevSum += prevRoll[j];
            }
        }
        if (sum >= minSum && sum <= maxSum) {
            rollSum[sum]++;
        }
        // Track transitions between sums
        if (i > 0 && prevSum >= minSum && prevSum <= maxSum && sum >= minSum && sum <= maxSum) {
            sumTrans[prevSum][sum]++;
            prevSumsList.push_back(prevSum);
            currSumsList.push_back(sum);
        }
        prevRoll = std::move(roll);
    }


    double chiDist = 0.0;
    double chiSums = 0.0;
    double chiHorInd = 0.0;
    double chiVertInd = 0.0;
    double chiObserved = 0.0;
    double lag1Autocorr = 0.0;
    double permPValue = 0.0;
    std::vector<std::tuple<double,int,int,int,double,double>> topContribs;
    // chiObserved = chiSquareIndSum(sumTrans);  Old version that didnt have diagnostics for my failing sum transitions
    topContribs = chiSquareIndSumWithDiagnostics(sumTrans, chiObserved);
    topContribs.resize(std::min(20, (int)topContribs.size()));
    std::thread t1([&]{ permPValue = permutation_pvalue_chi_parallel(prevSumsList, currSumsList, maxSum, 1000, chiObserved, roller.getEngine()); });
    lag1Autocorr = lag1_autocorr(currSumsList);
    chiDist = chiSquareDist(rollCount.data(), numRolls * numDice);
    chiHorInd = chiSquareInd(horizTrans, numRolls - 1);
    chiVertInd = chiSquareInd(vertTrans, numRolls - 1);
    chiSums = chiSquareSum(rollSum, numDice, numRolls);
    t1.join();

    return RollTestResults{
        rollCount,
        chiDist,
        rollSum,
        chiSums,
        horizTrans,
        chiHorInd,
        vertTrans,
        chiVertInd,
        sumTrans,
        prevSumsList,
        currSumsList,
        chiObserved,
        topContribs,
        lag1Autocorr,
        permPValue
    };
}

void printMatrix(const std::vector<std::vector<int>>& matrix, int minIdx, int maxIdx) {
// Nicely prints a matrix (like transition counts) for inspection.
    cout << "        ";
    for (int j = minIdx; j <= maxIdx; ++j) cout << setw(7) << j;
    cout << endl;
    for (int i = minIdx; i <= maxIdx; ++i) {
        cout << setw(7) << i << " |";
        for (int j = minIdx; j <= maxIdx; ++j) {
            cout << setw(7) << matrix[i][j];
        }
        cout << endl;
    }
}

void printRollTestResults(const RollTestResults& results, int numDice) {
// Prints all the results and statistics for a dice rolling experiment, including pass/fail for each test.
    const double chiCritSumsArr[4] = {18.31, 26.12, 33.92, 41.64}; // 2-5 dice
    const double chiCritSumIndArr[4] = {44.31, 95.02, 163.65, 486.36}; // 2-5 dice, alpha=0.01

    cout << "\nResults for " << numDice << " dice:" << endl;
    // Uniformity distribution
    cout << "Number of times each number was rolled:\n";
    for (size_t i = 0; i < results.rollCount.size(); ++i) {
        cout << (i + 1) << ": " << results.rollCount[i] << endl;
    }
    cout << "\nCritical value (df=5, alpha=0.05): 11.07\n";
    if (results.chiDist > 11.07)
        cout << "\nResult: " << results.chiDist << " Fail\n";
    else
        cout << "\nResult: " << results.chiDist << " Pass\n";

    // Sums distribution
    cout << "\nSum distribution:\n";
    int minSum = numDice;
    int maxSum = numDice * 6;
    for (int s = minSum; s <= maxSum; ++s) {
        cout << s << ": " << results.rollSum[s] << endl;
    }

    cout << "\nCritical value (df=" << (maxSum-minSum) << ", alpha=0.05): ";
    if (numDice >= 2 && numDice <= 5) {
        cout << chiCritSumsArr[numDice-2] << "\n";
        if (results.chiSums > chiCritSumsArr[numDice-2])
            cout << "\nResult: " << results.chiSums << " Fail\n";
        else
            cout << "\nResult: " << results.chiSums << " Pass\n";
    } else {
        cout << "[see chi-squared table]\n";
        cout << "\nResult: " << results.chiSums << " [no pass/fail]\n";
    }

    // Horizontal independence
    cout << "\nHorizontal Transition Counts (for independence test):\n";
    printMatrix(results.horizTrans, 0, 5);

    cout << "\nCritical value (df=25, alpha=0.05): 37.65" << endl;
    if (results.chiHorInd > 37.65)
        cout << "\nResult: " << results.chiHorInd << " Fail\n";
    else
        cout << "\nResult: " << results.chiHorInd << " Pass\n";

    // Vertical independence
    cout << "\nVertical Transition Counts (for independence test):\n";
    printMatrix(results.vertTrans, 0, 5);

    cout << "\nCritical value (df=25, alpha=0.05): 37.65" << endl;
    if (results.chiVertInd > 37.65)
        cout << "\nResult: " << results.chiVertInd << " Fail\n";
    else
        cout << "\nResult: " << results.chiVertInd << " Pass\n";

    // Sum independence
    cout << "\nSum Transition Counts (for independence test):\n";
    printMatrix(results.sumTrans, minSum, maxSum);

    // This function is modified to use the dianostics version
    cout << "\nCritical value (df=" << (maxSum-minSum)*(maxSum-minSum) << ", alpha=0.01): ";
    if (numDice >= 2 && numDice <= 5) {
        cout << chiCritSumIndArr[numDice-2] << "\n";
        if (results.chiObserved > chiCritSumIndArr[numDice-2])
            cout << "\nResult: " << results.chiObserved << " Fail\n";
        else
            cout << "\nResult: " << results.chiObserved << " Pass\n";
    } else {
        cout << "[see chi-squared table]\n";
        cout << "\nResult: " << results.chiObserved << " [no pass/fail]\n";
    }
    cout << "Lag-1 autocorrelation of sums: " << results.lag1Autocorr << endl;
    cout << "Permutation empirical p-value for chi-square: " << results.permPValue << endl;

    cout << "\nTop " << results.topContribs.size() << " cell contributions (contrib, row(prevSum), col(currSum), obs, exp, resid):\n";
    for (int i = 0; i < results.topContribs.size(); ++i) {
        auto [c, r, col, obs, exp, resid] = results.topContribs[i];
        cout << i << ": " << c << "  (" << r << "," << col << ")  obs=" << obs << " exp=" << exp << " resid=" << resid << "\n";
    }
}

int main() {
    // Thread pool using std::async for parallel rollTest
    auto fut5 = std::async(std::launch::async, rollTest, 7776000, 5);
    auto fut4 = std::async(std::launch::async, rollTest, 6480000, 4);
    auto fut3 = std::async(std::launch::async, rollTest, 4320000, 3);
    auto fut2 = std::async(std::launch::async, rollTest, 3600000, 2);

    constexpr int numRolls = 3600000;
    int rollCount[6] = {0};
    vector<vector<int>> transCount(6, vector<int>(6, 0));
    DiceRoller roller(1);
    int prev = 0;

    // Batch random generation for single die
    for(int i = 0; i < numRolls; ++i) {
        int val = roller.rollOne();
        ++rollCount[val - 1];
        if(i > 0) {
            int curr = val;
            transCount[prev - 1][curr - 1]++;
        }
        prev = val;
    }

    double chiDist = 0.0;
    double chiInd = 0.0;
    auto futDist = std::async(std::launch::async, chiSquareDist, rollCount, numRolls);
    auto futInd = std::async(std::launch::async, chiSquareInd, transCount, numRolls - 1);

    cout << "Number of times each number was rolled:\n";
    for (int i = 0; i < 6; ++i) {
        cout << (i + 1) << ": " << rollCount[i] << '\n';
    }

    chiDist = futDist.get();
    cout << "\nCritical value (df=5, alpha=0.05): 11.07\n";
    if (__builtin_expect(chiDist > 11.07, 0)) // [[unlikely]]
        cout << "\nResult: " << chiDist << " Fail\n" << endl;
    else
        cout << "\nResult: " << chiDist << " Pass\n" << endl;

    printMatrix(transCount, 0, 5);

    chiInd = futInd.get();
    cout << "\nCritical value (df=25, alpha=0.05): 37.65" << endl;
    if (__builtin_expect(chiInd > 37.65, 0)) // [[unlikely]]
        cout << "\nResult: " << chiInd << " Fail\n";
    else
        cout << "\nResult: " << chiInd << " Pass\n";

    // Wait for all dice results
    printRollTestResults(fut2.get(), 2);
    printRollTestResults(fut3.get(), 3);
    printRollTestResults(fut4.get(), 4);
    printRollTestResults(fut5.get(), 5);

    return 0;
}