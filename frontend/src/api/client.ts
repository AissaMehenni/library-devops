export const BOOK_API_URL =
  (import.meta.env.VITE_BOOK_API_URL as string | undefined) ?? 'http://localhost:8081';

export const MEMBER_API_URL =
  (import.meta.env.VITE_MEMBER_API_URL as string | undefined) ?? 'http://localhost:8082';

export async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  if (!response.ok) {
    let message = `${response.status} ${response.statusText}`;
    try {
      const body = await response.json();
      if (body && typeof body === 'object' && 'message' in body) {
        message = String((body as { message: unknown }).message);
      }
    } catch {
      // ignore non-JSON body
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}
