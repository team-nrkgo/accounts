import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import api from '@/lib/api';
import { useAuth } from '@/context/AuthContext';

interface InviteMemberModalProps {
    isOpen: boolean;
    onClose: () => void;
    onInviteSuccess: () => void;
}

interface Role {
    id: number;
    name: string;
}

import { toast } from '@/lib/toast-event';

export default function InviteMemberModal({ isOpen, onClose, onInviteSuccess }: InviteMemberModalProps) {
    const { currentOrg } = useAuth();
    const [isLoading, setIsLoading] = useState(false);
    const [roles, setRoles] = useState<Role[]>([]);
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        designation: '',
        roleId: '',
    });

    useEffect(() => {
        if (isOpen) {
            fetchRoles();
        }
    }, [isOpen]);

    const fetchRoles = async () => {
        try {
            const response = await api.get('/roles');
            if (response.data.success) {
                setRoles(response.data.data);
                if (response.data.data.length > 0) {
                    setFormData(prev => ({ ...prev, roleId: response.data.data[0].id.toString() }));
                }
            }
        } catch (error) {
            console.error("Failed to fetch roles", error);
        }
    };

    if (!isOpen) return null;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const payload = {
                first_name: formData.firstName,
                last_name: formData.lastName,
                email: formData.email,
                designation: formData.designation,
                role_id: Number(formData.roleId),
                org_id: currentOrg?.id
            };

            await api.post('/orgs/invite', payload);
            toast.success('Invitation sent successfully!');
            onInviteSuccess();
            onClose();
        } catch (error) {
            console.error("Invite failed", error);
            // Global error handler in api.ts will handle 400 validation errors
            // We can add a fallback here if needed, but for now removing the alert is priority
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
            <Card className="w-full max-w-lg shadow-2xl relative animate-in fade-in zoom-in-95 duration-200">
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 transition-colors"
                >
                    <span className="material-symbols-outlined text-xl">close</span>
                </button>

                <CardHeader className="pt-8 pb-6 px-8 text-center">
                    <CardTitle className="text-2xl font-bold text-slate-900">Invite new member!</CardTitle>
                </CardHeader>

                <CardContent className="px-8 pb-8">
                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="firstName" className="text-xs font-medium text-slate-500 uppercase tracking-wide">First name*</Label>
                                <Input
                                    id="firstName"
                                    name="firstName"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    required
                                    className="h-11 bg-slate-50 border-slate-200 focus:bg-white transition-colors"
                                />
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="lastName" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Last name</Label>
                                <Input
                                    id="lastName"
                                    name="lastName"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    className="h-11 bg-slate-50 border-slate-200 focus:bg-white transition-colors"
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="email" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Email*</Label>
                            <Input
                                id="email"
                                name="email"
                                type="email"
                                value={formData.email}
                                onChange={handleChange}
                                required
                                className="h-11 bg-slate-50 border-slate-200 focus:bg-white transition-colors"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="designation" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Designation</Label>
                            <Input
                                id="designation"
                                name="designation"
                                value={formData.designation}
                                onChange={handleChange}
                                className="h-11 bg-slate-50 border-slate-200 focus:bg-white transition-colors"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="roleId" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Role*</Label>
                            <div className="relative">
                                <select
                                    id="roleId"
                                    name="roleId"
                                    value={formData.roleId}
                                    onChange={handleChange}
                                    className="flex h-11 w-full items-center justify-between rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:bg-white focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 appearance-none"
                                >
                                    {roles.map(role => (
                                        <option key={role.id} value={role.id}>{role.name}</option>
                                    ))}
                                </select>
                                <span className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">
                                    <span className="material-symbols-outlined text-lg">expand_more</span>
                                </span>
                            </div>
                        </div>

                        <Button
                            type="submit"
                            className="w-full h-11 bg-slate-900 hover:bg-slate-800 text-white font-semibold rounded-xl mt-4 text-base shadow-lg shadow-slate-900/20"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Sending...' : 'Invite Now'}
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
