export default function ProgressBar({ progress, total }: { progress: number; total: number }) {
  const percent = Math.round((progress / total) * 100);
  return (
    <div className="progressBar" style={{ width: `${percent}%` }} />
  );
}