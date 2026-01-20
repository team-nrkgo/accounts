import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from '@/lib/toast-event';
import api from '@/lib/api';

interface EditRoleModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    role?: any; // If present, edit mode. Else create mode.
    orgId: number;
}

export default function EditRoleModal({ isOpen, onClose, onSuccess, role, orgId }: EditRoleModalProps) {
    const [isLoading, setIsLoading] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
    });

    useEffect(() => {
        if (isOpen) {
            if (role) {
                setFormData({
                    name: role.name || '',
                    description: role.description || '',
                });
            } else {
                setFormData({ name: '', description: '' });
            }
        }
    }, [isOpen, role]);

    if (!isOpen) return null;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            if (role) {
                // Edit
                await api.put(`/orgs/roles/${role.id}?org_id=${orgId}`, formData);
                toast.success('Role updated successfully');
            } else {
                // Create
                await api.post(`/orgs/roles?org_id=${orgId}`, formData);
                toast.success('Role created successfully');
            }
            onSuccess();
            onClose();
        } catch (error) {
            console.error("Role operation failed", error);
            // Global 400 handler will catch errors
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
                    <CardTitle className="text-2xl font-bold text-slate-900 text-left">
                        {role ? 'Edit Role' : 'Create Role'}
                    </CardTitle>
                </CardHeader>

                <CardContent className="px-8 pb-8">
                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div className="space-y-2">
                            <Label htmlFor="name" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Role Name</Label>
                            <Input
                                id="name"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                placeholder="e.g. Project Manager"
                                className="h-11 bg-slate-50 border-slate-200 focus:bg-white focus:border-blue-600 focus:ring-blue-600/20 transition-colors"
                                required
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="description" className="text-xs font-medium text-slate-500 uppercase tracking-wide">Description</Label>
                            <Input
                                id="description"
                                name="description"
                                value={formData.description}
                                onChange={handleChange}
                                placeholder="Short description of permissions"
                                className="h-11 bg-slate-50 border-slate-200 focus:bg-white focus:border-blue-600 focus:ring-blue-600/20 transition-colors"
                            />
                        </div>

                        <Button
                            type="submit"
                            className="w-full h-11 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl mt-4 text-base shadow-lg shadow-blue-600/20"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Saving...' : (role ? 'Update Role' : 'Create Role')}
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
