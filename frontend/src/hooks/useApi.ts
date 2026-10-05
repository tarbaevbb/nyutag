import { useState, useEffect, useCallback } from 'react';
import { get, post } from '../api/client';
import type { User, Lesson, LessonItem, AnswerResponse, CompleteResponse, Stats, ProgressData } from '../api/types';

export function useUser() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    get<User>('/me')
      .then(setUser)
      .catch((e: any) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return { user, loading, error, refetch: () => { setLoading(true); get<User>('/me').then(setUser).catch((e: any) => setError(e.message)).finally(() => setLoading(false)); } };
}

export function useLessons() {
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    get<Lesson[]>('/lessons')
      .then(setLessons)
      .catch((e: any) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return { lessons, loading, error, refetch: () => { setLoading(true); get<Lesson[]>('/lessons').then(setLessons).catch((e: any) => setError(e.message)).finally(() => setLoading(false)); } };
}

export function useLesson(id: number) {
  const [lesson, setLesson] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    get<any>(`/lessons/${id}`)
      .then(setLesson)
      .catch((e: any) => {
        if (e.status === 403 || e.status === 404) {
          setError('Этот урок пока недоступен');
        } else {
          setError(e.message);
        }
      })
      .finally(() => setLoading(false));
  }, [id]);

  return { lesson, loading, error };
}

export function useCompleteLesson() {
  const [completing, setCompleting] = useState(false);
  const [result, setResult] = useState<CompleteResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const complete = async (lessonId: number) => {
    setCompleting(true);
    setError(null);
    try {
      const data = await post<CompleteResponse, void>(`/lessons/${lessonId}/complete`, {});
      setResult(data);
      return data;
    } catch (e: any) {
      setError(e.message);
      throw e;
    } finally {
      setCompleting(false);
    }
  };

  return { complete, result, error, completing };
}

export function useSubmitAnswer() {
  const [submitting, setSubmitting] = useState(false);
  const [response, setResponse] = useState<AnswerResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const submit = async (lessonId: number, itemId: number, answer: any) => {
    setSubmitting(true);
    setError(null);
    try {
      const data = await post<AnswerResponse, any>(`/lessons/${lessonId}/items/${itemId}/answer`, answer);
      setResponse(data);
      return data;
    } catch (e: any) {
      setError(e.message);
      throw e;
    } finally {
      setSubmitting(false);
    }
  };

  return { submit, response, error, submitting };
}

export function useStats() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    get<Stats>('/stats')
      .then(setStats)
      .catch((e: any) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return { stats, loading, error };
}