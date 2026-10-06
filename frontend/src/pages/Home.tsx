import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser, useLessons } from '../hooks/useApi';

export default function Home() {
  const navigate = useNavigate();
  const { user, loading } = useUser();
  const { lessons } = useLessons();

  useEffect(() => {
    if (!loading && user?.isNewUser) {
      navigate('/onboarding');
    }
  }, [loading, user, navigate]);

  if (loading) return <div className="loading">Загрузка Сэсэна…</div>;
  if (!user) return <div className="error">Не удалось загрузить профиль</div>;

  const currentLesson =
    lessons.find(l => l.status === 'AVAILABLE' || l.status === 'IN_PROGRESS') ||
    lessons.find(l => l.orderIndex === 1);

  return (
    <div>
      <section className="hero">
        <div className="eyebrow">Сайн байна, {user.firstName}!</div>
        <h1>Продоллим<br/>учиться?</h1>
        <p>5–10 минут в день — и ты начнёшь говорить.</p>
        {currentLesson ? (
          <button onClick={() => navigate(`/lesson/${currentLesson.id}`)}>
            Продолжить →
          </button>
        ) : (
          <button onClick={() => navigate('/lessons')}>
            Выбрать урок
          </button>
        )}
      </section>
    </div>
  );
}