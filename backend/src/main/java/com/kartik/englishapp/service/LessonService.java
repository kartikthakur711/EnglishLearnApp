package com.kartik.englishapp.service;

import com.kartik.englishapp.config.AppException;
import com.kartik.englishapp.dto.AnswerRequest;
import com.kartik.englishapp.dto.AnswerResponse;
import com.kartik.englishapp.dto.LessonDto;
import com.kartik.englishapp.model.Lesson;
import com.kartik.englishapp.model.LessonProgress;
import com.kartik.englishapp.model.LevelBand;
import com.kartik.englishapp.model.UserAccount;
import com.kartik.englishapp.repository.LessonProgressRepository;
import com.kartik.englishapp.repository.LessonRepository;
import com.kartik.englishapp.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LessonService {
    private final LessonRepository lessonRepo;
    private final LessonProgressRepository progressRepo;
    private final UserAccountRepository userRepo;
    private final StreakService streakService;

    public LessonService(LessonRepository lessonRepo,
                         LessonProgressRepository progressRepo,
                         UserAccountRepository userRepo,
                         StreakService streakService) {
        this.lessonRepo = lessonRepo;
        this.progressRepo = progressRepo;
        this.userRepo = userRepo;
        this.streakService = streakService;
    }

    public List<LessonDto> getLessons(UserAccount user, String level, Integer chapterNo) {
        LevelBand levelBand = resolveLevel(user, level);
        List<Lesson> lessons = chapterNo == null
                ? lessonRepo.findByLevelBandOrderByChapterNoAsc(levelBand)
                : lessonRepo.findByLevelBandAndChapterNoOrderByIdAsc(levelBand, chapterNo);

        Map<Long, LessonProgress> progressMap = progressRepo.findByUser(user).stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), Function.identity()));

        return lessons.stream().map(lesson -> {
            LessonProgress p = progressMap.get(lesson.getId());
            return new LessonDto(
                    lesson.getId(),
                    lesson.getChapterNo(),
                    lesson.getChapterTitle(),
                    lesson.getTenseName(),
                    lesson.getLevelBand().name(),
                    lesson.getLessonText(),
                    lesson.getPracticeQuestion(),
                    lesson.getSampleAnswer(),
                    p != null && p.isCompleted(),
                    p == null ? 0 : p.getScore()
            );
        }).toList();
    }

    public List<Integer> getChapterNumbers(UserAccount user, String level) {
        LevelBand levelBand = resolveLevel(user, level);
        Set<Integer> chapters = lessonRepo.findByLevelBandOrderByChapterNoAsc(levelBand)
                .stream().map(Lesson::getChapterNo).collect(Collectors.toSet());
        return chapters.stream().sorted().toList();
    }

    public AnswerResponse submitAnswer(UserAccount user, AnswerRequest request) {
        Lesson lesson = lessonRepo.findById(request.lessonId())
                .orElseThrow(() -> new AppException("Lesson not found"));

        int score = scoreAnswer(request.answerText(), lesson.getSampleAnswer());
        boolean completed = score >= 60;

        LessonProgress progress = progressRepo.findByUserAndLesson(user, lesson).orElse(null);
        boolean existingProgress = progress != null;
        if (progress == null) {
            progress = new LessonProgress();
        }
        boolean wasCompletedBefore = progress.isCompleted();
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setScore(score);
        progress.setCompleted(completed);
        progress.setLastAttemptDate(LocalDate.now());
        progressRepo.save(progress);

        streakService.updateForToday(user);
        int pointsAwarded = 0;
        if (!existingProgress) {
            pointsAwarded = completed ? 15 : 5;
        } else if (!wasCompletedBefore && completed) {
            pointsAwarded = 10;
        }
        if (pointsAwarded > 0) {
            user.setTotalPoints(user.getTotalPoints() + pointsAwarded);
        }
        userRepo.save(user);

        String feedback;
        if (completed && wasCompletedBefore) {
            feedback = "Lesson already completed earlier. No extra points awarded.";
        } else if (!completed && existingProgress) {
            feedback = "Attempt recorded. Improve your answer to complete the lesson.";
        } else if (completed) {
            feedback = "Great work. You understood this tense well.";
        } else {
            feedback = "Keep practicing. Match the structure shown in sample answer.";
        }
        if (pointsAwarded > 0) {
            feedback = feedback + " +" + pointsAwarded + " points.";
        }

        return new AnswerResponse(score, completed, feedback, user.getDailyStreak(), user.getTotalPoints());
    }

    private LevelBand resolveLevel(UserAccount user, String level) {
        if (level == null || level.isBlank()) {
            return user.getLevelBand();
        }
        try {
            return LevelBand.valueOf(level.toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new AppException("Level must be MID or PRO");
        }
    }

    private int scoreAnswer(String answer, String sample) {
        String answerLower = answer.toLowerCase(Locale.ROOT);
        String sampleLower = sample.toLowerCase(Locale.ROOT);

        List<String> keywords = List.of("had", "have", "has", "been", "will", "would", "if", "by", "time", "already");
        long keywordHits = keywords.stream().filter(answerLower::contains).count();

        int similarity = commonWordSimilarity(answerLower, sampleLower);
        int score = (int) Math.min(100, similarity * 0.7 + keywordHits * 6 + Math.min(answer.length(), 100) * 0.1);
        return Math.max(score, 20);
    }

    private int commonWordSimilarity(String a, String b) {
        Set<String> aw = java.util.Arrays.stream(a.split("\\s+")).filter(s -> !s.isBlank()).collect(java.util.stream.Collectors.toSet());
        Set<String> bw = java.util.Arrays.stream(b.split("\\s+")).filter(s -> !s.isBlank()).collect(java.util.stream.Collectors.toSet());
        long common = aw.stream().filter(bw::contains).count();
        return (int) (common * 10);
    }
}
