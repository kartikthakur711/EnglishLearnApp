package com.kartik.englishapp.service;

import com.kartik.englishapp.model.UserAccount;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StreakService {

    public void updateForToday(UserAccount user) {
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastActiveDate();

        if (last == null) {
            user.setDailyStreak(1);
        } else if (last.equals(today)) {
            return;
        } else if (last.plusDays(1).equals(today)) {
            user.setDailyStreak(user.getDailyStreak() + 1);
        } else {
            user.setDailyStreak(1);
        }
        user.setLastActiveDate(today);
    }
}
