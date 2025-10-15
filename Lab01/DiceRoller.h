#pragma once
#include <cstdint>
#include <random>
#include <chrono>
#include <array>

// xoshiro256pp code retained for future use:
class xoshiro256pp {
public:
    using result_type = uint64_t;
    std::array<uint64_t, 4> s;
    explicit xoshiro256pp(const uint64_t seed1, const uint64_t seed2, const uint64_t seed3, const uint64_t seed4) noexcept
        : s{seed1, seed2, seed3, seed4} {}
    static constexpr uint64_t rotl(const uint64_t x, const int k) noexcept {
        return (x << k) | (x >> (64 - k));
    }
    inline uint64_t operator()() noexcept {
        const uint64_t result = rotl(s[0] + s[3], 23) + s[0];
        const uint64_t t = s[1] << 17;
        s[2] ^= s[0];
        s[3] ^= s[1];
        s[1] ^= s[2];
        s[0] ^= s[3];
        s[2] ^= t;
        s[3] = rotl(s[3], 45);
        return result;
    }
    static constexpr uint64_t min() noexcept { return 0; }
    static constexpr uint64_t max() noexcept { return UINT64_MAX; }
};

class DiceRoller {
public:
    DiceRoller(int numDice) noexcept;
    inline std::vector<int> rollDice() noexcept {
        std::vector<int> results;
        results.reserve(engines.size());
        for (auto& eng : engines) {
            results.push_back(dist(eng));
        }
        return results;
    }
    inline int rollOne() noexcept {
        return dist(engines[0]);
    }

    // inline std::vector<int> rollDice(const int numDice) noexcept {
    //     static thread_local std::mt19937_64 rng(std::random_device{}());
    //     std::vector<int> results;
    //     results.reserve(numDice);
    //     for (int i = 0; i < numDice; ++i) {
    //         results.push_back(dist(rng));
    //     }
    //     return results;
    // }
    std::mt19937_64& getEngine(int idx = 0) noexcept { return engines[idx]; }
    static thread_local std::uniform_int_distribution<int> dist;
private:
    std::vector<std::mt19937_64> engines;
};