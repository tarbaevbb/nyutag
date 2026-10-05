import { Link } from 'react-router-dom';

export default function LessonCard({ lesson }: { lesson: any }) {
  const isAvailable = lesson.status === 'AVAILABLE' || lesson.status === 'IN_PROGRESS';
  const isCompleted = lesson.status === 'COMPLETED';

  return (
    <Link
      to={isAvailable ? `/lesson/${lesson.id}` : '#'}
      className={`lesson ${lesson.status.toLowerCase()}`}
      style={isCompleted ? { pointerEvents: 'auto', opacity: 1 } : isAvailable ? {} : { opacity: 0.4, pointerEvents: 'none' }}
    >
      <span className="num">{isCompleted ? '✓' : lesson.orderIndex}</span>
      <span>
        <b>{lesson.title}</b>
        {lesson.subtitle && <small>{lesson.subtitle}</small>}
      </span>
      <span>{isCompleted ? '✓' : isAvailable ? '→' : '🔒'}</span>
    </Link>
  );
}