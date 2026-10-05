import { useState } from 'react';
import AudioButton from './AudioButton';
import ResultFeedback from './ResultFeedback';
import ProgressBar from './ProgressBar';

export default function QuizCard({ item, onAnswer, response, onNext, onComplete, isLast, submitting }: any) {
  const [selectedOption, setSelectedOption] = useState<number | null>(null);
  const [selectedText, setSelectedText] = useState<string>('');

  const options = item.options ? JSON.parse(item.options) : [];

  const handleSubmit = () => {
    if (item.type === 'MULTIPLE_CHOICE' && selectedOption !== null) {
      onAnswer({ selectedOptionIndex: selectedOption });
    } else if (item.type === 'TRANSLATION') {
      onAnswer({ selectedText });
    }
  };

  const isAnswered = response !== null;

  return (
    <div className="quizCard">
      {item.audioUrl && <AudioButton url={item.audioUrl} />}
      
      {item.prompt && <div className="prompt">{item.prompt}</div>}
      {item.buryat && <div className="buryat">{item.buryat}</div>}
      {item.russian && <div className="russian">{item.russian}</div>}
      {item.promptTranslation && <div className="promptTranslation">{item.promptTranslation}</div>}

      {item.type === 'MULTIPLE_CHOICE' && options.length > 0 && (
        <div className="options">
          {options.map((opt: string, i: number) => (
            <button
              key={i}
              className={`option ${selectedOption === i ? 'selected' : ''} ${isAnswered && i === response?.correctOptionIndex ? 'correct' : ''}`}
              onClick={() => setSelectedOption(i)}
              disabled={isAnswered}
            >
              {opt}
            </button>
          ))}
        </div>
      )}

      {item.type === 'TRANSLATION' && options.length > 0 && (
        <div className="options">
          {options.map((opt: string, i: number) => (
            <button
              key={i}
              className={`option ${selectedText === opt ? 'selected' : ''} ${isAnswered && i === response?.correctOptionIndex ? 'correct' : ''}`}
              onClick={() => setSelectedText(opt)}
              disabled={isAnswered}
            >
              {opt}
            </button>
          ))}
        </div>
      )}

      {isAnswered && response && (
        <ResultFeedback
          correct={response.correct}
          xpAwarded={response.xpAwarded}
          explanation={response.explanation}
        />
      )}

      {!isAnswered && (selectedOption !== null || selectedText) && (
        <button className="submitBtn" onClick={handleSubmit} disabled={submitting}>
          {submitting ? 'Отправка…' : 'Проверить'}
        </button>
      )}

      {isAnswered && response?.correct && !isLast && (
        <button className="nextBtn" onClick={onNext}>
          Продолжить
        </button>
      )}

      {isAnswered && isLast && (
        <button className="finishBtn" onClick={onComplete}>
          Завершить урок
        </button>
      )}
    </div>
  );
}