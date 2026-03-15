package com.interview.server.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.protocol.internal.response.Error;
import com.interview.server.model.AocResult;
import com.interview.server.model.AocResultStatus;
import com.interview.server.repository.AocResultRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class AocService {

    private final AocResultRepository aocResultRepository;

    public AocService(AocResultRepository aocResultRepository) {
        this.aocResultRepository = aocResultRepository;
    }

    public List<AocResult> getHistory() {
        return aocResultRepository.findRecent();
    }

    public AocResult solve(
        int day,
        int task,
        String input,
        String expectedOutput,
        boolean test
    ) {
        String output = calculateResult(day, task, input);

        AocResultStatus result;
        if (expectedOutput == null || expectedOutput.isBlank()) {
            result = AocResultStatus.NA;
        } else if (expectedOutput.trim().equals(output.trim())) {
            result = AocResultStatus.SUCCESS;
        } else {
            result = AocResultStatus.FAILURE;
        }

        AocResult aocResult = new AocResult(
            day,
            Uuids.timeBased(),
            task,
            input,
            output,
            result,
            expectedOutput,
            test
        );

        aocResultRepository.save(aocResult);
        return aocResult;
    }

    private String calculateResult(int day, int task, String input) {
        return switch (day) {
            case 1 -> solveDayOne(task, input);
            case 2 -> solveDayTwo(task, input);
            case 3 -> solveDayThree(task, input);
            default -> "Solution for day " +
            day +
            " task " +
            task +
            " not yet implemented";
        };
    }

    public record Range(long start, long end) {
        public LongStream getRepeatedPatternIds() {
            return LongStream.range(start, end + 1).filter((var number) -> {
                if (number < 10) {
                    return false;
                }
                var s = Long.toString(number);
                int len = s.length();
                for (int patLen = 1; patLen <= len / 2; patLen++) {
                    if (len % patLen != 0) {
                        continue;
                    }
                    var pattern = s.substring(0, patLen);
                    boolean match = true;
                    for (int i = patLen; i < len; i += patLen) {
                        if (!s.substring(i, i + patLen).equals(pattern)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return true;
                    }
                }
                return false;
            });
        }

        public LongStream getFakeIds() {
            return LongStream.range(start, end + 1).filter((var number) -> {
                if (number == 0) {
                    return false;
                }
                int length = (int) Math.floor(Math.log10(number)) + 1;
                if (length % 2 != 0) {
                    return false;
                }
                var numberString = Long.toString(number);
                var firstHalf = numberString.substring(0, length / 2);
                var secondHalf = numberString.substring(length / 2);
                return firstHalf.equals(secondHalf);
            });
        }
    }

    private String solveDayTwo(int task, String input) {
        return switch (task) {
            case 1 -> {
                var ranges = Stream.of(input.split(",")).map((var line) -> {
                    var split = line.split("-");
                    var first = Long.parseLong(split[0]);
                    var second = Long.parseLong(split[1]);
                    return new Range(first, second);
                });
                yield Long.toString(
                    ranges
                        .flatMapToLong((Range range) -> range.getFakeIds())
                        .sum()
                );
            }
            case 2 -> {
                var ranges = Stream.of(input.split(",")).map((var line) -> {
                    var split = line.split("-");
                    var first = Long.parseLong(split[0]);
                    var second = Long.parseLong(split[1]);
                    return new Range(first, second);
                });
                yield Long.toString(
                    ranges
                        .flatMapToLong((Range range) -> range.getRepeatedPatternIds())
                        .sum()
                );
            }
            default -> throw new RuntimeException(
                "There is only task one and task two no other tasks"
            );
        };
    }

    private String solveDayThree(int task, String input) {
        return switch (task) {
            case 1 -> {
                yield Integer.toString(input.lines().mapToInt((String line) -> {
                    List<Integer> numbers = line.chars()
                        .map(character -> character - '0') // Convert ASCII/Unicode to numeric value
                        .boxed()                           // Crucial: Converts IntStream to Stream<Integer>
                        .collect(Collectors.toUnmodifiableList());
                    var numbersLen = numbers.size();
                    var firstMaxRange = numbers.subList(0, numbersLen - 1);
                    var firstMax = firstMaxRange.stream().max(Integer::compare).orElseThrow();
                    var indexOfMax = numbers.indexOf(firstMax);
                    var secondMax = numbers.subList(indexOfMax, numbersLen).stream().max(Integer::compare).orElseThrow();
                    return firstMax * 10 + secondMax;
                }).sum());
            }
            case 2 -> {
                yield "";
            }
            default -> throw new RuntimeException(
                "There is only task one and task two no other tasks"
            );
        };
    }

    private String solveDayOne(int task, String input) {
        return switch (task) {
            case 1 -> {
                int dial = 50;
                int count = 0;

                for (String line : input.lines().toList()) {
                    char dir = line.charAt(0);
                    int dist = Integer.parseInt(line.substring(1));
                    dial = switch (dir) {
                        case 'R' -> (dial + dist) % 100;
                        case 'L' -> (((dial - dist) % 100) + 100) % 100;
                        default -> throw new RuntimeException(
                            "This has to be R or L: " + dir
                        );
                    };
                    if (dial == 0) {
                        count++;
                    }
                }

                yield String.valueOf(count);
            }
            case 2 -> {
                int dial = 50;
                int count = 0;

                for (String line : input.lines().toList()) {
                    char dir = line.charAt(0);
                    int dist = Integer.parseInt(line.substring(1));
                    dial = switch (dir) {
                        case 'R' -> {
                            int end = dial + dist;
                            int passes = end / 100;
                            int finalPos = end % 100;
                            if (finalPos == 0 && passes > 0) passes--;
                            count += passes;
                            yield finalPos;
                        }
                        case 'L' -> {
                            int finalPos = Math.floorMod(dial - dist, 100);
                            int passes = (dial == 0)
                                ? (dist == 0 ? 0 : (dist - 1) / 100)
                                : (dist + 100 - dial) / 100;
                            if (finalPos == 0 && passes > 0) passes--;
                            count += passes;
                            yield finalPos;
                        }
                        default -> throw new RuntimeException(
                            "This has to be R or L: " + dir
                        );
                    };
                    if (dial == 0) {
                        count++;
                    }
                }

                yield String.valueOf(count);
            }
            default -> throw new RuntimeException(
                "There is only task one and task two no other tasks"
            );
        };
    }
}
