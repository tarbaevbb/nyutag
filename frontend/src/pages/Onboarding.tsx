import { useNavigate } from 'react-router-dom';
import { useUser, useLessons } from '../hooks/useApi';
import { useEffect } from 'react';

export default function Onboarding() {
  const navigate = useNavigate();
  const { user, loading } = useUser();
  const { lessons } = useLessons();

  useEffect(() => {
    if (!loading && user && !user.isNewUser) {
      navigate('/lessons');
    }
  }, [user, loading, navigate]);

  const firstLesson = lessons.find(l => l.orderIndex === 1) || lessons[0];

  const start = () => {
    if (firstLesson) {
      navigate(`/lesson/${firstLesson.id}`);
    } else {
      navigate('/lessons');
    }
  };

  return (
    <div className="onboarding">
      <h1>Добро пожаловать в СЭСЭН!</h1>
      <p>Изучай бурятский язык шаг за шагом.</p>
      <button onClick={start}>Начать обучение</button>
    </div>
  );
}