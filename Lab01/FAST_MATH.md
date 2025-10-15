# Fast Math Compiler Flags

## GCC/Clang

- Add `-ffast-math` to your compile flags for aggressive floating-point optimizations.
- Example:
  ```bash
  g++ -O3 -ffast-math -march=native -o main main.cpp DiceRoller.cpp -lpthread -lcurl
  ```
- For even more, use `-Ofast` (implies `-ffast-math`):
  ```bash
  g++ -Ofast -march=native -o main main.cpp DiceRoller.cpp -lpthread -lcurl
  ```

## MSVC

- Use `/fp:fast` for fast floating-point math.

## Notes

- Fast math may break IEEE compliance and strict accuracy.
- Use only if you do not require strict floating-point correctness.
