import { FormEvent, useEffect, useState } from 'react';
import {
  Book,
  borrowBook,
  createBook,
  deleteBook,
  listBooks,
  returnBook,
} from '../api/books';

export default function BooksPage() {
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [pendingId, setPendingId] = useState<number | null>(null);

  async function refresh() {
    setError(null);
    setLoading(true);
    try {
      const data = await listBooks();
      setBooks(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Impossible de charger les livres');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    if (!title.trim() || !author.trim()) return;
    setError(null);
    try {
      await createBook({ title: title.trim(), author: author.trim(), available: true });
      setTitle('');
      setAuthor('');
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Impossible de créer le livre');
    }
  }

  async function handleToggle(book: Book) {
    setError(null);
    setPendingId(book.id);
    try {
      if (book.available) {
        await borrowBook(book.id);
      } else {
        await returnBook(book.id);
      }
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Impossible de mettre à jour le livre');
    } finally {
      setPendingId(null);
    }
  }

  async function handleDelete(book: Book) {
    setError(null);
    setPendingId(book.id);
    try {
      await deleteBook(book.id);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Impossible de supprimer le livre');
    } finally {
      setPendingId(null);
    }
  }

  return (
    <section>
      <h2 className="page__title">Livres</h2>

      {error && <div className="alert alert--error">{error}</div>}

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Ajouter un livre</h3>
        <form className="form" onSubmit={handleCreate}>
          <div className="form__row">
            <label className="form__label" htmlFor="title">Titre</label>
            <input
              id="title"
              className="form__input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </div>
          <div className="form__row">
            <label className="form__label" htmlFor="author">Auteur</label>
            <input
              id="author"
              className="form__input"
              value={author}
              onChange={(e) => setAuthor(e.target.value)}
              required
            />
          </div>
          <div>
            <button type="submit" className="button button--primary">Ajouter</button>
          </div>
        </form>
      </div>

      {loading ? (
        <p>Chargement des livres…</p>
      ) : books.length === 0 ? (
        <div className="empty">Aucun livre pour le moment — ajoutez-en un ci-dessus.</div>
      ) : (
        <ul className="list">
          {books.map((book) => (
            <li key={book.id} className="list__item">
              <div className="list__info">
                <span className="list__title">{book.title}</span>
                <span className="list__meta">par {book.author}</span>
                <span className={`badge ${book.available ? 'badge--available' : 'badge--unavailable'}`}>
                  {book.available ? 'Disponible' : 'Indisponible'}
                </span>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button
                  className={`button ${book.available ? 'button--primary' : 'button--secondary'}`}
                  onClick={() => handleToggle(book)}
                  disabled={pendingId === book.id}
                >
                  {book.available ? 'Emprunter' : 'Rendre'}
                </button>
                <button
                  className="button button--danger"
                  onClick={() => handleDelete(book)}
                  disabled={pendingId === book.id}
                >
                  Supprimer
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
