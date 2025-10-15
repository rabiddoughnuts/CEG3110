// DiceRoller.cpp
// This file provides random dice rolling using multiple sources of randomness for extra unpredictability.
#include <thread>
#include "DiceRoller.h"

using namespace std;

DiceRoller::DiceRoller(int numDice) noexcept {
    engines.reserve(numDice);
    // auto hash64 = [](uint64_t x) {
    //     x ^= (x >> 33);
    //     x *= 0xff51afd7ed558ccdULL;
    //     x ^= (x >> 33);
    //     x *= 0xc4ceb9fe1a85ec53ULL;
    //     x ^= (x >> 33);
    //     return x;
    // };
    for (int i = 0; i < numDice; ++i) {
        std::random_device rd;
        uint64_t seed = ((uint64_t)rd()) << 32 | rd();
        engines.emplace_back(std::mt19937_64(seed));
        // uint64_t rd_seed = ((uint64_t)rd()) << 32 | rd();
        // uint64_t time = std::chrono::duration_cast<std::chrono::milliseconds>(
        //     std::chrono::system_clock::now().time_since_epoch()).count();
        // uint64_t mixed = time ^ rd_seed ^ (i * 0x9e3779b97f4a7c15ULL);
        // uint64_t h1 = hash64(mixed);
        // uint64_t h2 = hash64(mixed + i);
        // uint64_t h3 = hash64(mixed ^ (i << 16));
        // uint64_t h4 = hash64(mixed * (i+1));
        // engines.emplace_back(h1, h2, h3, h4);
        // std::this_thread::sleep_for(std::chrono::microseconds(1100));
    }
}

thread_local std::uniform_int_distribution<int> DiceRoller::dist(1, 6);