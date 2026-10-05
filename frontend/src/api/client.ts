const API = '/api';

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const initData = (window as any).Telegram?.WebApp?.initData;
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (initData) {
    headers['X-Telegram-Init-Data'] = initData;
  }

  const response = await fetch(`${API}${endpoint}`, {
    ...options,
    headers: {
      ...headers,
      ...(options.headers as Record<string, string> || {}),
    },
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Network error' }));
    const err = new Error(error.error || 'Request failed');
    (err as any).status = response.status;
    throw err;
  }

  return response.json();
}

export async function get<T>(endpoint: string): Promise<T> {
  return request<T>(endpoint, { method: 'GET' });
}

export async function post<T, B>(endpoint: string, body: B): Promise<T> {
  return request<T>(endpoint, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export { API };