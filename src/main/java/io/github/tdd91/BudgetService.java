/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Bo-Xuan Fan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.tdd91;

import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BudgetService
 *
 * @author Bo-Xuan Fan
 * @since 2018-11-10
 */
public class BudgetService {

    private IBudgetRepo repo;

    public double totalAmount(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            return 0.0;
        }

        List<Budget> budgets = repo.getAll();
        Map<String, Integer> budgetPerDayMap = findBudgetPerDayMap(budgets);

        Map<YearMonth, Integer> durationDays = new HashMap<>();
        YearMonth startYearMonth = YearMonth.from(start);
        YearMonth endYearMonth = YearMonth.from(end);

        // same month
        if (startYearMonth.equals(endYearMonth)) {
            int days = start.until(end).getDays() + 1;
            durationDays.put(startYearMonth, days);
        } else {
            // diff month
            do {
                int days;

                if (startYearMonth.equals(YearMonth.from(start))) {
                    days = start.until(startYearMonth.atEndOfMonth()).getDays() + 1;
                } else if (startYearMonth.equals(YearMonth.from(end))) {
                    days = startYearMonth.atDay(1).until(end).getDays() + 1;
                } else {
                    days = startYearMonth.lengthOfMonth();
                }

                durationDays.put(startYearMonth, days);
                startYearMonth = startYearMonth.plusMonths(1);

            } while (!startYearMonth.isAfter(endYearMonth));
        }

        return durationDays.entrySet().stream()
            .map(entry -> {
                String key = DateTimeFormatter.ofPattern("yyyyMM").format(entry.getKey());
                if (budgetPerDayMap.containsKey(key)) {
                    return entry.getValue() * budgetPerDayMap.get(key);
                }
                return 0.0;
            })
            .reduce(0.0, (aDouble, number) -> aDouble.doubleValue() + number.doubleValue())
            .doubleValue();
    }

    private Map<String, Integer> findBudgetPerDayMap(List<Budget> budgets) {
        return budgets.stream()
            .peek(budget -> {
                int monthDays = YearMonth.parse(budget.getYearMonth(), DateTimeFormatter.ofPattern("yyyyMM")).lengthOfMonth();
                budget.setAmount(budget.getAmount() / monthDays);
            })
            .collect(toMap(Budget::getYearMonth, Budget::getAmount));
    }

}
