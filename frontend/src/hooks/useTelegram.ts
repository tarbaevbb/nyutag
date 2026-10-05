import { useEffect, useState } from 'react';

declare global {
  interface Window {
    Telegram?: {
      WebApp: {
        initEvent?: (type: string, handler: (...args: any[]) => void) => void;
        MainButton?: {
          Text: (text: string) => void;
          show: () => void;
          hide: () => void;
          onClick: (callback: () => void) => void;
          offClick: (callback: () => void) => void;
        };
        ready: () => void;
        expand: () => void;
        setHeaderColor: (color: string) => void;
        setBackgroundColor: (color: string) => void;
        applyTheme: () => void;
      };
    };
  }
}

export function useTelegram() {
  const [ready, setReady] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);

  useEffect(() => {
    const tg = (window as any).Telegram?.WebApp;
    if (!tg) return;

    tg.ready();
    tg.expand();
    setIsExpanded(tg.isExpanded?.() || false);

    const updateTheme = () => {
      const bgColor = tg.backgroundColor || tg.themeParams.bg_color || '#101114';
      const textColor = tg.textColor || tg.themeParams.text_color || '#f5f5f5';
      const buttonColor = tg.buttonColor || tg.themeParams.button_color || '#777d8b';
      const hintColor = tg.hintColor || tg.themeParams.hint_color || '#858995';

      document.documentElement.style.setProperty('--tg-bg-color', bgColor);
      document.documentElement.style.setProperty('--tg-text-color', textColor);
      document.documentElement.style.setProperty('--tg-button-color', buttonColor);
      document.documentElement.style.setProperty('--tg-hint-color', hintColor);
    };

    updateTheme();

    tg.onEvent('themeChanged', updateTheme);

    return () => {
      tg.offEvent('themeChanged', updateTheme);
    };
  }, []);

  return { ready, isExpanded };
}