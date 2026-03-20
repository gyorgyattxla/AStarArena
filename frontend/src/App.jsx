import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';

import Login from './pages/Login';
import Register from './pages/Register';
import Home from './pages/Home';
import Submit from './pages/Submit';
import Leaderboard from './pages/Leaderboard';
import MapUpload from './pages/MapUpload';

export default function App() {
    return (
        <Layout>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/submit" element={<Submit />} />
                <Route path="/leaderboard" element={<Leaderboard />} />
                <Route path="/maps/upload" element={<MapUpload />} />
            </Routes>
        </Layout>
    );
}