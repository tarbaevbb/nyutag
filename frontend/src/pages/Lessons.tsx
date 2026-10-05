import { useLessons } from '../hooks/useApi';
import LessonCard from '../components/LessonCard';

export default function Lessons() {
  const { lessons, loading } = useLessons();

  if (loading) return <div className="loading">Загрузка уроков…</div>;

  return (
    <div>
      <h2>Твой путь</h2>
      <div className="lessons">
        {lessons.map(l => (
          <LessonCard key={l.id} lesson={l} />
        ))}
      </div>
    </div>
  );
}