import { useNavigate } from 'react-router-dom';
import { useUser } from '../hooks/useApi';
import { useEffect } from 'react';

export default function Onboarding() {
  const navigate = useNavigate();
  const { user, loading } = useUser();

  useEffect(() => {
    if (!loading && user && user.xp > 0) {
      navigate('/lessons');
    }
  }, [user, loading, navigate]);

  return (
    <div className="onboarding">
      <h1>Добро пожаловать в СЭСЭН!</h1>
      <p>Изучай бурятский язык шаг за шагом.</p>
      <button onClick={() => navigate('/lessons')}>Начать обучение</button>
    </div>
  );
}