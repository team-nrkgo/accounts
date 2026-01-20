import axios from 'axios';

// Create Axios instance
const api = axios.create({
    baseURL: '/api', // Vite proxy will handle the redirect to localhost:8080
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true, // Critical: Send cookies with every request
});

import { toast } from './toast-event';

// Response Interceptor for Error Handling
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Handle 400 Bad Request with Message
        if (error.response && error.response.status === 400 && error.response.data) {
            const { success, message } = error.response.data;
            if (success === false && message) {
                toast.error(message);
            }
        }

        // Handle 401 (Unauthorized) - e.g., redirect to login
        if (error.response && error.response.status === 401) {
            if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/signup')) {
                // Optional: Clear any local state if needed
                // window.location.href = '/login'; 
                // We let the UI handle the redirect based on auth state to prevent loops
            }
        }
        return Promise.reject(error);
    }
);

export default api;
