package com.kartik.englishapp.service;

import com.kartik.englishapp.model.Lesson;
import com.kartik.englishapp.model.LevelBand;
import com.kartik.englishapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final LessonRepository lessonRepository;

    public DataSeeder(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Override
    public void run(String... args) {
        List<Lesson> defaults = List.of(
                lesson(1, "Chapter 1: Daily Actions", "Simple Present", LevelBand.MID,
                        "Use subject + base verb (adds s/es with he/she/it).",
                        "Write one sentence about your routine.",
                        "I study English every evening."),
                lesson(2, "Chapter 2: Ongoing Actions", "Present Continuous", LevelBand.MID,
                        "Use am/is/are + verb-ing for actions happening now.",
                        "Describe what you are doing right now.",
                        "I am learning chapter-wise tenses now."),
                lesson(3, "Chapter 3: Finished Past", "Simple Past", LevelBand.MID,
                        "Use verb second form for completed past action.",
                        "Write one sentence about yesterday.",
                        "I revised grammar yesterday."),
                lesson(4, "Chapter 4: Experience", "Present Perfect", LevelBand.MID,
                        "Use has/have + past participle for result or experience.",
                        "Write about an achievement.",
                        "I have completed four tense chapters."),
                lesson(5, "Chapter 5: Before a Past Point", "Past Perfect", LevelBand.PRO,
                        "Use had + past participle for earlier past action.",
                        "Write a sentence with two past actions.",
                        "She had left before I reached the station."),
                lesson(6, "Chapter 6: Future in Progress", "Future Continuous", LevelBand.PRO,
                        "Use will be + verb-ing for ongoing future action.",
                        "Write what you will be doing tomorrow evening.",
                        "I will be practicing spoken English tomorrow evening."),
                lesson(7, "Chapter 7: Advanced Duration", "Present Perfect Continuous", LevelBand.PRO,
                        "Use has/have been + verb-ing for duration until now.",
                        "Write how long you have been learning English.",
                        "I have been learning English for six months."),
                lesson(8, "Chapter 8: Mixed Conditionals", "Conditional Mix", LevelBand.PRO,
                        "Combine unreal past condition with present/future result.",
                        "Write one mixed conditional sentence.",
                        "If I had practiced more, I would be speaking fluently now.")
        );

        List<Lesson> missing = new ArrayList<>();
        for (Lesson l : defaults) {
            boolean exists = lessonRepository.existsByLevelBandAndChapterNoAndTenseName(
                    l.getLevelBand(), l.getChapterNo(), l.getTenseName()
            );
            if (!exists) {
                missing.add(l);
            }
        }

        if (!missing.isEmpty()) {
            lessonRepository.saveAll(missing);
            log.info("Seeded {} missing lesson(s)", missing.size());
        } else {
            log.info("Lessons already present; no seeding required");
        }
    }

    private Lesson lesson(int chapterNo, String chapterTitle, String tense, LevelBand level, String text, String question, String answer) {
        Lesson l = new Lesson();
        l.setChapterNo(chapterNo);
        l.setChapterTitle(chapterTitle);
        l.setTenseName(tense);
        l.setLevelBand(level);
        l.setLessonText(text);
        l.setPracticeQuestion(question);
        l.setSampleAnswer(answer);
        return l;
    }
}
