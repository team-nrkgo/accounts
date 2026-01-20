import React, { createContext, useContext, useEffect, useState } from 'react';
import api from '@/lib/api';

interface User {
    id: number;
    email: string;
    first_name: string;
    last_name: string | null;
    status: number;
    source: number;
}

interface InitResponse {
    user_information: User;
    default_organizations: any;
    other_organizations: any[];
}

interface AuthContextType {
    user: User | null;
    currentOrg: any | null; // using any for now, ideally strictly typed
    isLoading: boolean;
    login: (user: User) => void;
    logout: () => void;
    refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [currentOrg, setCurrentOrg] = useState<any | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const refreshUser = async () => {
        try {
            // Step 1: Check Session Status
            const statusResponse = await api.get<{ data: boolean }>('/auth/ustatus');
            const isValidSession = statusResponse.data?.data;

            if (!isValidSession) {
                setUser(null);
                setCurrentOrg(null);
                setIsLoading(false);
                return;
            }

            // Step 2: Fetch Data if Valid
            const response = await api.get<{ data: InitResponse }>('/auth/init');
            if (response.data?.data?.user_information) {
                setUser(response.data.data.user_information);
                // Set Default Org
                if (response.data.data.default_organizations) {
                    setCurrentOrg(response.data.data.default_organizations);
                }
            }
        } catch (error) {
            console.log("Auth Flow Error:", error);
            setUser(null);
            setCurrentOrg(null);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        refreshUser();
    }, []);

    const login = (newUser: User) => {
        setUser(newUser);
        // Optimize: In real app, we might need to fetch orgs again or pass them in login
        refreshUser();
    };

    const logout = async () => {
        try {
            await api.post('/auth/logout');
        } catch (error) {
            console.error("Logout failed on server", error);
        } finally {
            setUser(null);
            setCurrentOrg(null);
            // Optional: navigate to login manually if needed, but context update usually triggers it
        }
    };

    return (
        <AuthContext.Provider value={{ user, currentOrg, isLoading, login, logout, refreshUser }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
