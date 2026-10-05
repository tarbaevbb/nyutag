import { Link, useLocation } from 'react-router-dom';

export default function BottomNav() {
  const location = useLocation();

  return (
    <nav className="bottomNav">
      <Link to="/" className={location.pathname === '/' ? 'active' : ''}>
        <span>📖</span>
        <small>Учиться</small>
      </Link>
      <Link to="/lessons" className={location.pathname === '/lessons' ? 'active' : ''}>
        <span>📚</span>
        <small>Уроки</small>
      </Link>
      <Link to="/profile" className={location.pathname === '/profile' ? 'active' : ''}>
        <span>👤</span>
        <small>Профиль</small>
      </Link>
    </nav>
  );
}