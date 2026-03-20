import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export const authService = {
    login: async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        if (response.data && response.data.token) {
            localStorage.setItem('token', response.data.token);
        }
        return response.data;
    },
    register: async (displayedName, email, password) => {
        const response = await api.post('/auth/register', { displayedName, email, password });
        if (response.data && response.data.token) {
            localStorage.setItem('token', response.data.token);
        }
        return response.data;
    },
    logout: () => {
        localStorage.removeItem('token');
    }
};

export const mapService = {
    getAllMaps: async () => {
        const response = await api.get('/maps');
        return response.data;
    },
    uploadMap: async (content) => {
        const response = await api.post('/maps/upload', { content });
        return response.data;
    }
};

export const heuristicService = {
    submit: async (sourceCode) => {
        const response = await api.post('/heuristics/submit', { sourceCode });
        return response.data;
    },
    getLeaderboard: async () => {
        const response = await api.get('/heuristics/leaderboard');
        return response.data;
    },
    getStatus: async (submissionId) => {
        const response = await api.get(`/heuristics/${submissionId}`);
        return response.data;
    },
    getResults: async (submissionId) => {
        const response = await api.get(`/heuristics/${submissionId}/results`);
        return response.data;
    }
};

export default api;