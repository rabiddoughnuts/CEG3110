# Profile-Guided Optimization (PGO) Instructions

## GCC/Clang Example

### 1. Build with profiling instrumentation:

```bash
g++ -O2 -fprofile-generate -o main_pgo main.cpp DiceRoller.cpp -lpthread -lcurl
```

### 2. Run the instrumented binary to collect profile data:

```bash
./main_pgo
```

### 3. Rebuild using the collected profile data:

```bash
g++ -O3 -fprofile-use -o main_pgo_opt main.cpp DiceRoller.cpp -lpthread -lcurl
```

## MSVC Example

1. Compile with `/GL /GENPROFILE` flags.
2. Run the instrumented binary.
3. Recompile with `/GL /USEPROFILE`.

## Notes

- Use representative workloads for profiling.
- Clean up `.gcda`/`.gcno` files after use.
- For more details, see your compiler's documentation.
