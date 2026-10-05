export default function ResultFeedback({ correct, xpAwarded, explanation }: {
  correct: boolean;
  xpAwarded: number;
  explanation?: string;
}) {
  return (
    <div className={`feedback ${correct ? 'correct' : 'incorrect'}`}>
      <span className="feedbackIcon">{correct ? '✓' : '✗'}</span>
      <span className="feedbackText">
        {correct ? 'Верно!' : 'Неверно.'}
        {xpAwarded > 0 && <span className="xpAwarded"> +{xpAwarded} XP</span>}
      </span>
      {explanation && <div className="explanation">{explanation}</div>}
    </div>
  );
}