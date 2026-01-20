import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { toast } from '@/lib/toast-event';
import api from '@/lib/api';

interface EditMemberModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    member: any; // Using any for simplicity matching member structure from Organization.tsx
    orgId: number;
}

export default function EditMemberModal({ isOpen, onClose, onSuccess, member, orgId }: EditMemberModalProps) {
    const [isLoading, setIsLoading] = useState(false);
    const [roles, setRoles] = useState<any[]>([]);
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        designation: '',
        roleId: '',
    });

    useEffect(() => {
        if (isOpen && member) {
            setFormData({
                firstName: member.firstName || '',
                lastName: member.lastName || '',
                email: member.userEmail || '',
                designation: member.designation || '',
                roleId: member.roleId ? String(member.roleId) : '',
            });

            // Fetch Roles
            fetchRoles();
        }
    }, [isOpen, member]);

    const fetchRoles = async () => {
        try {
            const response = await api.get(`/orgs/roles?org_id=${orgId}`);
            if (response.data.success) {
                setRoles(response.data.data);
            }
        } catch (error) {
            console.error("Failed to fetch roles", error);
        }
    };

    if (!isOpen || !member) return null;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const payload = {
                member_id: member.id,
                org_id: orgId,
                first_name: formData.firstName,
                last_name: formData.lastName,
                designation: formData.designation,
                role_id: formData.roleId ? parseInt(formData.roleId) : undefined
            };

            await api.put('/orgs/member', payload);
            toast.success('Member updated successfully!');
            onSuccess();
            onClose();
        } catch (error) {
            console.error("Update failed", error);
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

                <CardHeader className="pt-8 pb-6 px-8 text-center align-left items-start">
                    <CardTitle className="text-2xl font-bold text-slate-900 text-left">Edit Member</CardTitle>
                </CardHeader>

                <CardContent className="px-8 pb-8">
                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="firstName" className="text-xs font-medium text-slate-500 uppercase tracking-wide">First Name</Label>
                                <Input
                                    id="firstName"
                                    name="firstName"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    className="h-11 bg-slate-50 border-slate-200 focus:bg-white focus:border-blue-600 focus:ring-blue-600/20 transition-colors"
                                />
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="lastName" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Last Name</Label>
                                <Input
                                    id="lastName"
                                    name="lastName"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    className="h-11 bg-slate-50 border-slate-200 focus:bg-white focus:border-blue-600 focus:ring-blue-600/20 transition-colors"
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="email" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Email</Label>
                            <Input
                                id="email"
                                value={formData.email}
                                disabled
                                className="h-11 bg-slate-50/50 border-slate-200 text-slate-500 cursor-not-allowed"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="designation" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Designation</Label>
                            <Input
                                id="designation"
                                name="designation"
                                value={formData.designation}
                                onChange={handleChange}
                                placeholder="e.g. Senior Developer"
                                className="h-11 bg-slate-50 border-slate-200 focus:bg-white focus:border-blue-600 focus:ring-blue-600/20 transition-colors"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="roleId" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Role</Label>
                            <select
                                id="roleId"
                                name="roleId"
                                value={formData.roleId}
                                onChange={handleChange}
                                className="h-11 w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-blue-600 focus:ring-blue-600/20 focus:ring-2 outline-none transition-all appearance-none"
                            >
                                <option value="" disabled>Select Role</option>
                                {roles.map((r) => (
                                    <option key={r.id} value={r.id}>
                                        {r.name} {r.orgId ? '(Custom)' : ''}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <Button
                            type="submit"
                            className="w-full h-11 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl mt-4 text-base shadow-lg shadow-blue-600/20"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Saving...' : 'Save Changes'}
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
