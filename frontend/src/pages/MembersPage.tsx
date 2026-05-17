import { FormEvent, useEffect, useState } from 'react';
import { Member, createMember, deleteMember, listMembers } from '../api/members';

export default function MembersPage() {
  const [members, setMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [pendingId, setPendingId] = useState<number | null>(null);

  async function refresh() {
    setError(null);
    setLoading(true);
    try {
      const data = await listMembers();
      setMembers(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load members');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    if (!name.trim() || !email.trim()) return;
    setError(null);
    try {
      await createMember({ name: name.trim(), email: email.trim() });
      setName('');
      setEmail('');
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create member');
    }
  }

  async function handleDelete(member: Member) {
    setError(null);
    setPendingId(member.id);
    try {
      await deleteMember(member.id);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to delete member');
    } finally {
      setPendingId(null);
    }
  }

  return (
    <section>
      <h2 className="page__title">Members</h2>

      {error && <div className="alert alert--error">{error}</div>}

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Add a member</h3>
        <form className="form" onSubmit={handleCreate}>
          <div className="form__row">
            <label className="form__label" htmlFor="name">Name</label>
            <input
              id="name"
              className="form__input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <div className="form__row">
            <label className="form__label" htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              className="form__input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div>
            <button type="submit" className="button button--primary">Add member</button>
          </div>
        </form>
      </div>

      {loading ? (
        <p>Loading members…</p>
      ) : members.length === 0 ? (
        <div className="empty">No members yet — add the first one above.</div>
      ) : (
        <ul className="list">
          {members.map((member) => (
            <li key={member.id} className="list__item">
              <div className="list__info">
                <span className="list__title">{member.name}</span>
                <span className="list__meta">{member.email}</span>
              </div>
              <button
                className="button button--danger"
                onClick={() => handleDelete(member)}
                disabled={pendingId === member.id}
              >
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
