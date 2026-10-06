import { useParams, useNavigate } from 'react-router-dom';
import { useLesson, useSubmitAnswer, useCompleteLesson } from '../hooks/useApi';
import { useEffect, useState } from 'react';
import QuizCard from '../components/QuizCard';

export default function Lesson() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const lessonId = parseInt(id || '1', 10);
  const { lesson, loading, error } = useLesson(lessonId);
  const { submit, response, submitting } = useSubmitAnswer();
  const { complete, result, completing } = useCompleteLesson();

  const [currentItemIndex, setCurrentItemIndex] = useState(0);
  const [answered, setAnswered] = useState(false);

  useEffect(() => {
    setCurrentItemIndex(0);
    setAnswered(false);
  }, [lessonId]);

  if (loading) return <div className="loading">Загрузка урока…</div>;
  if (error) return <div className="error">{error}</div>;
  if (!lesson) return <div className="error">Урок не найден</div>;

  const items = lesson.items || [];
  const currentItem = items[currentItemIndex];

  if (!currentItem) {
    return (
      <div className="lessonComplete">
        <h2>🎉 Урок завершён!</h2>
        {result ? (
          <div className="result">
            <p>XP: +{result.xpEarned}</p>
            <p>Streak: {result.streak} 🔥</p>
            {result.nextLessonId && (
              <button onClick={() => navigate(`/lesson/${result.nextLessonId}`)}>
                Следующий урок →
              </button>
            )}
          </div>
        ) : (
          <p>Прогресс сохранён.</p>
        )}
        <button onClick={() => navigate('/lessons')}>К списку уроков</button>
      </div>
    );
  }

  const handleAnswer = async (answer: any) => {
    try {
      await submit(lessonId, currentItem.id, {
        ...answer,
        responseMs: 0,
      });
      setAnswered(true);
    } catch {}
  };

  const handleNext = () => {
    setAnswered(false);
    setCurrentItemIndex(prev => prev + 1);
  };

  const handleFinish = async () => {
    try {
      await complete(lessonId);
      setCurrentItemIndex(items.length);
    } catch {}
  };

  return (
    <div>
      <button className="back" onClick={() => navigate('/lessons')}>← Назад</button>
      <div className="progress">
        УРОК {currentItem.orderIndex} · {currentItemIndex + 1}/{items.length}
      </div>
      <h1>{lesson.title}</h1>
      <QuizCard
        key={currentItem.id}
        item={currentItem}
        onAnswer={handleAnswer}
        response={answered ? response : null}
        onNext={handleNext}
        onComplete={handleFinish}
        isLast={currentItemIndex === items.length - 1}
        submitting={submitting || completing}
      />
    </div>
  );
}