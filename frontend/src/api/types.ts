export interface User {
  id: number;
  firstName: string;
  lastName?: string;
  username?: string;
  avatarUrl?: string;
  level: string;
  xp: number;
  streak: number;
  currentLesson: number;
  progressPercent: number;
}

export interface Lesson {
  id: number;
  title: string;
  subtitle?: string;
  orderIndex: number;
  xpReward: number;
  status: 'LOCKED' | 'AVAILABLE' | 'IN_PROGRESS' | 'COMPLETED';
}

export interface LessonItem {
  id: number;
  orderIndex: number;
  type: 'PHRASE' | 'WORD' | 'TRANSLATION' | 'MULTIPLE_CHOICE' | 'LISTENING';
  prompt?: string;
  promptTranslation?: string;
  buryat?: string;
  russian?: string;
  audioUrl?: string;
  options?: string;
}

export interface AnswerResponse {
  correct: boolean;
  correctOptionIndex?: number;
  correctAnswerText?: string;
  explanation?: string;
  xpAwarded: number;
}

export interface CompleteResponse {
  xp: number;
  streak: number;
  nextLessonId?: number;
  lessonCompleted: boolean;
}

export interface Stats {
  xp: number;
  streak: number;
  completedLessons: number;
  correctPercent: number;
}

export interface ProgressData {
  progress: Array<{
    id: number;
    userId: number;
    lessonId: number;
    status: string;
    progress: number;
    completedAt?: string;
    attempts: number;
    bestScore?: number;
    xpEarned: number;
  }>;
  lessons: Lesson[];
}