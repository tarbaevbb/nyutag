export default function AudioButton({ url }: { url: string }) {
  const handlePlay = () => {
    if (url) {
      const audio = new Audio(url);
      audio.play();
    }
  };

  return (
    <button className="audioButton" onClick={handlePlay} aria-label="Воспроизвести аудио">
      🔊
    </button>
  );
}