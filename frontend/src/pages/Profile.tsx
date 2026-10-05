import { useUser, useStats } from '../hooks/useApi';

export default function Profile() {
  const { user, loading } = useUser();
  const { stats } = useStats();

  if (loading) return <div className="loading">Загрузка…</div>;
  if (!user) return <div className="error">Профиль не доступен</div>;

  return (
    <div className="profile">
      <div className="avatar">
        {user.avatarUrl ? (
          <img src={user.avatarUrl} alt={user.firstName} />
        ) : (
          <div className="avatarFallback">{user.firstName?.[0] || '?'}</div>
        )}
      </div>
      <h2>{user.firstName} {user.lastName || ''}</h2>
      {user.username && <div className="username">@{user.username}</div>}
      
      <div className="stats">
        <div className="stat">
          <span className="stat-value">{user.xp}</span>
          <span className="stat-label">XP</span>
        </div>
        <div className="stat">
          <span className="stat-value">{user.streak} 🔥</span>
          <span className="stat-label">Streak</span>
        </div>
        <div className="stat">
          <span className="stat-value">{user.progressPercent}%</span>
          <span className="stat-label">Прогресс</span>
        </div>
      </div>

      <div className="level-badge">{user.level}</div>
    </div>
  );
}