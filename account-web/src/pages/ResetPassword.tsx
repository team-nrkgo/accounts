import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import api from '@/lib/api';
import { Loader2, Eye, EyeOff, ArrowLeft } from 'lucide-react';

export default function ResetPassword() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const token = searchParams.get('token');

    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);

    const [isLoading, setIsLoading] = useState(false);
    const [status, setStatus] = useState<'idle' | 'success' | 'error'>('idle');
    const [message, setMessage] = useState('');

    useEffect(() => {
        if (!token) {
            setStatus('error');
            setMessage('Invalid or missing reset token.');
        }
    }, [token]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (password !== confirmPassword) {
            setStatus('error');
            setMessage('Passwords do not match.');
            return;
        }

        if (password.length < 8) {
            setStatus('error');
            setMessage('Password must be at least 8 characters long.');
            return;
        }

        setIsLoading(true);
        setStatus('idle');
        setMessage('');

        try {
            const response = await api.post('/auth/reset-password', { token, newPassword: password });
            setStatus('success');
            setMessage(response.data.message || 'Password reset successfully. You can now login.');
        } catch (err: any) {
            setStatus('error');
            setMessage(err.response?.data?.message || 'Failed to reset password. The link may have expired.');
        } finally {
            setIsLoading(false);
        }
    };

    if (!token) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
                <Card className="w-full max-w-md shadow-lg">
                    <CardHeader>
                        <CardTitle className="text-red-500 text-center">Invalid Request</CardTitle>
                        <CardDescription className="text-center">Missing password reset token.</CardDescription>
                    </CardHeader>
                    <CardFooter className="justify-center">
                        <Link to="/login" className="text-sm text-primary hover:underline">Back to Login</Link>
                    </CardFooter>
                </Card>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
            <Card className="w-full max-w-md shadow-lg">
                <CardHeader className="space-y-1">
                    <div className="flex justify-center mb-4">
                        <img src="https://nrkgo.com/assets/logo.png" alt="NRKGo" className="h-12 w-auto" />
                    </div>
                    <CardTitle className="text-2xl text-center font-bold">Reset Password</CardTitle>
                    <CardDescription className="text-center">
                        Enter a new password for your account.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    {status === 'success' ? (
                        <div className="space-y-4">
                            <div className="bg-green-50 border border-green-200 text-green-700 p-4 rounded-md text-sm text-center">
                                {message}
                            </div>
                            <Button onClick={() => navigate('/login')} className="w-full">
                                Go to Login
                            </Button>
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-4">
                            {status === 'error' && (
                                <div className="p-3 text-sm text-red-500 bg-red-50 border border-red-200 rounded-md">
                                    {message}
                                </div>
                            )}
                            <div className="space-y-2">
                                <Label htmlFor="password">New Password</Label>
                                <div className="relative">
                                    <Input
                                        id="password"
                                        type={showPassword ? "text" : "password"}
                                        required
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        placeholder="Min. 8 characters"
                                    />
                                    <Button
                                        type="button"
                                        variant="ghost"
                                        size="sm"
                                        className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                                        onClick={() => setShowPassword(!showPassword)}
                                    >
                                        {showPassword ? (
                                            <EyeOff className="h-4 w-4 text-muted-foreground" />
                                        ) : (
                                            <Eye className="h-4 w-4 text-muted-foreground" />
                                        )}
                                    </Button>
                                </div>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="confirmPassword">Confirm Password</Label>
                                <Input
                                    id="confirmPassword"
                                    type="password" // Keep confirm hidden always or sync visibility? Usually hidden.
                                    required
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                />
                            </div>
                            <Button type="submit" className="w-full" disabled={isLoading}>
                                {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                                Reset Password
                            </Button>
                        </form>
                    )}
                </CardContent>
                {status !== 'success' && (
                    <CardFooter className="justify-center">
                        <Link to="/login" className="flex items-center text-sm text-muted-foreground hover:text-primary transition-colors">
                            <ArrowLeft className="h-4 w-4 mr-2" />
                            Back to Login
                        </Link>
                    </CardFooter>
                )}
            </Card>
        </div>
    );
}
