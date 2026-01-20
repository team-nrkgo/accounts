import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { toast } from '@/lib/toast-event';
import api from '@/lib/api';
import ConfirmDialog from '@/components/common/ConfirmDialog';

interface Session {
    id: number;
    machine_ip: string;
    browser: string;
    device_os: string;
    device_name: string;
    created_time: string;
    cookie: string; // The token
}

export default function Security() {
    const [sessions, setSessions] = useState<Session[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [revokingId, setRevokingId] = useState<number | null>(null);
    const [currentSessionToken, setCurrentSessionToken] = useState<string | null>(null);

    useEffect(() => {
        fetchSessions();
        // Get current session token from cookie to highlight it
        const token = document.cookie
            .split('; ')
            .find(row => row.startsWith('user_session='))
            ?.split('=')[1];
        setCurrentSessionToken(token || null);
    }, []);

    const fetchSessions = async () => {
        setIsLoading(true);
        try {
            const response = await api.get('/user/sessions');
            if (response.data.success) {
                setSessions(response.data.data);
            }
        } catch (error) {
            console.error("Failed to fetch sessions", error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleRevoke = async (sessionId: number) => {
        try {
            const response = await api.delete(`/user/sessions?session_id=${sessionId}`);
            if (response.data.success) {
                toast.success('Session revoked successfully');
                fetchSessions();
            }
        } catch (error) {
            console.error("Failed to revoke session", error);
            toast.error('Failed to revoke session');
        } finally {
            setRevokingId(null);
        }
    };

    return (
        <div className="w-full">
            <div className="px-10 py-8 pb-32 w-full space-y-12">
                <div className="max-w-4xl">
                    <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Security</h1>
                    <p className="text-slate-500 text-sm mt-1">Manage your active sessions and security settings.</p>
                </div>

                <div className="space-y-6">
                    <div>
                        <h3 className="text-sm font-semibold text-slate-900 mb-1">Active Sessions</h3>
                        <p className="text-sm text-slate-500 mb-4">These are the devices currently logged into your account. You can revoke any session to log it out immediately.</p>
                    </div>

                    <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
                        <div className="divide-y divide-slate-100">
                            {isLoading ? (
                                <div className="p-12 text-center text-slate-400">Loading sessions...</div>
                            ) : sessions.length === 0 ? (
                                <div className="p-12 text-center text-slate-400">No active sessions found.</div>
                            ) : (
                                sessions.map((session) => {
                                    const isCurrent = session.cookie === currentSessionToken;
                                    return (
                                        <div key={session.id} className="p-6 flex items-center justify-between hover:bg-slate-50/50 transition-colors">
                                            <div className="flex items-center gap-4">
                                                <div className="size-10 rounded-full bg-slate-100 flex items-center justify-center text-slate-500">
                                                    <span className="material-symbols-outlined text-[20px]">
                                                        {session.device_name?.toLowerCase().includes('mobile') ? 'smartphone' : 'desktop_windows'}
                                                    </span>
                                                </div>
                                                <div>
                                                    <div className="flex items-center gap-2">
                                                        <span className="font-semibold text-slate-900 text-sm">{session.device_name || 'Unknown Device'}</span>
                                                        {isCurrent && (
                                                            <span className="px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 text-[10px] font-bold uppercase tracking-wider">Current Session</span>
                                                        )}
                                                    </div>
                                                    <div className="flex items-center gap-3 mt-0.5">
                                                        <span className="text-xs text-slate-500">{session.browser || 'Unknown Browser'} on {session.device_os || 'Unknown OS'}</span>
                                                        <span className="text-slate-300">•</span>
                                                        <span className="text-xs text-slate-500 font-mono tracking-tight">{session.machine_ip}</span>
                                                    </div>
                                                    <p className="text-[11px] text-slate-400 mt-1">
                                                        First seen: {new Date(session.created_time).toLocaleString()}
                                                    </p>
                                                </div>
                                            </div>

                                            {!isCurrent && (
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    className="text-red-500 hover:text-red-600 hover:bg-red-50 font-semibold text-xs rounded-lg px-4"
                                                    onClick={() => setRevokingId(session.id)}
                                                >
                                                    Revoke
                                                </Button>
                                            )}
                                        </div>
                                    );
                                })
                            )}
                        </div>
                    </div>
                </div>

                <div className="pt-8 border-t border-slate-200 mt-12">
                    <div className="bg-amber-50 border border-amber-200 rounded-xl p-6 flex items-start gap-4">
                        <div className="size-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 shrink-0">
                            <span className="material-symbols-outlined text-[20px]">info</span>
                        </div>
                        <div>
                            <h4 className="text-amber-900 font-bold text-sm mb-1">Security Recommendation</h4>
                            <p className="text-amber-800/80 text-sm leading-relaxed max-w-2xl">
                                If you see any activity from a device or location you don't recognize, we recommend revoking that session immediately and changing your password.
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <ConfirmDialog
                isOpen={revokingId !== null}
                onClose={() => setRevokingId(null)}
                onConfirm={() => revokingId && handleRevoke(revokingId)}
                title="Revoke Session"
                message="Are you sure you want to revoke this session? The device will be logged out immediately."
                confirmText="Revoke Session"
                variant="danger"
            />
        </div>
    );
}
