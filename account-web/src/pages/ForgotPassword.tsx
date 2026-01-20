import { useState } from 'react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Link } from 'react-router-dom';
import api from '@/lib/api';
import { Loader2, ArrowLeft } from 'lucide-react';

export default function ForgotPassword() {
    const [email, setEmail] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [status, setStatus] = useState<'idle' | 'success' | 'error'>('idle');
    const [message, setMessage] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setStatus('idle');
        setMessage('');

        try {
            const response = await api.post('/auth/forgot-password', { email });
            setStatus('success');
            setMessage(response.data.message || 'If an account exists with this email, a password reset link has been sent.');
        } catch (err: any) {
            setStatus('error');
            setMessage(err.response?.data?.message || 'Failed to process request. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
            <Card className="w-full max-w-md shadow-lg">
                <CardHeader className="space-y-1">
                    <div className="flex justify-center mb-4">
                        <img src="https://nrkgo.com/assets/logo.png" alt="NRKGo" className="h-12 w-auto" />
                    </div>
                    <CardTitle className="text-2xl text-center font-bold">Forgot Password</CardTitle>
                    <CardDescription className="text-center">
                        Enter your email address and we'll send you a link to reset your password.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    {status === 'success' ? (
                        <div className="bg-green-50 border border-green-200 text-green-700 p-4 rounded-md text-sm text-center">
                            {message}
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-4">
                            {status === 'error' && (
                                <div className="p-3 text-sm text-red-500 bg-red-50 border border-red-200 rounded-md">
                                    {message}
                                </div>
                            )}
                            <div className="space-y-2">
                                <Label htmlFor="email">Email</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="m@example.com"
                                    required
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                />
                            </div>
                            <Button type="submit" className="w-full" disabled={isLoading}>
                                {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                                Send Reset Link
                            </Button>
                        </form>
                    )}
                </CardContent>
                <CardFooter className="justify-center">
                    <Link to="/login" className="flex items-center text-sm text-muted-foreground hover:text-primary transition-colors">
                        <ArrowLeft className="h-4 w-4 mr-2" />
                        Back to Login
                    </Link>
                </CardFooter>
            </Card>
        </div>
    );
}
