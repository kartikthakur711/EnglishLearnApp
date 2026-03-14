package com.kartik.englishapp.repository;

import com.kartik.englishapp.model.Lesson;
import com.kartik.englishapp.model.LevelBand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByLevelBandOrderByChapterNoAsc(LevelBand levelBand);
    List<Lesson> findByLevelBandAndChapterNoOrderByIdAsc(LevelBand levelBand, int chapterNo);
}
