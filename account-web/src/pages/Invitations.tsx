import { useEffect, useState, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import api from '@/lib/api';
import { toast } from '@/lib/toast-event';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

interface InvitationDetails {
    email: string;
    orgName: string;
    firstName: string;
    lastName: string;
    newUser: boolean;
}

export default function Invitations() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { user, isLoading: isAuthLoading, refreshUser } = useAuth();

    const [invitation, setInvitation] = useState<InvitationDetails | null>(null);
    const [isLoadingDetails, setIsLoadingDetails] = useState(true);
    const [isProcessing, setIsProcessing] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Form state for shadow users
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [password, setPassword] = useState('');

    const token = searchParams.get('token');

    useEffect(() => {
        if (token) {
            fetchInvitationDetails();
        } else {
            setError("Invalid invitation link. Token is missing.");
            setIsLoadingDetails(false);
        }
    }, [token]);

    const fetchInvitationDetails = async () => {
        try {
            const response = await api.get(`/orgs/invitation-details?token=${token}`);
            if (response.data.success) {
                const details = response.data.data;
                setInvitation(details);
                setFirstName(details.firstName || '');
                setLastName(details.lastName || '');
                // Magic Link Support: Backend might have set a cookie. Refresh auth state.
                await refreshUser();
            }
        } catch (err: any) {
            console.error("Fetch invite error:", err);
            setError(err.response?.data?.message || "Invalid or expired invitation link.");
        } finally {
            setIsLoadingDetails(false);
        }
    };

    const hasAttemptedAccept = useRef(false);

    // Auto-Accept if user is logged in (Magic Link Flow)
    useEffect(() => {
        if (user && invitation && !invitation.newUser && !hasAttemptedAccept.current) {
            hasAttemptedAccept.current = true;
            handleAcceptExisting();
        }
    }, [user, invitation]);

    const handleAcceptExisting = async () => {
        if (isProcessing) return;
        setIsProcessing(true);
        try {
            const response = await api.post(`/orgs/accept-invite?token=${token}`);
            if (response.data.success) {
                toast.success("Joined successfully!");
                await refreshUser();
                navigate('/settings/profile');
            }
        } catch (err: any) {
            // Check if error is because it was JUST accepted (race condition or re-try)
            if (err.response?.data?.message?.includes('Invalid invitation token') || err.response?.data?.message?.includes('already accepted')) {
                console.log("Token invalid/used, assuming auto-join success, redirecting...");
                navigate('/settings/profile');
            } else {
                toast.error(err.response?.data?.message || "Failed to join organization.");
                hasAttemptedAccept.current = false; // Allow retry
            }
        } finally {
            setIsProcessing(false);
        }
    };

    const handleClaimAccount = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsProcessing(true);
        try {
            const response = await api.post('/orgs/claim-account', {
                token,
                password,
                firstName,
                lastName
            });
            if (response.data.success) {
                toast.success("Account activated! Welcome aboard.");
                await refreshUser();
                navigate('/settings/organization');
            }
        } catch (err: any) {
            toast.error(err.response?.data?.message || "Failed to activate account.");
            console.error("Claim account error:", err);
        } finally {
            setIsProcessing(false);
        }
    };

    // Wait for auth to load only if we might need to auto-redirect (we don't anymore, but UI might depend on 'user')
    // But we shouldn't block initial fetch. 'isAuthLoading' blocks the whole UI in previous code?
    // Line 99 in previous file: if (isLoadingDetails || isAuthLoading) return <Loading />
    // This IS OKAY as long as useAuth doesn't redirect.

    if (isLoadingDetails) { // Removed isAuthLoading check to prevent hanging if Auth fails silently
        return (
            <div className="min-h-screen flex items-center justify-center bg-slate-50">
                <div className="text-center space-y-4">
                    <div className="size-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
                    <p className="text-slate-500 font-medium">Fetching invitation details...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
                <Card className="w-full max-w-sm shadow-xl border-slate-200">
                    <CardHeader className="text-center pt-8">
                        <div className="size-16 bg-red-50 text-red-500 rounded-full flex items-center justify-center mx-auto mb-4">
                            <span className="material-symbols-outlined text-3xl">error</span>
                        </div>
                        <CardTitle className="text-xl text-slate-900">Unable to Join</CardTitle>
                        <CardDescription>{error}</CardDescription>
                    </CardHeader>
                    <CardContent className="px-8 pb-8">
                        <Button className="w-full bg-blue-600 hover:bg-blue-700 h-11 rounded-xl font-semibold mt-2" onClick={() => navigate('/')}>
                            Go to Home
                        </Button>
                    </CardContent>
                </Card>
            </div>
        );
    }

    if (!invitation) return null;

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4 py-12">
            <Card className="w-full max-w-lg shadow-xl border-slate-200">
                <CardHeader className="text-center space-y-2">
                    <div className="size-16 bg-blue-50 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-2">
                        <span className="material-symbols-outlined text-3xl">mail</span>
                    </div>
                    <CardTitle className="text-2xl font-bold text-slate-900">You've Been Invited!</CardTitle>
                    <CardDescription className="text-base text-slate-500">
                        Join <span className="font-semibold text-slate-900">{invitation.orgName}</span> on NRKGo
                    </CardDescription>
                </CardHeader>
                <CardContent className="px-8 pb-8">
                    {invitation.newUser ? (
                        <form onSubmit={handleClaimAccount} className="space-y-4 pt-2">
                            <div className="p-4 bg-blue-50/50 rounded-xl border border-blue-100 mb-6">
                                <p className="text-sm text-blue-800 leading-relaxed font-medium text-center">
                                    Welcome! To join the organization, please complete your account setup.
                                </p>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-1.5">
                                    <Label htmlFor="firstName" className="text-xs uppercase tracking-wider text-slate-500 font-bold ml-1">First Name</Label>
                                    <Input id="firstName" value={firstName} onChange={e => setFirstName(e.target.value)} required placeholder="Jane" className="h-11 rounded-xl border-slate-200 focus:ring-blue-500 focus:border-blue-500" />
                                </div>
                                <div className="space-y-1.5">
                                    <Label htmlFor="lastName" className="text-xs uppercase tracking-wider text-slate-500 font-bold ml-1">Last Name</Label>
                                    <Input id="lastName" value={lastName} onChange={e => setLastName(e.target.value)} placeholder="Doe" className="h-11 rounded-xl border-slate-200 focus:ring-blue-500 focus:border-blue-500" />
                                </div>
                            </div>

                            <div className="space-y-1.5">
                                <Label htmlFor="email" className="text-xs uppercase tracking-wider text-slate-500 font-bold ml-1">Email</Label>
                                <Input id="email" value={invitation.email} disabled className="h-11 rounded-xl bg-slate-50 border-slate-200 text-slate-500" />
                            </div>

                            <div className="space-y-1.5">
                                <Label htmlFor="password" className="text-xs uppercase tracking-wider text-slate-500 font-bold ml-1">Set Password</Label>
                                <Input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required placeholder="••••••••" minLength={8} className="h-11 rounded-xl border-slate-200 focus:ring-blue-500 focus:border-blue-500" />
                                <p className="text-[11px] text-slate-400 ml-1">Must be at least 8 characters.</p>
                            </div>

                            <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 h-11 rounded-xl font-bold mt-4 shadow-lg shadow-blue-200 transition-all hover:scale-[1.01] active:scale-[0.99]" disabled={isProcessing}>
                                {isProcessing ? "Activating..." : "Create Account & Join"}
                            </Button>
                        </form>
                    ) : (
                        <div className="space-y-6 text-center pt-4">
                            {!user ? (
                                <>
                                    <p className="text-slate-600 text-sm leading-relaxed">
                                        You already have an account with <span className="font-semibold text-slate-800">{invitation.email}</span>. Please log in to accept this invitation.
                                    </p>
                                    <Button className="w-full bg-blue-600 hover:bg-blue-700 h-11 rounded-xl font-bold shadow-lg shadow-blue-200" onClick={() => navigate(`/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`)}>
                                        Log In to Join
                                    </Button>
                                </>
                            ) : (
                                <>
                                    <div className="p-4 bg-emerald-50 rounded-xl border border-emerald-100">
                                        <p className="text-sm text-emerald-800 font-medium">
                                            Logged in as <span className="font-bold underline decoration-emerald-300 underline-offset-2">{user.email}</span>
                                        </p>
                                    </div>
                                    <Button className="w-full bg-blue-600 hover:bg-blue-700 h-11 rounded-xl font-bold shadow-lg shadow-blue-200 transition-all hover:scale-[1.01] active:scale-[0.99]" onClick={handleAcceptExisting} disabled={isProcessing}>
                                        {isProcessing ? "Joining..." : "Accept Invitation"}
                                    </Button>
                                </>
                            )}
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
