import React from 'react';
import { Link, Navigate } from 'react-router-dom';
import { Trophy, Code2, ArrowRight, Map } from 'lucide-react';

export default function Home() {
    const token = localStorage.getItem('token');

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    return (
        <div className="max-w-6xl mx-auto space-y-8">
            {/* Hero Section */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-10 text-center">
                <div className="mx-auto bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mb-6">
                    <Code2 size={32} className="text-blue-600" />
                </div>
                <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight mb-4">
                    Welcome to the Arena
                </h1>
                <p className="text-lg text-gray-600 max-w-2xl mx-auto">
                    Write your custom Java heuristic. We'll inject it into our A* engine, compile it in an isolated Docker container, and run it against the toughest Sokoban maps.
                </p>
            </div>

            {/* Action Cards */}
            <div className="grid md:grid-cols-3 gap-6">
                
                {/* Submit Card */}
                <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 flex flex-col items-start hover:shadow-md transition-shadow group">
                    <div className="bg-green-100 p-3 rounded-lg mb-5">
                        <Code2 className="text-green-600" size={24} />
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">Submit Heuristic</h3>
                    <p className="text-gray-600 mb-6 flex-grow">
                        Use the integrated Monaco editor to write your algorithm. Track your nodes expanded and execution time in real-time.
                    </p>
                    <Link 
                        to="/submit" 
                        className="inline-flex items-center gap-2 bg-green-600 hover:bg-green-700 text-white font-medium px-5 py-2.5 rounded-lg transition-colors w-full justify-center sm:w-auto"
                    >
                        Enter Arena <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />
                    </Link>
                </div>

                {/* Leaderboard Card */}
                <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 flex flex-col items-start hover:shadow-md transition-shadow group">
                    <div className="bg-purple-100 p-3 rounded-lg mb-5">
                        <Trophy className="text-purple-600" size={24} />
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">Global Leaderboard</h3>
                    <p className="text-gray-600 mb-6 flex-grow">
                        See how your solution stacks up against other engineers. Rankings are based on efficiency and speed across all validated maps.
                    </p>
                    <Link 
                        to="/leaderboard" 
                        className="inline-flex items-center gap-2 bg-purple-600 hover:bg-purple-700 text-white font-medium px-5 py-2.5 rounded-lg transition-colors w-full justify-center sm:w-auto"
                    >
                        View Rankings <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />
                    </Link>
                </div>

                {/* Map Upload Card */}
                <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 flex flex-col items-start hover:shadow-md transition-shadow group">
                    <div className="bg-orange-100 p-3 rounded-lg mb-5">
                        <Map className="text-orange-600" size={24} />
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">Upload Map</h3>
                    <p className="text-gray-600 mb-6 flex-grow">
                        Add new custom ASCII levels to the global test suite to challenge everyone's algorithms.
                    </p>
                    <Link 
                        to="/maps/upload" 
                        className="inline-flex items-center gap-2 bg-orange-600 hover:bg-orange-700 text-white font-medium px-5 py-2.5 rounded-lg transition-colors w-full justify-center sm:w-auto"
                    >
                        Add Level <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />
                    </Link>
                </div>

            </div>
        </div>
    );
}