export default function Skeleton({ lines = 3 }: { lines?: number }) {
  return (
    <div className="skeleton">
      {Array.from({ length: lines }).map((_, i) => (
        <div key={i} className="skeletonLine" style={{ width: `${80 + Math.random() * 20}%` }} />
      ))}
    </div>
  );
}