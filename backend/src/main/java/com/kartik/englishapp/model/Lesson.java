package com.kartik.englishapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int chapterNo;

    @Column(nullable = false)
    private String chapterTitle;

    @Column(nullable = false)
    private String tenseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelBand levelBand;

    @Lob
    @Column(nullable = false)
    private String lessonText;

    @Column(nullable = false)
    private String practiceQuestion;

    @Column(nullable = false)
    private String sampleAnswer;

    public Long getId() { return id; }
    public int getChapterNo() { return chapterNo; }
    public void setChapterNo(int chapterNo) { this.chapterNo = chapterNo; }
    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }
    public String getTenseName() { return tenseName; }
    public void setTenseName(String tenseName) { this.tenseName = tenseName; }
    public LevelBand getLevelBand() { return levelBand; }
    public void setLevelBand(LevelBand levelBand) { this.levelBand = levelBand; }
    public String getLessonText() { return lessonText; }
    public void setLessonText(String lessonText) { this.lessonText = lessonText; }
    public String getPracticeQuestion() { return practiceQuestion; }
    public void setPracticeQuestion(String practiceQuestion) { this.practiceQuestion = practiceQuestion; }
    public String getSampleAnswer() { return sampleAnswer; }
    public void setSampleAnswer(String sampleAnswer) { this.sampleAnswer = sampleAnswer; }
}
