import { MEMBER_API_URL, request } from './client';

export type Member = {
  id: number;
  name: string;
  email: string;
};

export function listMembers(): Promise<Member[]> {
  return request<Member[]>(`${MEMBER_API_URL}/members`);
}

export function createMember(payload: Omit<Member, 'id'>): Promise<Member> {
  return request<Member>(`${MEMBER_API_URL}/members`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function deleteMember(id: number): Promise<void> {
  return request<void>(`${MEMBER_API_URL}/members/${id}`, { method: 'DELETE' });
}
