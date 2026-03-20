import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Code2, Trophy, LogOut, Home, Map } from 'lucide-react';

export default function Layout({ children }) {
    const navigate = useNavigate();
    const location = useLocation();
    const token = localStorage.getItem('token');

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    const NavLink = ({ to, icon: Icon, label }) => {
        const isActive = location.pathname === to;
        return (
            <Link
                to={to}
                className={`flex items-center gap-2 px-4 py-2 rounded-md transition-colors ${
                    isActive
                        ? 'bg-blue-600 text-white'
                        : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                }`}
            >
                <Icon size={18} />
                <span className="font-medium">{label}</span>
            </Link>
        );
    };

    return (
        <div className="min-h-screen flex flex-col bg-gray-50">
            <header className="bg-gray-900 text-white border-b border-gray-800 sticky top-0 z-50">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center gap-8">
                            <Link to="/" className="flex items-center gap-2 hover:opacity-80 transition-opacity">
                                <div className="bg-blue-600 p-1.5 rounded-lg">
                                    <Code2 size={24} className="text-white" />
                                </div>
                                <span className="text-xl font-bold tracking-tight">A* Arena</span>
                            </Link>

                            {token && (
                                <nav className="hidden md:flex space-x-2">
                                    <NavLink to="/" icon={Home} label="Dashboard" />
                                    <NavLink to="/submit" icon={Code2} label="Arena" />
                                    <NavLink to="/leaderboard" icon={Trophy} label="Leaderboard" />
                                    <NavLink to="/maps/upload" icon={Map} label="Maps" />
                                </nav>
                            )}
                        </div>

                        <div className="flex items-center gap-4">
                            {!token ? (
                                <div className="flex items-center gap-3">
                                    <Link
                                        to="/login"
                                        className="text-gray-300 hover:text-white font-medium transition-colors"
                                    >
                                        Log in
                                    </Link>
                                    <Link
                                        to="/register"
                                        className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium transition-colors shadow-sm"
                                    >
                                        Sign up
                                    </Link>
                                </div>
                            ) : (
                                <button
                                    onClick={handleLogout}
                                    className="flex items-center gap-2 text-gray-400 hover:text-red-400 transition-colors px-3 py-2 rounded-md hover:bg-gray-800"
                                >
                                    <LogOut size={18} />
                                    <span className="font-medium">Logout</span>
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </header>

            <main className="flex-grow w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {children}
            </main>
        </div>
    );
}