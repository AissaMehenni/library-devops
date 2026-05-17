import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import BooksPage from './pages/BooksPage';
import MembersPage from './pages/MembersPage';
import './App.css';

export default function App() {
  return (
    <div className="app">
      <header className="header">
        <h1 className="header__title">📚 Library</h1>
        <nav className="nav">
          <NavLink to="/books" className={({ isActive }) => (isActive ? 'nav__link nav__link--active' : 'nav__link')}>
            Books
          </NavLink>
          <NavLink to="/members" className={({ isActive }) => (isActive ? 'nav__link nav__link--active' : 'nav__link')}>
            Members
          </NavLink>
        </nav>
      </header>
      <main className="main">
        <Routes>
          <Route path="/" element={<Navigate to="/books" replace />} />
          <Route path="/books" element={<BooksPage />} />
          <Route path="/members" element={<MembersPage />} />
          <Route path="*" element={<Navigate to="/books" replace />} />
        </Routes>
      </main>
    </div>
  );
}
