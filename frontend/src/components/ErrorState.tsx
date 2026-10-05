export default function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="errorState">
      <p>{message}</p>
      {onRetry && <button onClick={onRetry}>Повторить</button>}
    </div>
  );
}