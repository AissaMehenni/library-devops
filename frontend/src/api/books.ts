import { BOOK_API_URL, request } from './client';

export type Book = {
  id: number;
  title: string;
  author: string;
  available: boolean;
};

export function listBooks(): Promise<Book[]> {
  return request<Book[]>(`${BOOK_API_URL}/books`);
}

export function borrowBook(id: number): Promise<Book> {
  return request<Book>(`${BOOK_API_URL}/books/borrow/${id}`, { method: 'POST' });
}

export function returnBook(id: number): Promise<Book> {
  return request<Book>(`${BOOK_API_URL}/books/return/${id}`, { method: 'POST' });
}

export function createBook(payload: Omit<Book, 'id'>): Promise<Book> {
  return request<Book>(`${BOOK_API_URL}/books`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function deleteBook(id: number): Promise<void> {
  return request<void>(`${BOOK_API_URL}/books/${id}`, { method: 'DELETE' });
}
